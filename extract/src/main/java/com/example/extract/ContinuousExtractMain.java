package com.example.extract;

import com.example.thl.THLEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContinuousExtractMain {

    private static final Logger logger = LoggerFactory.getLogger(ContinuousExtractMain.class);

    private MySQLBinlogExtractor extractor;
    private String inputDir;
    private String outputDir;
    private long scanInterval;
    private AtomicBoolean running = new AtomicBoolean(true);

    private Map<String, FileProgress> fileProgressMap = new LinkedHashMap<>();
    private String progressRecordFile;

    public static void main(String[] args) {
        logger.info("Starting Continuous Extract Module...");

        try {
            Properties props = new Properties();
            InputStream input = ContinuousExtractMain.class.getClassLoader().getResourceAsStream("extract.properties");
            if (input == null) {
                throw new RuntimeException("extract.properties not found in classpath");
            }
            props.load(input);
            input.close();

            ContinuousExtractMain main = new ContinuousExtractMain();
            main.initialize(props);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received, stopping...");
                main.stop();
            }));

            main.start();

        } catch (Exception e) {
            logger.error("Fatal error in Continuous Extract Module", e);
            System.exit(1);
        }
    }

    public void initialize(Properties props) throws Exception {
        this.inputDir = props.getProperty("input.dir", "../output/binlog");
        this.outputDir = props.getProperty("output.dir", "../output/thl");
        this.scanInterval = Long.parseLong(props.getProperty("scan.interval", "5000"));
        this.progressRecordFile = outputDir + "/.extract_progress";

        this.extractor = new MySQLBinlogExtractor();
        this.extractor.initialize(props);

        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        loadProgress();

        logger.info("Continuous Extract initialized - input: {}, output: {}, scanInterval: {}ms",
            inputDir, outputDir, scanInterval);
    }

    public void start() {
        logger.info("Starting continuous binlog monitoring...");

        while (running.get()) {
            try {
                scanAndProcessFiles();
                Thread.sleep(scanInterval);
            } catch (InterruptedException e) {
                logger.info("Extract thread interrupted");
                break;
            } catch (Exception e) {
                logger.error("Error during file scanning", e);
            }
        }

        extractor.close();
        saveProgress();
        logger.info("Continuous Extract stopped");
    }

    public void stop() {
        running.set(false);
    }

    private void scanAndProcessFiles() throws Exception {
        File inputDirFile = new File(inputDir);

        if (!inputDirFile.exists()) {
            logger.debug("Input directory does not exist: {}", inputDir);
            return;
        }

        File[] binlogFiles = inputDirFile.listFiles((dir, name) ->
            name.startsWith("binlog-") && name.endsWith(".bin"));

        if (binlogFiles == null || binlogFiles.length == 0) {
            logger.debug("No binlog files found in directory: {}", inputDir);
            return;
        }

        Arrays.sort(binlogFiles, (a, b) -> {
            String nameA = a.getName().replace("binlog-", "").replace(".bin", "");
            String nameB = b.getName().replace("binlog-", "").replace(".bin", "");
            try {
                return Long.compare(Long.parseLong(nameA), Long.parseLong(nameB));
            } catch (NumberFormatException e) {
                return a.getName().compareTo(b.getName());
            }
        });

        for (File binlogFile : binlogFiles) {
            if (!running.get()) {
                break;
            }
            processFileIncremental(binlogFile);
        }
    }

    private void processFileIncremental(File binlogFile) throws Exception {
        FileProgress progress = fileProgressMap.get(binlogFile.getName());

        if (progress == null) {
            progress = new FileProgress(binlogFile.getName());
            fileProgressMap.put(binlogFile.getName(), progress);
            logger.info("Detected new binlog file: {} (size: {})", binlogFile.getName(), binlogFile.length());
        }

        if (progress.completed) {
            return;
        }

        int totalLinesInFile = countLines(binlogFile);

        if (totalLinesInFile <= progress.linesRead) {
            return;
        }

        int newLines = totalLinesInFile - progress.linesRead;
        logger.info("Processing binlog file: {} ({} new lines, already read: {}, total: {})",
            binlogFile.getName(), newLines, progress.linesRead, totalLinesInFile);

        String binlogTimestamp = binlogFile.getName()
            .replace("binlog-", "").replace(".bin", "");
        String outputFileName = "thl-" + binlogTimestamp + ".thl";
        File outputFile = new File(outputDir, outputFileName);

        int newEventCount = 0;

        if (progress.linesRead == 0) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
                newEventCount = readAndExtractNewLines(binlogFile, oos, progress, 0);
            }
        } else {
            try (AppendingObjectOutputStream oos = new AppendingObjectOutputStream(new FileOutputStream(outputFile, true))) {
                newEventCount = readAndExtractNewLines(binlogFile, oos, progress, progress.linesRead);
            }
        }

        long currentSize = binlogFile.length();
        boolean fileStoppedGrowing = (currentSize == progress.lastFileSize && currentSize > 0);
        progress.lastFileSize = currentSize;

        if (fileStoppedGrowing && newLines == newEventCount + (totalLinesInFile - progress.linesRead - newLines)) {
            progress.stableCheckCount++;
        } else {
            progress.stableCheckCount = 0;
        }

        if (progress.stableCheckCount >= 3) {
            progress.completed = true;
            logger.info("Binlog file {} appears complete (size stable), marking as completed", binlogFile.getName());
        }

        logger.info("Processed binlog file: {} -> {} new events (total lines read: {})",
            binlogFile.getName(), newEventCount, progress.linesRead);

        extractor.saveSeqno();
        saveProgress();
    }

    private int countLines(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        return count;
    }

    private int readAndExtractNewLines(File binlogFile, ObjectOutputStream oos, FileProgress progress, int skipLines) throws Exception {
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
                if (event != null) {
                    oos.writeObject(event);
                    eventCount++;
                }
                progress.linesRead++;
            }
        }
        return eventCount;
    }

    private void loadProgress() {
        File recordFile = new File(progressRecordFile);
        if (!recordFile.exists()) {
            logger.info("No extract progress record found, starting fresh");
            return;
        }

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
            logger.warn("Error loading extract progress record", e);
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
            logger.debug("Saved {} file progress records", fileProgressMap.size());
        } catch (IOException e) {
            logger.warn("Error saving extract progress record", e);
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

    private static class AppendingObjectOutputStream extends ObjectOutputStream {
        AppendingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }
    }
}
