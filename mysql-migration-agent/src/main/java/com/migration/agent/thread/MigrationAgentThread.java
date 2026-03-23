package com.migration.agent.thread;

import com.migration.agent.checkpoint.CheckpointManager;
import com.migration.agent.checkpoint.CheckpointManager.BinlogPositionInfo;
import com.migration.agent.manager.ProcessManager;
import com.migration.agent.model.TaskMessage;
import com.migration.agent.model.TaskStatusMessage;
import com.migration.agent.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MigrationAgentThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MigrationAgentThread.class);
    
    private static final String BINLOG_JAR_PATH = "mysql-migration-binlog/target/mysql-migration-binlog-1.0.0.jar";
    private static final String MIGRATION_FULL_JAR_PATH = "mysql-migration-full/target/mysql-migration-full-1.0.0.jar";
    private static final String MIGRATION_INCREMENT_JAR_PATH = "mysql-migration-increment/target/mysql-migration-increment-1.0.0.jar";
    
    private static final long BINLOG_MONITOR_INTERVAL = 30000;
    private static final long INCREMENT_MONITOR_INTERVAL = 30000;
    private static final long STATUS_REPORT_INTERVAL = 60000;
    private static final long PROGRESS_MONITOR_INTERVAL = 3000;
    
    private final TaskMessage taskMessage;
    private final KafkaProducerService kafkaProducer;
    private final String taskId;
    private final AtomicBoolean running;
    private final AtomicBoolean stopped;
    private final boolean skipFullMigration;
    private final String migrationMode;
    
    private ProcessManager binlogProcess;
    private ProcessManager fullProcess;
    private ProcessManager incrementProcess;
    
    private Thread binlogMonitorThread;
    private Thread incrementMonitorThread;
    private Thread fullMigrationMonitorThread;
    
    private int totalTables = 0;
    private volatile int completedTables = 0;
    
    public MigrationAgentThread(TaskMessage taskMessage, KafkaProducerService kafkaProducer) {
        this(taskMessage, kafkaProducer, false);
    }
    
    public MigrationAgentThread(TaskMessage taskMessage, KafkaProducerService kafkaProducer, boolean skipFullMigration) {
        this.taskMessage = taskMessage;
        this.kafkaProducer = kafkaProducer;
        this.taskId = taskMessage.getTaskId();
        this.running = new AtomicBoolean(true);
        this.stopped = new AtomicBoolean(false);
        this.skipFullMigration = skipFullMigration;
        this.migrationMode = taskMessage.getMigrationMode();
        
        this.totalTables = calculateTotalTables(taskMessage.getSyncObjects());
    }
    
    private int calculateTotalTables(Map<String, Object> syncObjects) {
        if (syncObjects == null || syncObjects.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (Map.Entry<String, Object> entry : syncObjects.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List) {
                count += ((List<?>) value).size();
            } else if (value instanceof Map) {
                Map<?, ?> dbValue = (Map<?, ?>) value;
                Object tablesObj = dbValue.get("tables");
                if (tablesObj instanceof List) {
                    count += ((List<?>) tablesObj).size();
                }
            }
        }
        return count;
    }
    
    @Override
    public void run() {
        String threadName = "MigrationAgentThread-" + taskId;
        Thread.currentThread().setName(threadName);
        logger.info("[{}] 开始执行同步任务, skipFullMigration={}, migrationMode={}", threadName, skipFullMigration, migrationMode);
        
        try {
            if (skipFullMigration) {
                sendStatus("INCREMENT_RUNNING", "从增量同步阶段恢复", 100);
                
                if (!startBinlogProcess()) {
                    return;
                }
                
                if (!startIncrementProcess()) {
                    return;
                }
                
                logger.info("[{}] 增量同步任务恢复完成，进入持续监控模式", threadName);
            } else {
                sendStatus("STARTING", "任务启动中", 0, totalTables, 0, null, 0, 0L, 0L);
                
                // 1. 先记录或加载 checkpoint
                if (!initCheckpoint()) {
                    return;
                }
                
                // 2. 启动 binlog 进程（从 checkpoint 位点开始监听）
                if (!startBinlogProcess()) {
                    return;
                }
                
                // 3. 执行全量迁移（不需要再记录 checkpoint）
                if (!executeFullMigration()) {
                    return;
                }
                
                boolean isFullOnly = !"fullAndIncre".equals(migrationMode);
                if (isFullOnly) {
                    logger.info("[{}] 仅全量同步任务完成，状态: FULL_COMPLETED", threadName);
                    sendStatus("FULL_COMPLETED", "全量同步完成", 100, totalTables, totalTables, null, 100, 0L, 0L);
                    return;
                }
                
                if (!startIncrementProcess()) {
                    return;
                }
                
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
            sendStatus("FAILED", "任务执行异常: " + e.getMessage(), 0);
        } finally {
            stopAllProcesses();
            logger.info("[{}] 同步任务线程结束", threadName);
        }
    }
    
    private boolean initCheckpoint() {
        String threadName = "MigrationAgentThread-" + taskId;
        logger.info("[{}] 初始化 checkpoint", threadName);
        
        String checkpointDbPath = "./files/" + taskId + "/checkpoint/checkpoint";
        CheckpointManager checkpointManager = null;
        
        try {
            checkpointManager = new CheckpointManager(checkpointDbPath);
            
            // 检查是否已有 checkpoint 记录
            BinlogPositionInfo existingCheckpoint = checkpointManager.loadCheckpoint();
            
            if (existingCheckpoint != null) {
                logger.info("[{}] 发现已存在的 checkpoint: {}", threadName, existingCheckpoint);
                // 已有 checkpoint，binlog 进程将从此位点开始监听
            } else {
                // 没有 checkpoint，需要从源数据库获取当前位点并记录
                logger.info("[{}] 未找到 checkpoint，从源数据库获取当前位点", threadName);
                
                // 从配置文件读取源数据库信息
                String sourceHost = null;
                int sourcePort = 3306;
                String sourceUser = null;
                String sourcePassword = null;
                
                // 优先从 taskMessage 获取
                TaskMessage.DatabaseConfig sourceConfig = taskMessage.getSource();
                if (sourceConfig != null) {
                    sourceHost = sourceConfig.getHost();
                    sourcePort = sourceConfig.getPort();
                    sourceUser = sourceConfig.getUsername();
                    sourcePassword = sourceConfig.getPassword();
                    logger.info("[{}] 从 taskMessage.getSource() 获取源数据库配置: {}:{}", threadName, sourceHost, sourcePort);
                }
                
                // 如果 taskMessage 中没有，尝试从 sourceConnection 解析
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
                
                // 如果还是没有，从配置文件读取
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
                logger.info("[{}] 已记录当前位点作为 checkpoint: {}", threadName, currentPosition);
            }
            
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
    
    private boolean startBinlogProcess() {
        String threadName = "MigrationAgentThread-" + taskId;
        logger.info("[{}] 启动 binlog 监控进程", threadName);
        
        try {
            binlogProcess = new ProcessManager(BINLOG_JAR_PATH, "BinlogMain-" + taskId);
            binlogProcess.setTaskId(taskId);
            binlogProcess.start();
            
            Thread.sleep(2000);
            
            if (!binlogProcess.isRunning()) {
                logger.error("[{}] binlog 进程启动失败", threadName);
                sendStatus("FAILED", "binlog 进程启动失败", 0);
                return false;
            }
            
            logger.info("[{}] binlog 监控进程启动成功", threadName);
            
            binlogMonitorThread = new Thread(() -> {
                while (running.get() && binlogProcess != null) {
                    try {
                        Thread.sleep(BINLOG_MONITOR_INTERVAL);
                        
                        if (!running.get() || stopped.get()) {
                            break;
                        }
                        
                        if (!binlogProcess.isRunning()) {
                            if (stopped.get()) {
                                logger.info("[{}] binlog 进程已停止（暂停）", threadName);
                                break;
                            }
                            logger.error("[{}] binlog 进程异常退出", threadName);
                            sendStatus("FAILED", "binlog 进程异常退出", 0);
                            running.set(false);
                            break;
                        }
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.error("[{}] binlog 监控异常", threadName, e);
                    }
                }
            }, "BinlogMonitor-" + taskId);
            binlogMonitorThread.setDaemon(true);
            binlogMonitorThread.start();
            
            return true;
            
        } catch (Exception e) {
            logger.error("[{}] 启动 binlog 进程失败", threadName, e);
            sendStatus("FAILED", "启动 binlog 进程失败: " + e.getMessage(), 0);
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
                        
                        logger.debug("[{}] 全量同步进度: {}/{}, 当前表: {} ({}%)", 
                            taskId, completedTables, totalTables, currentTable, currentTableProgress);
                        
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
            incrementProcess = new ProcessManager(MIGRATION_INCREMENT_JAR_PATH, "MigrationIncrement-" + taskId);
            incrementProcess.setTaskId(taskId);
            incrementProcess.setMainArgs(new String[]{taskId});
            incrementProcess.start();
            
            Thread.sleep(2000);
            
            if (!incrementProcess.isRunning()) {
                logger.error("[{}] 增量同步进程启动失败", threadName);
                sendStatus("FAILED", "增量同步进程启动失败", 100);
                return false;
            }
            
            sendStatus("INCREMENT_RUNNING", "增量同步中", 100);
            
            incrementMonitorThread = new Thread(() -> {
                long lastReportTime = System.currentTimeMillis();
                
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
                            sendStatus("FAILED", "增量同步进程异常退出", 100);
                            running.set(false);
                            break;
                        }
                        
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastReportTime >= STATUS_REPORT_INTERVAL) {
                            sendStatus("INCREMENT_RUNNING", "增量同步中", 100);
                            lastReportTime = currentTime;
                            logger.debug("[{}] 增量同步状态已上报", threadName);
                        }
                        
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
            
            logger.info("[{}] 增量同步进程启动成功", threadName);
            return true;
            
        } catch (Exception e) {
            logger.error("[{}] 启动增量同步进程失败", threadName, e);
            sendStatus("FAILED", "启动增量同步进程失败: " + e.getMessage(), 100);
            return false;
        }
    }
    
    public void stop() {
        String threadName = "MigrationAgentThread-" + taskId;
        logger.info("[{}] 收到停止信号", threadName);
        
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
        
        if (fullProcess != null) {
            try {
                fullProcess.stop();
                logger.info("[{}] 全量迁移进程已停止", threadName);
            } catch (Exception e) {
                logger.error("[{}] 停止全量迁移进程失败", threadName, e);
            }
        }
        
        if (binlogProcess != null) {
            try {
                binlogProcess.stop();
                logger.info("[{}] binlog 进程已停止", threadName);
            } catch (Exception e) {
                logger.error("[{}] 停止 binlog 进程失败", threadName, e);
            }
        }
        
        if (binlogMonitorThread != null) {
            binlogMonitorThread.interrupt();
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
        
        kafkaProducer.sendStatus(statusMessage);
    }
}
