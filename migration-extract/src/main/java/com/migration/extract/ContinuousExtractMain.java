package com.migration.extract;

import com.migration.thl.THLEvent;
import com.migration.thl.THLFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContinuousExtractMain {

    private static final Logger logger = LoggerFactory.getLogger(ContinuousExtractMain.class);

    private static final long HEARTBEAT_IDLE_THRESHOLD_MS = 1000;
    private static final long HEARTBEAT_INTERVAL_MS = 1000;

    private com.migration.common.Extractor<byte[], THLEvent> extractor;
    private MySQLBinlogExtractor mysqlExtractor;
    private PostgresWalExtractor pgExtractor;
    private String inputDir;
    private String outputDir;
    private long scanInterval;
    private AtomicBoolean running = new AtomicBoolean(true);
    private String captureType;

    private Map<String, FileProgress> fileProgressMap = new LinkedHashMap<>();
    private String progressRecordFile;

    private THLFileWriter currentThlWriter;
    private File currentThlFile;
    private volatile long lastRealEventTime = System.currentTimeMillis();
    private volatile long lastHeartbeatTime = 0;
    private long heartbeatSeqno = 0;

    public static void main(String[] args) {
        String configPath = null;
        for (int i = 0; i < args.length; i++) {
            if ("--config".equals(args[i]) && i + 1 < args.length) {
                configPath = args[i + 1];
            }
        }

        Properties props = new Properties();

        if (configPath != null) {
            try (InputStream input = new FileInputStream(configPath)) {
                props.load(input);
            } catch (IOException e) {
                logger.error("Failed to load config: {}", configPath, e);
                System.exit(1);
            }
        } else {
            String taskIdHint = System.getProperty("task.id", "unknown");
            String defaultConfig = "files/" + taskIdHint + "/config.properties";
            File configFile = new File(defaultConfig);
            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    props.load(input);
                } catch (IOException e) {
                    logger.error("Failed to load default config", e);
                    System.exit(1);
                }
            }
        }

        String taskId = props.getProperty("task.id", System.getProperty("task.id", "unknown"));
        String captureType = props.getProperty("capture.type", "binlog").toLowerCase();
        logger.info("=== Migration Extract (Continuous) Starting (type={}) ===", captureType);

        try {
            ContinuousExtractMain main = new ContinuousExtractMain();
            main.initialize(props);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received, stopping extract...");
                main.stop();
            }));

            main.start();
        } catch (Exception e) {
            logger.error("Fatal error in Continuous Extract", e);
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    public void initialize(Properties props) throws Exception {
        this.inputDir = props.getProperty("extract.input.dir",
                "files/" + props.getProperty("task.id", "unknown") + "/binlog_output");
        this.outputDir = props.getProperty("extract.output.dir",
                "files/" + props.getProperty("task.id", "unknown") + "/thl_output");
        this.scanInterval = Long.parseLong(props.getProperty("extract.scan.interval", "3000"));
        this.progressRecordFile = outputDir + "/.extract_progress";
        this.captureType = props.getProperty("capture.type", "binlog").toLowerCase();

        if ("wal".equals(captureType) || "postgresql".equals(captureType)) {
            pgExtractor = new PostgresWalExtractor();
            this.extractor = (com.migration.common.Extractor<byte[], THLEvent>) pgExtractor;
            logger.info("Using PostgreSQL WAL Extractor");
        } else {
            mysqlExtractor = new MySQLBinlogExtractor();
            this.extractor = (com.migration.common.Extractor<byte[], THLEvent>) mysqlExtractor;
            logger.info("Using MySQL Binlog Extractor");
        }
        this.extractor.initialize(props);

        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        loadProgress();

        logger.info("Continuous Extract initialized - type: {}, input: {}, output: {}, scanInterval: {}ms",
                captureType, inputDir, outputDir, scanInterval);
    }

    public void start() {
        logger.info("Starting continuous binlog extraction...");

        while (running.get()) {
            try {
                int eventsThisRound = scanAndProcessFiles();

                if (eventsThisRound > 0) {
                    lastRealEventTime = System.currentTimeMillis();
                }

                writeHeartbeatIfNeeded();

                Thread.sleep(scanInterval);
            } catch (InterruptedException e) {
                logger.info("Extract thread interrupted");
                break;
            } catch (Exception e) {
                logger.error("Error during file scanning", e);
            }
        }

        closeCurrentThlWriter();
        closeExtractor();
        saveProgress();
        logger.info("Continuous Extract stopped");
    }

    public void stop() {
        running.set(false);
    }

    private int scanAndProcessFiles() throws Exception {
        File inputDirFile = new File(inputDir);
        if (!inputDirFile.exists()) {
            return 0;
        }

        File[] binlogFiles = inputDirFile.listFiles((dir, name) ->
                name.startsWith("binlog_") && name.endsWith(".cap"));

        if (binlogFiles == null || binlogFiles.length == 0) {
            return 0;
        }

        Arrays.sort(binlogFiles, Comparator.comparing(File::getName));

        int totalEvents = 0;
        for (File binlogFile : binlogFiles) {
            if (!running.get()) break;
            totalEvents += processFileIncremental(binlogFile);
        }
        return totalEvents;
    }

    private int processFileIncremental(File binlogFile) throws Exception {
        FileProgress progress = fileProgressMap.get(binlogFile.getName());

        if (progress == null) {
            progress = new FileProgress(binlogFile.getName());
            fileProgressMap.put(binlogFile.getName(), progress);
            logger.info("Detected new binlog file: {}", binlogFile.getName());
        }

        if (progress.completed) return 0;

        int totalLinesInFile = countLines(binlogFile);
        if (totalLinesInFile <= progress.linesRead) return 0;

        int newLines = totalLinesInFile - progress.linesRead;
        logger.info("Processing binlog file: {} ({} new lines, already read: {}, total: {})",
                binlogFile.getName(), newLines, progress.linesRead, totalLinesInFile);

        int newEventCount = 0;

        String baseName = binlogFile.getName().replace(".cap", "");
        File outputDirFile = new File(outputDir);
        String[] existingThlFiles = outputDirFile.list((dir, name) ->
                name.startsWith(baseName) && name.endsWith(".thl"));
        int thlIndex = (existingThlFiles != null) ? existingThlFiles.length : 0;
        String outputFileName = baseName + "_" + thlIndex + ".thl";
        File outputFile = new File(outputDir, outputFileName);

        try (THLFileWriter thlWriter = new THLFileWriter(outputFile.getAbsolutePath())) {
            newEventCount = readAndExtractNewLines(binlogFile, thlWriter, progress, progress.linesRead);
        }

        if (newEventCount > 0) {
            currentThlFile = outputFile;
        }

        long currentSize = binlogFile.length();
        boolean fileStoppedGrowing = (currentSize == progress.lastFileSize && currentSize > 0);
        progress.lastFileSize = currentSize;

        if (fileStoppedGrowing) {
            progress.stableCheckCount++;
        } else {
            progress.stableCheckCount = 0;
        }

        if (progress.stableCheckCount >= 3) {
            progress.completed = true;
            logger.info("Binlog file {} appears complete, marking as completed", binlogFile.getName());
        }

        logger.info("Processed binlog file: {} -> {} new events", binlogFile.getName(), newEventCount);
        if (newEventCount > 0) {
            saveExtractorSeqno();
        }
        saveProgress();
        return newEventCount;
    }

    private void writeHeartbeatIfNeeded() {
        long now = System.currentTimeMillis();
        long idleMs = now - lastRealEventTime;

        if (idleMs < HEARTBEAT_IDLE_THRESHOLD_MS) {
            return;
        }

        if (now - lastHeartbeatTime < HEARTBEAT_INTERVAL_MS) {
            return;
        }

        if (currentThlFile == null) {
            currentThlFile = createHeartbeatThlFile();
            if (currentThlFile == null) {
                return;
            }
        }

        try {
            if (currentThlWriter == null) {
                currentThlWriter = new THLFileWriter(currentThlFile.getAbsolutePath());
            }

            long seqno = getNextHeartbeatSeqno();

            THLEvent heartbeat = new THLEvent();
            heartbeat.setSeqno(seqno);
            heartbeat.setType(THLEvent.HEARTBEAT_EVENT);
            heartbeat.setSourceTstamp(new Timestamp(now));
            heartbeat.setEventId("heartbeat:" + seqno);
            heartbeat.setSourceId(captureType);
            heartbeat.addMetadata("event_type", "HEARTBEAT");
            heartbeat.addMetadata("heartbeat_timestamp", now);

            currentThlWriter.writeEvent(heartbeat);

            lastHeartbeatTime = now;

            logger.debug("Heartbeat event written: seqno={}, sourceTstamp={}", seqno, heartbeat.getSourceTstamp());
        } catch (Exception e) {
            logger.warn("Failed to write heartbeat event: {}", e.getMessage());
            closeCurrentThlWriter();
        }
    }

    private File createHeartbeatThlFile() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = sdf.format(new Date());
            String fileName = "heartbeat_" + timestamp + ".thl";
            File outputFile = new File(outputDir, fileName);
            return outputFile;
        } catch (Exception e) {
            logger.warn("Failed to create heartbeat THL file: {}", e.getMessage());
            return null;
        }
    }

    private long getNextHeartbeatSeqno() {
        if (mysqlExtractor != null) {
            heartbeatSeqno = mysqlExtractor.getCurrentSeqno();
            mysqlExtractor.incrementSeqnoForHeartbeat();
            return heartbeatSeqno;
        } else if (pgExtractor != null) {
            heartbeatSeqno = pgExtractor.getCurrentSeqno();
            pgExtractor.incrementSeqnoForHeartbeat();
            return heartbeatSeqno;
        }
        return ++heartbeatSeqno;
    }

    private void closeCurrentThlWriter() {
        if (currentThlWriter != null) {
            try {
                currentThlWriter.close();
            } catch (Exception e) {
                logger.warn("Error closing current THL writer: {}", e.getMessage());
            }
            currentThlWriter = null;
        }
    }

    private int countLines(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) count++;
        }
        return count;
    }

    private int readAndExtractNewLines(File binlogFile, THLFileWriter thlWriter,
                                        FileProgress progress, int skipLines) throws Exception {
        int eventCount = 0;
        int currentLine = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(binlogFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                currentLine++;
                if (currentLine <= skipLines) continue;
                if (line.trim().isEmpty()) {
                    progress.linesRead++;
                    continue;
                }

                byte[] eventBytes = line.getBytes("UTF-8");
                THLEvent event = extractor.extract(eventBytes);
                if (event != null && thlWriter != null) {
                    Boolean multiRow = (Boolean) event.getMetadata().get("multi_row");
                    if (multiRow != null && multiRow) {
                        @SuppressWarnings("unchecked")
                        java.util.List<String> rowsData = (java.util.List<String>) event.getMetadata().get("rows_data");
                        @SuppressWarnings("unchecked")
                        java.util.List<String> rowsDataBefore = (java.util.List<String>) event.getMetadata().get("rows_data_before");
                        if (rowsData != null && rowsData.size() > 1) {
                            for (int i = 0; i < rowsData.size(); i++) {
                                THLEvent rowEvent = new THLEvent();
                                rowEvent.setSeqno(event.getSeqno() + i);
                                rowEvent.setEventId(event.getEventId() + "_" + i);
                                rowEvent.setSourceId(event.getSourceId());
                                rowEvent.setSourceTstamp(event.getSourceTstamp());
                                
                                for (java.util.Map.Entry<String, Object> entry : event.getMetadata().entrySet()) {
                                    String key = entry.getKey();
                                    if (!"rows_data".equals(key) && !"multi_row".equals(key) && !"rows_data_before".equals(key)) {
                                        rowEvent.addMetadata(key, entry.getValue());
                                    }
                                }
                                rowEvent.addMetadata("row_data", rowsData.get(i));
                                if (rowsDataBefore != null && i < rowsDataBefore.size()) {
                                    rowEvent.addMetadata("row_data_before", rowsDataBefore.get(i));
                                }
                                
                                thlWriter.writeEvent(rowEvent);
                                eventCount++;
                            }
                            progress.linesRead++;
                            continue;
                        }
                    }
                    thlWriter.writeEvent(event);
                    eventCount++;
                }
                progress.linesRead++;
            }
        }
        return eventCount;
    }

    private void loadProgress() {
        File recordFile = new File(progressRecordFile);
        if (!recordFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(recordFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    FileProgress progress = new FileProgress(parts[0]);
                    progress.linesRead = Integer.parseInt(parts[1]);
                    progress.lastFileSize = Long.parseLong(parts[2]);
                    progress.completed = Boolean.parseBoolean(parts[3]);
                    fileProgressMap.put(parts[0], progress);
                }
            }
            logger.info("Loaded {} file progress records", fileProgressMap.size());
        } catch (IOException e) {
            logger.warn("Error loading extract progress", e);
        }
    }

    private void saveProgress() {
        File recordFile = new File(progressRecordFile);
        File parentDir = recordFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(recordFile))) {
            for (FileProgress progress : fileProgressMap.values()) {
                writer.write(progress.fileName + "|" + progress.linesRead + "|" +
                        progress.lastFileSize + "|" + progress.completed);
                writer.newLine();
            }
        } catch (IOException e) {
            logger.warn("Error saving extract progress", e);
        }
    }

    private static class FileProgress {
        String fileName;
        int linesRead;
        long lastFileSize;
        int stableCheckCount;
        boolean completed;

        FileProgress(String fileName) {
            this.fileName = fileName;
            this.linesRead = 0;
            this.lastFileSize = 0;
            this.stableCheckCount = 0;
            this.completed = false;
        }
    }

    private void closeExtractor() {
        if (mysqlExtractor != null) {
            mysqlExtractor.close();
        } else if (pgExtractor != null) {
            pgExtractor.close();
        }
    }

    private void saveExtractorSeqno() {
        if (mysqlExtractor != null) {
            mysqlExtractor.saveSeqno();
        } else if (pgExtractor != null) {
            pgExtractor.saveSeqno();
        }
    }
}
