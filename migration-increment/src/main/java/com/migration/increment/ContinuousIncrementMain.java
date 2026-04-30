package com.migration.increment;

import com.migration.increment.checkpoint.SeqnoCheckpointManager;
import com.migration.thl.THLEvent;
import com.migration.thl.THLFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContinuousIncrementMain {

    private static final Logger logger = LoggerFactory.getLogger(ContinuousIncrementMain.class);

    private String thlDirectory;
    private String targetHost;
    private int targetPort;
    private String targetDatabase;
    private String targetUser;
    private String targetPassword;
    private long scanInterval;
    private String taskId;
    private boolean isPostgresql;

    private Connection targetConnection;
    private THLToSqlConverter sqlConverter;
    private SeqnoCheckpointManager checkpointManager;
    private Properties props;

    private AtomicBoolean running = new AtomicBoolean(true);
    private Map<String, Long> processedFiles = new LinkedHashMap<>();
    private String progressFile;

    private long lastExecutedSeqno = 0;

    private volatile long lastRtoMs = -1;
    private volatile long lastRtoReportTime = 0;
    private static final long RTO_REPORT_INTERVAL_MS = 5000;
    private static final int RTO_REPORT_EVENT_INTERVAL = 100;

    public static void main(String[] args) {
        logger.info("=== MySQL Migration Increment (Continuous) Starting ===");

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

        try {
            ContinuousIncrementMain main = new ContinuousIncrementMain();
            main.initialize(props);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received, stopping increment...");
                main.stop();
            }));

            main.start();
        } catch (Exception e) {
            logger.error("Fatal error in Continuous Increment", e);
            System.exit(1);
        }
    }

    public void initialize(Properties props) throws Exception {
        this.props = props;
        taskId = props.getProperty("task.id", "unknown");
        thlDirectory = props.getProperty("increment.thl.dir",
                "files/" + taskId + "/thl_output");
        targetHost = props.getProperty("target.db.host", "localhost");
        targetPort = Integer.parseInt(props.getProperty("target.db.port", "3306"));
        targetDatabase = props.getProperty("target.db.database", "");
        targetUser = props.getProperty("target.db.username", "root");
        targetPassword = props.getProperty("target.db.password", "");
        scanInterval = Long.parseLong(props.getProperty("increment.scan.interval", "3000"));
        isPostgresql = "postgresql".equalsIgnoreCase(props.getProperty("target.db.type", "mysql"));

        String checkpointPath = "./files/" + taskId + "/checkpoint/increment_checkpoint";
        checkpointManager = new SeqnoCheckpointManager(checkpointPath);

        lastExecutedSeqno = checkpointManager.loadSeqno();

        connectToTargetDatabase();

        sqlConverter = new THLToSqlConverter(props);

        progressFile = "./files/" + taskId + "/checkpoint/.increment_progress";
        loadProgress();

        logger.info("Continuous Increment initialized - thlDir: {}, target: {}:{}/{}, lastSeqno: {}",
                thlDirectory, targetHost, targetPort, targetDatabase, lastExecutedSeqno);
    }

    private void connectToTargetDatabase() throws SQLException {
        String url;
        if (isPostgresql) {
            String jdbcUrl = props.getProperty("target.db.jdbc.url");
            if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
                url = jdbcUrl;
            } else {
                url = "jdbc:postgresql://" + targetHost + ":" + targetPort + "/" + targetDatabase;
            }
        } else {
            url = "jdbc:mysql://" + targetHost + ":" + targetPort + "/" + targetDatabase +
                    "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
        }
        targetConnection = DriverManager.getConnection(url, targetUser, targetPassword);
        logger.info("Connected to target database: {}:{}/{} (type: {})", targetHost, targetPort, targetDatabase,
                isPostgresql ? "postgresql" : "mysql");
    }

    public void start() {
        logger.info("Starting continuous THL processing...");

        while (running.get()) {
            try {
                scanAndProcessThlFiles();
                Thread.sleep(scanInterval);
            } catch (InterruptedException e) {
                logger.info("Increment thread interrupted");
                break;
            } catch (RuntimeException e) {
                logger.error("Fatal error during THL file processing, shutting down", e);
                throw e;
            } catch (Exception e) {
                logger.error("Error during THL file scanning", e);
            }
        }

        close();
        logger.info("Continuous Increment stopped");
    }

    public void stop() {
        running.set(false);
    }

    private void scanAndProcessThlFiles() {
        File thlDir = new File(thlDirectory);
        if (!thlDir.exists() || !thlDir.isDirectory()) {
            return;
        }

        File[] thlFiles = thlDir.listFiles((dir, name) ->
                name.endsWith(".thl") && !name.startsWith("."));

        if (thlFiles == null || thlFiles.length == 0) {
            return;
        }

        Arrays.sort(thlFiles, Comparator.comparing(File::getName));

        for (File thlFile : thlFiles) {
            if (!running.get()) break;
            processThlFile(thlFile);
        }
    }

    private void processThlFile(File thlFile) {
        String fileName = thlFile.getName();
        Long lastProcessedSeqno = processedFiles.get(fileName);

        if (lastProcessedSeqno != null && lastProcessedSeqno == -1) {
            return;
        }

        logger.info("Processing THL file: {}", fileName);
        int eventCount = 0;

        try (THLFileReader reader = new THLFileReader(thlFile.getAbsolutePath())) {
            THLEvent event;
            while ((event = reader.readEvent()) != null) {
                if (!running.get()) break;

                if (event.getSeqno() <= lastExecutedSeqno) {
                    continue;
                }

                if (event.getType() == THLEvent.HEARTBEAT_EVENT) {
                    lastExecutedSeqno = event.getSeqno();

                    String binlogFile = (String) event.getMetadata("binlog_file");
                    Long binlogPosition = (Long) event.getMetadata("binlog_position");
                    checkpointManager.saveCheckpoint(
                            event.getSeqno(),
                            binlogFile != null ? binlogFile : "",
                            binlogPosition != null ? binlogPosition : 0,
                            event.getEventId() != null ? event.getEventId() : ""
                    );

                    if (event.getSourceTstamp() != null) {
                        long rtoMs = System.currentTimeMillis() - event.getSourceTstamp().getTime();
                        if (rtoMs >= 0) {
                            lastRtoMs = rtoMs;
                            lastRtoReportTime = System.currentTimeMillis();
                            writeRtoMetric(rtoMs);
                            logger.debug("Heartbeat RTO: {}ms (seqno={})", rtoMs, event.getSeqno());
                        }
                    }

                    continue;
                }

                List<String> sqlStatements = sqlConverter.convertToSql(event);

                if (sqlStatements != null && !sqlStatements.isEmpty()) {
                    logger.info("Generated {} SQL statements for seqno={}", sqlStatements.size(), event.getSeqno());
                }

                for (String sql : sqlStatements) {
                    try {
                        logger.info("Executing SQL (seqno={}): {}", event.getSeqno(), sql.substring(0, Math.min(300, sql.length())));
                        executeSql(sql);
                        eventCount++;
                    } catch (SQLException e) {
                        String sqlUpper = sql.toUpperCase().trim();
                        String errorMsg = e.getMessage();
                        boolean isRecoverable = false;

                        if (errorMsg != null && (errorMsg.contains("Duplicate entry") || errorMsg.contains("1062"))) {
                            isRecoverable = true;
                            logger.warn("Duplicate entry ignored (seqno={}): {}", event.getSeqno(), errorMsg);
                        } else if ((sqlUpper.startsWith("UPDATE") || sqlUpper.startsWith("DELETE"))
                                && errorMsg != null
                                && (errorMsg.contains("0 rows affected") || errorMsg.contains("not found"))) {
                            isRecoverable = true;
                            logger.warn("No rows affected for UPDATE/DELETE, ignoring (seqno={}): {}", event.getSeqno(), errorMsg);
                        }

                        if (!isRecoverable) {
                            logger.error("Failed to execute SQL (seqno={}): {} - Error: {}", event.getSeqno(), sql.substring(0, Math.min(200, sql.length())), errorMsg);
                            throw new RuntimeException("SQL execution failed (seqno=" + event.getSeqno() + "): " + errorMsg, e);
                        }
                    }
                }

                lastExecutedSeqno = event.getSeqno();

                String binlogFile = (String) event.getMetadata("binlog_file");
                Long binlogPosition = (Long) event.getMetadata("binlog_position");
                checkpointManager.saveCheckpoint(
                        event.getSeqno(),
                        binlogFile != null ? binlogFile : "",
                        binlogPosition != null ? binlogPosition : 0,
                        event.getEventId() != null ? event.getEventId() : ""
                );

                if (event.getSourceTstamp() != null) {
                    long now = System.currentTimeMillis();
                    long rtoMs = now - event.getSourceTstamp().getTime();
                    if (rtoMs >= 0 && (eventCount % RTO_REPORT_EVENT_INTERVAL == 0 || now - lastRtoReportTime > RTO_REPORT_INTERVAL_MS)) {
                        lastRtoMs = rtoMs;
                        lastRtoReportTime = now;
                        writeRtoMetric(rtoMs);
                    }
                }

                if (eventCount % 100 == 0) {
                    logger.info("Processed {} events, last seqno: {}", eventCount, lastExecutedSeqno);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing THL file: {}", fileName, e);
            throw new RuntimeException("Fatal error processing THL file: " + fileName, e);
        }

        if (eventCount > 0) {
            logger.info("Processed THL file: {} -> {} events executed, last seqno: {}", fileName, eventCount, lastExecutedSeqno);
        }

        processedFiles.put(fileName, -1L);
        saveProgress();
    }

    private void executeSql(String sql) throws SQLException {
        if (sql == null || sql.trim().isEmpty()) return;

        String trimmedSql = sql.trim();

        if (isPostgresql) {
            if (trimmedSql.equalsIgnoreCase("COMMIT;") || trimmedSql.equalsIgnoreCase("COMMIT")) {
                if (!targetConnection.getAutoCommit()) {
                    targetConnection.commit();
                }
                return;
            }
            if (trimmedSql.toUpperCase().startsWith("SET SEARCH_PATH") ||
                trimmedSql.toUpperCase().startsWith("SET SEARCH_PATH")) {
                try (Statement stmt = targetConnection.createStatement()) {
                    stmt.execute(trimmedSql);
                }
                return;
            }
        }

        try (Statement stmt = targetConnection.createStatement()) {
            boolean hasResultSet = stmt.execute(trimmedSql);
            int updateCount = stmt.getUpdateCount();
            logger.info("SQL executed: autoCommit={}, hasResultSet={}, updateCount={}", 
                targetConnection.getAutoCommit(), hasResultSet, updateCount);
        }
    }

    private void loadProgress() {
        File file = new File(progressFile);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    processedFiles.put(parts[0], Long.parseLong(parts[1]));
                }
            }
        } catch (Exception e) {
            logger.warn("Error loading increment progress", e);
        }
    }

    private void saveProgress() {
        File file = new File(progressFile);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Long> entry : processedFiles.entrySet()) {
                writer.write(entry.getKey() + "|" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            logger.warn("Error saving increment progress", e);
        }
    }

    private void writeRtoMetric(long rtoMs) {
        String metricsDir = "./files/" + taskId + "/binlog_output";
        File dir = new File(metricsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File metricFile = new File(dir, "rto_metric");
        try (PrintWriter pw = new PrintWriter(new FileWriter(metricFile, false))) {
            pw.println(System.currentTimeMillis() + "|" + rtoMs);
        } catch (IOException e) {
            logger.warn("Failed to write RTO metric: {}", e.getMessage());
        }
    }

    public long getLastRtoMs() {
        return lastRtoMs;
    }

    public void close() {
        saveProgress();
        checkpointManager.close();
        if (targetConnection != null) {
            try {
                targetConnection.close();
            } catch (SQLException e) {
                logger.error("Error closing target connection", e);
            }
        }
    }
}
