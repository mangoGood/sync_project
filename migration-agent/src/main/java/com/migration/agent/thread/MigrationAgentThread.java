package com.migration.agent.thread;

import com.migration.agent.checkpoint.CheckpointManager;
import com.migration.agent.checkpoint.CheckpointManager.BinlogPositionInfo;
import com.migration.agent.manager.ProcessManager;
import com.migration.agent.model.TaskMessage;
import com.migration.agent.model.TaskStateInfo;
import com.migration.agent.model.TaskStatusMessage;
import com.migration.agent.service.KafkaProducerService;
import com.migration.agent.service.TaskStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.migration.agent.util.SyncErrorCodeMapper;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MigrationAgentThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MigrationAgentThread.class);

    private static final String CAPTURE_JAR_PATH = "migration-capture/target/migration-capture-1.0.0.jar";
    private static final String MIGRATION_FULL_JAR_PATH = "migration-full/target/migration-full-1.0.0.jar";
    private static final String EXTRACT_JAR_PATH = "migration-extract/target/migration-extract-1.0.0.jar";
    private static final String INCREMENT_JAR_PATH = "migration-increment/target/migration-increment-1.0.0.jar";

    private static final long CAPTURE_MONITOR_INTERVAL = 30000;
    private static final long INCREMENT_MONITOR_INTERVAL = 10000;
    private static final long PROGRESS_MONITOR_INTERVAL = 3000;

    private final TaskMessage taskMessage;
    private final KafkaProducerService kafkaProducer;
    private final TaskStateService taskStateService;
    private final String taskId;
    private final AtomicBoolean running;
    private final AtomicBoolean stopped;
    private final boolean skipFullMigration;
    private final String migrationMode;
    private final String sourceType;
    private final String targetType;

    private ProcessManager captureProcess;
    private ProcessManager fullProcess;
    private ProcessManager extractProcess;
    private ProcessManager incrementProcess;

    private Thread captureMonitorThread;
    private Thread incrementMonitorThread;
    private Thread fullMigrationMonitorThread;

    private int totalTables = 0;
    private volatile int completedTables = 0;
    private volatile String lastSuccessfulStatus = null;

    public MigrationAgentThread(TaskMessage taskMessage, KafkaProducerService kafkaProducer) {
        this(taskMessage, kafkaProducer, null, false);
    }

    public MigrationAgentThread(TaskMessage taskMessage, KafkaProducerService kafkaProducer, boolean skipFullMigration) {
        this(taskMessage, kafkaProducer, null, skipFullMigration);
    }

    public MigrationAgentThread(TaskMessage taskMessage, KafkaProducerService kafkaProducer, TaskStateService taskStateService, boolean skipFullMigration) {
        this.taskMessage = taskMessage;
        this.kafkaProducer = kafkaProducer;
        this.taskStateService = taskStateService;
        this.taskId = taskMessage.getTaskId();
        this.running = new AtomicBoolean(true);
        this.stopped = new AtomicBoolean(false);
        this.skipFullMigration = skipFullMigration;
        this.migrationMode = taskMessage.getMigrationMode();
        this.sourceType = taskMessage.getSourceType() != null ? taskMessage.getSourceType() : "mysql";
        this.targetType = taskMessage.getTargetType() != null ? taskMessage.getTargetType() : "mysql";

        this.totalTables = calculateTotalTables(taskMessage.getSyncObjects());
    }

    private int calculateTotalTables(Map<String, Object> syncObjects) {
        if (syncObjects == null || syncObjects.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Map.Entry<String, Object> entry : syncObjects.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof java.util.List) {
                count += ((java.util.List<?>) value).size();
            } else if (value instanceof Map) {
                Map<?, ?> dbValue = (Map<?, ?>) value;
                Object tablesObj = dbValue.get("tables");
                if (tablesObj instanceof java.util.List) {
                    count += ((java.util.List<?>) tablesObj).size();
                }
            }
        }
        return count;
    }

    @Override
    public void run() {
        String threadName = "MigrationAgentThread-" + taskId;
        Thread.currentThread().setName(threadName);
        logger.info("[{}] 开始执行同步任务, skipFullMigration={}, migrationMode={}, sourceType={}, targetType={}", 
            threadName, skipFullMigration, migrationMode, sourceType, targetType);

        try {
            if (skipFullMigration) {
                Thread.interrupted();
                lastSuccessfulStatus = "INCREMENT_RUNNING";
                sendStatus("INCREMENT_RUNNING", "从增量同步阶段恢复", 100);

                if (!startCaptureProcess()) {
                    return;
                }

                if (!startExtractProcess()) {
                    return;
                }

                if (!startIncrementProcess()) {
                    return;
                }

                logger.info("[{}] 增量同步任务恢复完成，进入持续监控模式", threadName);
            } else {
                sendStatus("STARTING", "任务启动中", 0, totalTables, 0, null, 0, 0L, 0L);

                if (!initCheckpoint()) {
                    return;
                }

                if (!startCaptureProcess()) {
                    return;
                }

                if (!executeFullMigration()) {
                    return;
                }

                boolean isFullOnly = !"fullAndIncre".equals(migrationMode);
                if (isFullOnly) {
                    logger.info("[{}] 仅全量同步任务完成，状态: FULL_COMPLETED", threadName);
                    stopAllProcesses();
                    sendStatus("FULL_COMPLETED", "全量同步完成", 100, totalTables, totalTables, null, 100, 0L, 0L);
                    return;
                }

                lastSuccessfulStatus = "FULL_COMPLETED";

                if (!startExtractProcess()) {
                    return;
                }

                if (!startIncrementProcess()) {
                    return;
                }

                lastSuccessfulStatus = "INCREMENT_RUNNING";

                logger.info("[{}] 增量同步任务启动完成，进入持续监控模式", threadName);
            }

            while (running.get() && !stopped.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

        } catch (Exception e) {
            logger.error("[{}] 同步任务执行异常", threadName, e);
            saveTaskStateOnFailure();
            String errorCode = SyncErrorCodeMapper.mapExceptionToErrorCode(e, "agent");
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            sendFailedStatus(errorCode, "任务执行异常: " + errorMsg);
        } finally {
            stopAllProcesses();
            logger.info("[{}] 同步任务线程结束", threadName);
        }
    }

    private boolean initCheckpoint() {
        String threadName = "MigrationAgentThread-" + taskId;
        logger.info("[{}] 初始化 checkpoint, sourceType={}", threadName, sourceType);

        if ("postgresql".equals(sourceType)) {
            return initPostgresCheckpoint(threadName);
        } else {
            return initMysqlCheckpoint(threadName);
        }
    }

    private boolean initMysqlCheckpoint(String threadName) {

        String checkpointDbPath = "./files/" + taskId + "/checkpoint/checkpoint";
        CheckpointManager checkpointManager = null;

        try {
            checkpointManager = new CheckpointManager(checkpointDbPath);

            BinlogPositionInfo existingCheckpoint = checkpointManager.loadCheckpoint();

            BinlogPositionInfo checkpointToUse = existingCheckpoint;

            if (existingCheckpoint != null) {
                logger.info("[{}] 发现已存在的 checkpoint: {}", threadName, existingCheckpoint);
            } else {
                logger.info("[{}] 未找到 checkpoint，从源数据库获取当前位点", threadName);

                String sourceHost = null;
                int sourcePort = 3306;
                String sourceUser = null;
                String sourcePassword = null;

                TaskMessage.DatabaseConfig sourceConfig = taskMessage.getSource();
                if (sourceConfig != null) {
                    sourceHost = sourceConfig.getHost();
                    sourcePort = sourceConfig.getPort();
                    sourceUser = sourceConfig.getUsername();
                    sourcePassword = sourceConfig.getPassword();
                    logger.info("[{}] 从 taskMessage.getSource() 获取源数据库配置: {}:{}", threadName, sourceHost, sourcePort);
                }

                if (sourceHost == null && taskMessage.getSourceConnection() != null) {
                    try {
                        com.migration.agent.util.ConnectionStringParser.ConnectionInfo sourceInfo =
                            com.migration.agent.util.ConnectionStringParser.parse(taskMessage.getSourceConnection());
                        if (sourceInfo != null) {
                            sourceHost = sourceInfo.getHost();
                            sourcePort = sourceInfo.getPort();
                            sourceUser = sourceInfo.getUsername();
                            sourcePassword = sourceInfo.getPassword();
                            logger.info("[{}] 从 taskMessage.getSourceConnection() 获取源数据库配置: {}:{}", threadName, sourceHost, sourcePort);
                        }
                    } catch (Exception e) {
                        logger.warn("[{}] 解析 sourceConnection 失败: {}", threadName, e.getMessage());
                    }
                }

                if (sourceHost == null) {
                    try {
                        java.util.Properties props = new java.util.Properties();
                        try (java.io.InputStream input = new java.io.FileInputStream("./files/" + taskId + "/config.properties")) {
                            props.load(input);
                        }
                        sourceHost = props.getProperty("source.db.host");
                        sourcePort = Integer.parseInt(props.getProperty("source.db.port", "3306"));
                        sourceUser = props.getProperty("source.db.username");
                        sourcePassword = props.getProperty("source.db.password");
                        logger.info("[{}] 从配置文件获取源数据库配置: {}:{}", threadName, sourceHost, sourcePort);
                    } catch (Exception e) {
                        logger.error("[{}] 读取配置文件失败: {}", threadName, e.getMessage());
                    }
                }

                if (sourceHost == null || sourceUser == null) {
                    logger.error("[{}] 源数据库配置为空", threadName);
                    sendStatus("FAILED", "源数据库配置为空", 0);
                    return false;
                }

                BinlogPositionInfo currentPosition = checkpointManager.getCurrentPositionFromSource(
                    sourceHost, sourcePort, sourceUser, sourcePassword
                );

                checkpointManager.saveCheckpoint(currentPosition);
                checkpointToUse = currentPosition;
                logger.info("[{}] 已记录当前位点作为 checkpoint: {}", threadName, currentPosition);
            }

            updateCheckpointConfig(checkpointToUse);

            return true;

        } catch (Exception e) {
            logger.error("[{}] 初始化 checkpoint 失败", threadName, e);
            sendStatus("FAILED", "初始化 checkpoint 失败: " + e.getMessage(), 0);
            return false;
        } finally {
            if (checkpointManager != null) {
                checkpointManager.close();
            }
        }
    }

    private boolean initPostgresCheckpoint(String threadName) {
        String checkpointDbPath = "./files/" + taskId + "/checkpoint/checkpoint";
        CheckpointManager checkpointManager = null;

        try {
            checkpointManager = new CheckpointManager(checkpointDbPath);

            BinlogPositionInfo existingCheckpoint = checkpointManager.loadCheckpoint();

            BinlogPositionInfo checkpointToUse = existingCheckpoint;

            if (existingCheckpoint != null) {
                logger.info("[{}] 发现已存在的 PostgreSQL checkpoint: {}", threadName, existingCheckpoint);
            } else {
                logger.info("[{}] 未找到 checkpoint，从 PostgreSQL 源数据库获取当前 WAL LSN", threadName);

                String sourceHost = null;
                int sourcePort = 5432;
                String sourceUser = null;
                String sourcePassword = null;

                TaskMessage.DatabaseConfig sourceConfig = taskMessage.getSource();
                if (sourceConfig != null) {
                    sourceHost = sourceConfig.getHost();
                    sourcePort = sourceConfig.getPort();
                    sourceUser = sourceConfig.getUsername();
                    sourcePassword = sourceConfig.getPassword();
                    logger.info("[{}] 从 taskMessage.getSource() 获取源数据库配置: {}:{}", threadName, sourceHost, sourcePort);
                }

                if (sourceHost == null && taskMessage.getSourceConnection() != null) {
                    try {
                        com.migration.agent.util.ConnectionStringParser.ConnectionInfo sourceInfo =
                            com.migration.agent.util.ConnectionStringParser.parse(taskMessage.getSourceConnection());
                        if (sourceInfo != null) {
                            sourceHost = sourceInfo.getHost();
                            sourcePort = sourceInfo.getPort();
                            sourceUser = sourceInfo.getUsername();
                            sourcePassword = sourceInfo.getPassword();
                            logger.info("[{}] 从 taskMessage.getSourceConnection() 获取源数据库配置: {}:{}", threadName, sourceHost, sourcePort);
                        }
                    } catch (Exception e) {
                        logger.warn("[{}] 解析 sourceConnection 失败: {}", threadName, e.getMessage());
                    }
                }

                if (sourceHost == null) {
                    try {
                        java.util.Properties props = new java.util.Properties();
                        try (java.io.InputStream input = new java.io.FileInputStream("./files/" + taskId + "/config.properties")) {
                            props.load(input);
                        }
                        sourceHost = props.getProperty("source.db.host");
                        sourcePort = Integer.parseInt(props.getProperty("source.db.port", "5432"));
                        sourceUser = props.getProperty("source.db.username");
                        sourcePassword = props.getProperty("source.db.password");
                        logger.info("[{}] 从配置文件获取源数据库配置: {}:{}", threadName, sourceHost, sourcePort);
                    } catch (Exception e) {
                        logger.error("[{}] 读取配置文件失败: {}", threadName, e.getMessage());
                    }
                }

                if (sourceHost == null || sourceUser == null) {
                    logger.error("[{}] 源数据库配置为空", threadName);
                    sendStatus("FAILED", "源数据库配置为空", 0);
                    return false;
                }

                BinlogPositionInfo currentPosition = checkpointManager.getCurrentPositionFromPostgres(
                    sourceHost, sourcePort, sourceUser, sourcePassword
                );

                checkpointManager.saveCheckpoint(currentPosition);
                checkpointToUse = currentPosition;
                logger.info("[{}] 已记录当前 PostgreSQL WAL LSN 作为 checkpoint: {}", threadName, currentPosition);
            }

            updateCheckpointConfig(checkpointToUse);

            return true;

        } catch (Exception e) {
            logger.error("[{}] 初始化 PostgreSQL checkpoint 失败", threadName, e);
            sendStatus("FAILED", "初始化 PostgreSQL checkpoint 失败: " + e.getMessage(), 0);
            return false;
        } finally {
            if (checkpointManager != null) {
                checkpointManager.close();
            }
        }
    }

    private void updateCheckpointConfig(BinlogPositionInfo checkpoint) {
        if (checkpoint == null) {
            return;
        }

        if ("postgresql".equals(sourceType)) {
            updatePostgresCheckpointConfig(checkpoint);
        } else {
            updateMysqlCheckpointConfig(checkpoint);
        }
    }

    private void updateMysqlCheckpointConfig(BinlogPositionInfo checkpoint) {
        if (checkpoint.getFilename() == null || checkpoint.getFilename().isEmpty()) {
            return;
        }

        String threadName = "MigrationAgentThread-" + taskId;
        java.util.Properties props = new java.util.Properties();
        File configFile = new File("./files/" + taskId + "/config.properties");

        try {
            if (configFile.exists()) {
                try (java.io.InputStream input = new java.io.FileInputStream(configFile)) {
                    props.load(input);
                }
            }

            props.setProperty("checkpoint.binlog.file", checkpoint.getFilename());
            props.setProperty("checkpoint.binlog.position", String.valueOf(checkpoint.getPosition()));
            props.setProperty("capture.binlog.file", checkpoint.getFilename());
            props.setProperty("capture.binlog.position", String.valueOf(checkpoint.getPosition()));
            props.setProperty("extract.skip.before.checkpoint", "true");

            try (java.io.OutputStream output = new java.io.FileOutputStream(configFile)) {
                props.store(output, "Updated checkpoint by MigrationAgentThread for task: " + taskId);
            }

            logger.info("[{}] Checkpoint 位点已写入配置文件: {}:{}", threadName, checkpoint.getFilename(), checkpoint.getPosition());
        } catch (Exception e) {
            logger.error("[{}] 写入 checkpoint 配置失败", threadName, e);
        }
    }

    private void updatePostgresCheckpointConfig(BinlogPositionInfo checkpoint) {
        String threadName = "MigrationAgentThread-" + taskId;
        java.util.Properties props = new java.util.Properties();
        File configFile = new File("./files/" + taskId + "/config.properties");

        try {
            if (configFile.exists()) {
                try (java.io.InputStream input = new java.io.FileInputStream(configFile)) {
                    props.load(input);
                }
            }

            if (checkpoint.getFilename() != null) {
                props.setProperty("checkpoint.wal.lsn", checkpoint.getFilename());
                props.setProperty("capture.wal.lsn", checkpoint.getFilename());
            }
            props.setProperty("checkpoint.wal.position", String.valueOf(checkpoint.getPosition()));
            props.setProperty("capture.wal.position", String.valueOf(checkpoint.getPosition()));
            props.setProperty("extract.skip.before.checkpoint", "true");

            try (java.io.OutputStream output = new java.io.FileOutputStream(configFile)) {
                props.store(output, "Updated PostgreSQL checkpoint by MigrationAgentThread for task: " + taskId);
            }

            logger.info("[{}] PostgreSQL Checkpoint 位点已写入配置文件: LSN={}", threadName, checkpoint.getFilename());
        } catch (Exception e) {
            logger.error("[{}] 写入 PostgreSQL checkpoint 配置失败", threadName, e);
        }
    }

    private boolean startCaptureProcess() {
        String threadName = "MigrationAgentThread-" + taskId;
        String captureType = "postgresql".equals(sourceType) ? "WAL" : "binlog";
        logger.info("[{}] 启动 capture 进程（拉取 {}）, sourceType={}", threadName, captureType, sourceType);

        try {
            captureProcess = new ProcessManager(CAPTURE_JAR_PATH, "CaptureMain-" + taskId);
            captureProcess.setTaskId(taskId);
            captureProcess.start();
            logger.info("[{}] capture 进程已提交启动，等待初始化...", threadName);

            for (int i = 0; i < 6; i++) {
                try { Thread.sleep(5000); } catch (InterruptedException e) {
                    logger.warn("[{}] capture 等待被中断 (attempt {}), stopped={}", threadName, i+1, stopped.get());
                    Thread.interrupted();
                    if (stopped.get()) {
                        logger.info("[{}] capture 等待中止，任务已停止", threadName);
                        return false;
                    }
                }

                if (captureProcess.isRunning()) {
                    logger.info("[{}] capture 进程启动成功 (after {}s)", threadName, (i+1)*5);
                    break;
                }
                logger.info("[{}] capture 进程未就绪，继续等待... ({}s elapsed)", threadName, (i+1)*5);
            }

            if (!captureProcess.isRunning()) {
                logger.error("[{}] capture 进程启动失败 (waited 30s)", threadName);
                sendStatus("FAILED", "capture 进程启动失败", 0);
                return false;
            }

            captureMonitorThread = new Thread(() -> {
                while (running.get() && captureProcess != null) {
                    try {
                        Thread.sleep(CAPTURE_MONITOR_INTERVAL);

                        if (!running.get() || stopped.get()) {
                            break;
                        }

                        if (!captureProcess.isRunning()) {
                            if (stopped.get()) {
                                logger.info("[{}] capture 进程已停止（暂停）", threadName);
                                break;
                            }
                            logger.error("[{}] capture 进程异常退出", threadName);
                            sendStatus("FAILED", "capture 进程异常退出", 0);
                            running.set(false);
                            break;
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.error("[{}] capture 监控异常", threadName, e);
                    }
                }
            }, "CaptureMonitor-" + taskId);
            captureMonitorThread.setDaemon(true);
            captureMonitorThread.start();

            return true;

        } catch (Exception e) {
            logger.error("[{}] 启动 capture 进程失败", threadName, e);
            sendStatus("FAILED", "启动 capture 进程失败: " + e.getMessage(), 0);
            return false;
        }
    }

    private boolean startExtractProcess() {
        String threadName = "MigrationAgentThread-" + taskId;
        logger.info("[{}] 启动 extract 进程（解析 {} 生成 THL）", threadName, "postgresql".equals(sourceType) ? "WAL" : "binlog");

        try {
            extractProcess = new ProcessManager(EXTRACT_JAR_PATH, "ContinuousExtractMain-" + taskId);
            extractProcess.setTaskId(taskId);
            extractProcess.start();
            logger.info("[{}] extract 进程已提交启动，等待初始化...", threadName);

            for (int i = 0; i < 6; i++) {
                try { Thread.sleep(5000); } catch (InterruptedException e) {
                    logger.warn("[{}] extract 等待被中断 (attempt {}), stopped={}", threadName, i+1, stopped.get());
                    Thread.interrupted();
                    if (stopped.get()) {
                        logger.info("[{}] extract 等待中止，任务已停止", threadName);
                        return false;
                    }
                }

                if (extractProcess.isRunning()) {
                    logger.info("[{}] extract 进程启动成功 (after {}s)", threadName, (i+1)*5);
                    return true;
                }
                logger.info("[{}] extract 进程未就绪，继续等待... ({}s elapsed)", threadName, (i+1)*5);
            }

            logger.error("[{}] extract 进程启动失败 (waited 30s)", threadName);
            sendStatus("FAILED", "extract 进程启动失败", 0);
            return false;

        } catch (Exception e) {
            logger.error("[{}] 启动 extract 进程失败", threadName, e);
            sendStatus("FAILED", "启动 extract 进程失败: " + e.getMessage(), 0);
            return false;
        }
    }

    private boolean executeFullMigration() {
        String threadName = "MigrationAgentThread-" + taskId;
        logger.info("[{}] 开始执行全量迁移", threadName);

        try {
            fullProcess = new ProcessManager(MIGRATION_FULL_JAR_PATH, "MigrationFull-" + taskId);
            fullProcess.setTaskId(taskId);
            fullProcess.start();

            sendStatus("FULL_MIGRATING", "全量同步中", 0, 0, 0, null, 0, 0L, 0L);

            startFullMigrationMonitor();

            int exitCode = fullProcess.waitFor();

            if (fullMigrationMonitorThread != null) {
                fullMigrationMonitorThread.interrupt();
            }

            if (stopped.get()) {
                logger.info("[{}] 全量迁移被暂停", threadName);
                return false;
            }

            if (exitCode == 0) {
                logger.info("[{}] 全量迁移完成", threadName);
                sendStatus("FULL_COMPLETED", "全量同步完成", 100, totalTables, totalTables, null, 100, 0L, 0L);
                return true;
            } else {
                logger.error("[{}] 全量迁移失败，退出码: {}", threadName, exitCode);
                sendStatus("FAILED", "全量迁移失败，退出码: " + exitCode, 0);
                running.set(false);
                return false;
            }

        } catch (Exception e) {
            if (stopped.get()) {
                logger.info("[{}] 全量迁移被暂停（异常捕获）", threadName);
                return false;
            }
            logger.error("[{}] 全量迁移执行异常", threadName, e);
            sendStatus("FAILED", "全量迁移执行异常: " + e.getMessage(), 0);
            running.set(false);
            return false;
        }
    }

    private void startFullMigrationMonitor() {
        fullMigrationMonitorThread = new Thread(() -> {
            String progressDbUrl = "jdbc:h2:./files/" + taskId + "/migration_progress;MODE=MySQL;AUTO_SERVER=TRUE";

            while (running.get() && fullProcess != null && fullProcess.isRunning()) {
                try {
                    Thread.sleep(PROGRESS_MONITOR_INTERVAL);

                    if (!running.get() || stopped.get()) {
                        break;
                    }

                    try (Connection conn = DriverManager.getConnection(progressDbUrl, "sa", "")) {
                        String completedSql = "SELECT COUNT(*) FROM migration_progress WHERE status = 'COMPLETED'";
                        try (Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery(completedSql)) {
                            if (rs.next()) {
                                completedTables = rs.getInt(1);
                            }
                        }

                        String currentTableSql = "SELECT table_name, total_rows, migrated_rows, status FROM migration_progress WHERE status = 'IN_PROGRESS' LIMIT 1";
                        String currentTable = null;
                        long currentTableRows = 0;
                        long currentTableTotalRows = 0;
                        int currentTableProgress = 0;

                        try (Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery(currentTableSql)) {
                            if (rs.next()) {
                                currentTable = rs.getString("table_name");
                                currentTableTotalRows = rs.getLong("total_rows");
                                currentTableRows = rs.getLong("migrated_rows");
                                if (currentTableTotalRows > 0) {
                                    currentTableProgress = (int) ((currentTableRows * 100) / currentTableTotalRows);
                                }
                            }
                        }

                        int overallProgress = 0;
                        if (totalTables > 0) {
                            overallProgress = (completedTables * 100) / totalTables;
                        }

                        sendStatus("FULL_MIGRATING", "全量同步中", overallProgress,
                            totalTables, completedTables, currentTable, currentTableProgress,
                            currentTableRows, currentTableTotalRows);

                    } catch (SQLException e) {
                        logger.debug("[{}] 读取迁移进度失败: {}", taskId, e.getMessage());
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("[{}] 全量迁移监控异常", taskId, e);
                }
            }
        }, "FullMigrationMonitor-" + taskId);
        fullMigrationMonitorThread.setDaemon(true);
        fullMigrationMonitorThread.start();
    }

    private boolean startIncrementProcess() {
        String threadName = "MigrationAgentThread-" + taskId;
        logger.info("[{}] 启动增量同步进程", threadName);

        try {
            incrementProcess = new ProcessManager(INCREMENT_JAR_PATH, "ContinuousIncrementMain-" + taskId);
            incrementProcess.setTaskId(taskId);
            incrementProcess.start();
            logger.info("[{}] increment 进程已提交启动，等待初始化...", threadName);

            for (int i = 0; i < 6; i++) {
                try { Thread.sleep(5000); } catch (InterruptedException e) {
                    logger.warn("[{}] increment 等待被中断 (attempt {}), stopped={}", threadName, i+1, stopped.get());
                    Thread.interrupted();
                    if (stopped.get()) {
                        logger.info("[{}] increment 等待中止，任务已停止", threadName);
                        return false;
                    }
                }

                if (incrementProcess.isRunning()) {
                    logger.info("[{}] increment 进程启动成功 (after {}s)", threadName, (i+1)*5);
                    break;
                }
                logger.info("[{}] increment 进程未就绪，继续等待... ({}s elapsed)", threadName, (i+1)*5);
            }

            if (!incrementProcess.isRunning()) {
                logger.error("[{}] 增量同步进程启动失败 (waited 30s)", threadName);
                sendStatus("FAILED", "增量同步进程启动失败", 100);
                return false;
            }

            sendStatus("INCREMENT_RUNNING", "增量同步中", 100);

            incrementMonitorThread = new Thread(() -> {
                while (running.get() && incrementProcess != null) {
                    try {
                        Thread.sleep(INCREMENT_MONITOR_INTERVAL);

                        if (!running.get() || stopped.get()) {
                            break;
                        }

                        if (!incrementProcess.isRunning()) {
                            if (stopped.get()) {
                                logger.info("[{}] 增量同步进程已停止（暂停）", threadName);
                                break;
                            }
                            logger.error("[{}] 增量同步进程异常退出", threadName);
                            saveTaskStateOnFailure();
                            sendStatus("FAILED", "增量同步进程异常退出", 100);
                            running.set(false);
                            break;
                        }

                        sendStatus("INCREMENT_RUNNING", "增量同步中", 100);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.error("[{}] 增量同步监控异常", threadName, e);
                    }
                }
            }, "IncrementMonitor-" + taskId);
            incrementMonitorThread.setDaemon(true);
            incrementMonitorThread.start();

            return true;

        } catch (Exception e) {
            logger.error("[{}] 启动增量同步进程失败", threadName, e);
            sendStatus("FAILED", "启动增量同步进程失败: " + e.getMessage(), 100);
            return false;
        }
    }

    private void saveTaskStateOnFailure() {
        if (taskStateService == null) {
            logger.warn("[{}] TaskStateService 未注入，无法保存任务状态到 H2", taskId);
            return;
        }

        try {
            String statusToSave = lastSuccessfulStatus;
            if (statusToSave == null) {
                statusToSave = "FAILED";
            }

            TaskStateInfo stateInfo = new TaskStateInfo(taskId);
            stateInfo.setTaskName(taskMessage.getTaskName());
            stateInfo.setUserId(taskMessage.getUserId());
            stateInfo.setMigrationMode(migrationMode);
            stateInfo.setSourceConnection(taskMessage.getSourceConnection());
            stateInfo.setTargetConnection(taskMessage.getTargetConnection());
            stateInfo.setSourceType(sourceType);
            stateInfo.setTargetType(targetType);
            stateInfo.setStatus(statusToSave);
            stateInfo.setProgress("INCREMENT_RUNNING".equals(statusToSave) || "FULL_COMPLETED".equals(statusToSave) ? 100 : 0);
            stateInfo.setCreatedAt(taskMessage.getCreatedAt() != null ? taskMessage.getCreatedAt() : java.time.LocalDateTime.now());

            taskStateService.saveTaskState(stateInfo);
            logger.info("[{}] 任务失败时状态已保存到 H2, statusToSave={}", taskId, statusToSave);
        } catch (Exception e) {
            logger.error("[{}] 保存任务状态到 H2 失败", taskId, e);
        }
    }

    public void stop() {
        stopped.set(true);
        running.set(false);

        stopAllProcesses();
    }

    private void stopAllProcesses() {
        String threadName = "MigrationAgentThread-" + taskId;
        logger.info("[{}] 停止所有进程", threadName);

        if (incrementProcess != null) {
            try {
                incrementProcess.stop();
                logger.info("[{}] 增量同步进程已停止", threadName);
            } catch (Exception e) {
                logger.error("[{}] 停止增量同步进程失败", threadName, e);
            }
        }

        if (extractProcess != null) {
            try {
                extractProcess.stop();
                logger.info("[{}] extract 进程已停止", threadName);
            } catch (Exception e) {
                logger.error("[{}] 停止 extract 进程失败", threadName, e);
            }
        }

        if (fullProcess != null) {
            try {
                fullProcess.stop();
                logger.info("[{}] 全量迁移进程已停止", threadName);
            } catch (Exception e) {
                logger.error("[{}] 停止全量迁移进程失败", threadName, e);
            }
        }

        if (captureProcess != null) {
            try {
                captureProcess.stop();
                logger.info("[{}] capture 进程已停止", threadName);
            } catch (Exception e) {
                logger.error("[{}] 停止 capture 进程失败", threadName, e);
            }
        }

        if (captureMonitorThread != null) {
            captureMonitorThread.interrupt();
        }

        if (incrementMonitorThread != null) {
            incrementMonitorThread.interrupt();
        }
    }

    public boolean isRunning() {
        return running.get() && !stopped.get();
    }

    public String getTaskId() {
        return taskId;
    }

    private void sendStatus(String status, String message, int progress) {
        sendStatus(status, message, progress, null, null, null, null, null, null);
    }

    private void sendFailedStatus(String errorCode, String message) {
        TaskStatusMessage statusMessage = new TaskStatusMessage();
        statusMessage.setTaskId(taskId);
        statusMessage.setStatus("FAILED");
        statusMessage.setMessage(message);
        statusMessage.setProgress(0);
        statusMessage.setErrorCode(errorCode);
        logger.info("[MigrationAgentThread-{}] FAILED status with errorCode={}, message={}", taskId, errorCode, message);
        kafkaProducer.sendStatus(statusMessage);
    }

    private void sendStatus(String status, String message, int progress,
            Integer totalTables, Integer completedTables, String currentTable,
            Integer currentTableProgress, Long currentTableRows, Long currentTableTotalRows) {
        TaskStatusMessage statusMessage = new TaskStatusMessage();
        statusMessage.setTaskId(taskId);
        statusMessage.setStatus(status);
        statusMessage.setMessage(message);
        statusMessage.setProgress(progress);
        statusMessage.setTotalTables(totalTables);
        statusMessage.setCompletedTables(completedTables);
        statusMessage.setCurrentTable(currentTable);
        statusMessage.setCurrentTableProgress(currentTableProgress);
        statusMessage.setCurrentTableRows(currentTableRows);
        statusMessage.setCurrentTableTotalRows(currentTableTotalRows);

        Long rpoMs = readMetricFile("./files/" + taskId + "/binlog_output/rpo_metric");
        Long rtoMs = readMetricFile("./files/" + taskId + "/binlog_output/rto_metric");
        Long calculatedRpo = calculateRpo();
        if (calculatedRpo != null) {
            rpoMs = calculatedRpo;
        }
        statusMessage.setRpoMs(rpoMs);
        statusMessage.setRtoMs(rtoMs);

        if ("FAILED".equals(status)) {
            String errorCode = SyncErrorCodeMapper.mapFailureToErrorCode(message);
            statusMessage.setErrorCode(errorCode);
            logger.info("[MigrationAgentThread-{}] FAILED status with errorCode={}, message={}", taskId, errorCode, message);
        }

        kafkaProducer.sendStatus(statusMessage);
    }

    private Long readMetricFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) return null;
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
                String line = reader.readLine();
                if (line != null && !line.isEmpty()) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2) {
                        return Long.parseLong(parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private Long readMetricTimestamp(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) return null;
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
                String line = reader.readLine();
                if (line != null && !line.isEmpty()) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        return Long.parseLong(parts[2].trim());
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private Long calculateRpo() {
        Long lastSourceEventTs = readMetricTimestamp("./files/" + taskId + "/binlog_output/rpo_metric");
        Long lastAppliedSourceTs = readMetricTimestamp("./files/" + taskId + "/binlog_output/rto_metric");
        if (lastSourceEventTs != null && lastAppliedSourceTs != null && lastSourceEventTs > 0 && lastAppliedSourceTs > 0) {
            long rpo = lastSourceEventTs - lastAppliedSourceTs;
            return rpo >= 0 ? rpo : 0L;
        }
        return null;
    }
}
