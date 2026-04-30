package com.example.increment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContinuousIncrementMain {

    private static final Logger logger = LoggerFactory.getLogger(ContinuousIncrementMain.class);

    private THLToSqlConverter converter;
    private String inputDir;
    private String targetUrl;
    private long scanInterval;
    private AtomicBoolean running = new AtomicBoolean(true);

    private Map<String, FileProgress> fileProgressMap = new LinkedHashMap<>();
    private String progressRecordFile;

    public static void main(String[] args) {
        logger.info("Starting Continuous Increment Module...");

        try {
            Properties props = new Properties();
            InputStream input = ContinuousIncrementMain.class.getClassLoader().getResourceAsStream("increment.properties");
            if (input == null) {
                throw new RuntimeException("increment.properties not found in classpath");
            }
            props.load(input);
            input.close();

            ContinuousIncrementMain main = new ContinuousIncrementMain();
            main.initialize(props);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received, stopping...");
                main.stop();
            }));

            main.start();

        } catch (Exception e) {
            logger.error("Fatal error in Continuous Increment Module", e);
            System.exit(1);
        }
    }

    public void initialize(Properties props) throws Exception {
        this.inputDir = props.getProperty("input.dir", "../output/thl");
        this.targetUrl = props.getProperty("target.mysql.url");
        this.scanInterval = Long.parseLong(props.getProperty("scan.interval", "5000"));
        this.progressRecordFile = inputDir + "/.increment_progress";

        this.converter = new THLToSqlConverter(props);

        loadProgress();

        logger.info("Continuous Increment initialized - input: {}, target: {}, scanInterval: {}ms",
            inputDir, targetUrl, scanInterval);
        logger.info("Last executed seqno: {}", converter.getLastExecutedSeqno());
    }

    public void start() {
        logger.info("Starting continuous THL monitoring...");

        while (running.get()) {
            try {
                scanAndProcessFiles();
                Thread.sleep(scanInterval);
            } catch (InterruptedException e) {
                logger.info("Increment thread interrupted");
                break;
            } catch (THLToSqlConverter.SqlExecutionException e) {
                logger.error("SQL execution failed at seqno {}, cannot continue. Failed SQL: {}",
                    e.getSeqno(), e.getFailedSql());
                logger.error("Please fix the issue and restart. The process will now exit.");
                converter.disconnect();
                saveProgress();
                System.exit(2);
            } catch (Exception e) {
                logger.error("Error during file scanning", e);
            }
        }

        converter.disconnect();
        saveProgress();
        logger.info("Continuous Increment stopped");
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

        File[] thlFiles = inputDirFile.listFiles((dir, name) ->
            name.startsWith("thl-") && name.endsWith(".thl"));

        if (thlFiles == null || thlFiles.length == 0) {
            logger.debug("No THL files found in directory: {}", inputDir);
            return;
        }

        Arrays.sort(thlFiles, (a, b) -> {
            String nameA = a.getName().replace("thl-", "").replace(".thl", "");
            String nameB = b.getName().replace("thl-", "").replace(".thl", "");
            try {
                return Long.compare(Long.parseLong(nameA), Long.parseLong(nameB));
            } catch (NumberFormatException e) {
                return a.getName().compareTo(b.getName());
            }
        });

        for (File thlFile : thlFiles) {
            if (!running.get()) {
                break;
            }
            processFileIncremental(thlFile);
        }
    }

    private void processFileIncremental(File thlFile) throws Exception {
        FileProgress progress = fileProgressMap.get(thlFile.getName());

        if (progress == null) {
            progress = new FileProgress(thlFile.getName());
            fileProgressMap.put(thlFile.getName(), progress);
            logger.info("Detected new THL file: {}", thlFile.getName());
        }

        if (progress.completed) {
            return;
        }

        int totalEventsInFile = converter.countEventsInFile(thlFile);

        if (totalEventsInFile <= progress.eventsRead) {
            return;
        }

        int newEvents = totalEventsInFile - progress.eventsRead;
        logger.info("Processing THL file: {} ({} new events, already read: {}, total: {})",
            thlFile.getName(), newEvents, progress.eventsRead, totalEventsInFile);

        int processedCount = converter.processFileFromEvent(thlFile, progress.eventsRead);
        progress.eventsRead = totalEventsInFile;

        long currentSize = thlFile.length();
        boolean sizeStable = (currentSize == progress.lastFileSize && currentSize > 0);
        progress.lastFileSize = currentSize;

        if (sizeStable) {
            progress.stableCheckCount++;
        } else {
            progress.stableCheckCount = 0;
        }

        if (progress.stableCheckCount >= 3) {
            progress.completed = true;
            logger.info("THL file {} appears complete (size stable), marking as completed", thlFile.getName());
        }

        logger.info("Processed THL file: {} -> {} new events processed (total events read: {})",
            thlFile.getName(), processedCount, progress.eventsRead);

        saveProgress();
    }

    private void loadProgress() {
        File recordFile = new File(progressRecordFile);
        if (!recordFile.exists()) {
            logger.info("No increment progress record found, starting fresh");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(recordFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    FileProgress progress = new FileProgress(parts[0]);
                    progress.eventsRead = Integer.parseInt(parts[1]);
                    progress.lastFileSize = Long.parseLong(parts[2]);
                    progress.completed = Boolean.parseBoolean(parts[3]);
                    fileProgressMap.put(parts[0], progress);
                }
            }
            logger.info("Loaded {} file progress records", fileProgressMap.size());
        } catch (IOException e) {
            logger.warn("Error loading increment progress record", e);
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
                writer.write(progress.fileName + "|" + progress.eventsRead + "|" +
                    progress.lastFileSize + "|" + progress.completed);
                writer.newLine();
            }
            logger.debug("Saved {} file progress records", fileProgressMap.size());
        } catch (IOException e) {
            logger.warn("Error saving increment progress record", e);
        }
    }

    private static class FileProgress {
        String fileName;
        int eventsRead;
        long lastFileSize;
        int stableCheckCount;
        boolean completed;

        FileProgress(String fileName) {
            this.fileName = fileName;
            this.eventsRead = 0;
            this.lastFileSize = 0;
            this.stableCheckCount = 0;
            this.completed = false;
        }
    }
}
