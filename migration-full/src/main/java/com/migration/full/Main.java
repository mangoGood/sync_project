package com.migration.full;

import com.migration.config.MigrationConfig;
import com.migration.db.DatabaseConnection;
import com.migration.full.checkpoint.CheckpointRecorder;
import com.migration.full.metadata.MetadataReader;
import com.migration.full.migration.DataMigration;
import com.migration.full.migration.SchemaMigration;
import com.migration.model.TableInfo;
import com.migration.full.progress.ProgressManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * MySQL 数据库全量迁移工具主程序
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("数据库全量迁移工具启动");
        logger.info("========================================");

        ProgressManager progressManager = null;

        try {
            String taskId = getTaskId(args);
            String configFile = getConfigFile(args, taskId);
            logger.info("任务 ID: {}", taskId);
            logger.info("使用配置文件: {}", configFile);

            MigrationConfig config = taskId != null ? 
                new MigrationConfig(configFile, taskId) : new MigrationConfig(configFile);
            logger.info("源数据库: {}", config.getSourceConfig().getDatabase());
            logger.info("目标数据库: {}", config.getTargetConfig().getDatabase());

            String progressDbPath = taskId != null ? 
                "./files/" + taskId + "/migration_progress" : "./migration_progress";
            progressManager = new ProgressManager(progressDbPath, config.isEnableResume());
            
            // 检查是否有未完成的迁移
            if (progressManager.isEnabled() && progressManager.hasIncompleteProgress()) {
                logger.info("\n========================================");
                logger.info("检测到未完成的迁移");
                logger.info("========================================");
                progressManager.printProgressSummary();
                logger.info("将从上次中断的位置继续迁移\n");
            }

            // 创建数据库连接
            DatabaseConnection sourceConn = new DatabaseConnection(config.getSourceConfig());
            DatabaseConnection targetConn = new DatabaseConnection(config.getTargetConfig());

            // 测试源数据库连接
            if (!sourceConn.testConnection()) {
                throw new SQLException("无法连接到源数据库");
            }

            // 确保目标数据库存在（如果不存在则创建）
            targetConn.ensureDatabaseExists();

            // 测试目标数据库连接
            if (!targetConn.testConnection()) {
                throw new SQLException("无法连接到目标数据库");
            }

            // 记录源数据库快照位点（用于增量同步）
            // 只有在配置允许时才记录，agent 已经在启动前记录了 checkpoint
            String checkpointDbPath = config.getCheckpointDbPath();
            if (checkpointDbPath != null && !checkpointDbPath.isEmpty() && config.isRecordCheckpoint()) {
                CheckpointRecorder checkpointRecorder = new CheckpointRecorder(checkpointDbPath);
                try {
                    checkpointRecorder.recordSnapshot(sourceConn);
                } finally {
                    checkpointRecorder.close();
                }
            } else {
                logger.info("跳过 checkpoint 记录（已由 agent 在启动前记录）");
            }

            // 读取源数据库元数据
            MetadataReader metadataReader = new MetadataReader(sourceConn);
            
            List<TableInfo> tables;
            Set<String> includedTables = config.getIncludedTables();
            
            if (includedTables != null && !includedTables.isEmpty()) {
                logger.info("使用同步对象过滤，共指定 {} 个表", includedTables.size());
                tables = metadataReader.getFilteredTablesInfo(includedTables);
            } else {
                tables = metadataReader.getAllTablesInfo();
            }
            
            if (tables.isEmpty()) {
                logger.warn("源数据库中没有找到任何表");
                return;
            }

            logger.info("找到 {} 个表需要迁移", tables.size());
            for (TableInfo table : tables) {
                long rowCount = metadataReader.getTableRowCount(table.getTableName());
                logger.info("  - {}: {} 行", table.getTableName(), rowCount);
            }

            // 迁移表结构
            if (config.isCreateTables()) {
                logger.info("\n========================================");
                logger.info("开始迁移表结构");
                logger.info("========================================");
                
                SchemaMigration schemaMigration = new SchemaMigration(
                    sourceConn, targetConn, config.isDropTables()
                );
                schemaMigration.migrateAllTables(tables);
                logger.info("表结构迁移完成");
            }

            // 迁移数据
            if (config.isMigrateData()) {
                logger.info("\n========================================");
                logger.info("开始迁移数据");
                logger.info("========================================");
                
                DataMigration dataMigration = new DataMigration(
                    sourceConn, targetConn, 
                    config.getBatchSize(), 
                    config.isContinueOnError(),
                    progressManager
                );
                dataMigration.migrateAllData(tables);
                logger.info("数据迁移完成");
            }

            // 打印迁移进度摘要
            if (progressManager.isEnabled()) {
                logger.info("\n========================================");
                logger.info("迁移进度摘要");
                logger.info("========================================");
                progressManager.printProgressSummary();
            }

            // 关闭连接
            sourceConn.close();
            targetConn.close();

            logger.info("\n========================================");
            logger.info("数据库全量迁移成功完成！");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("数据库全量迁移失败", e);
            System.exit(1);
        } finally {
            // 关闭进度管理器
            if (progressManager != null) {
                progressManager.close();
            }
        }
    }

    /**
     * 获取任务 ID
     */
    private static String getTaskId(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--task-id".equals(args[i]) || "-t".equals(args[i])) {
                return args[i + 1];
            }
        }
        String taskId = System.getProperty("task.id");
        if (taskId != null && !taskId.isEmpty()) {
            return taskId;
        }
        taskId = System.getenv("TASK_ID");
        if (taskId != null && !taskId.isEmpty()) {
            return taskId;
        }
        return null;
    }

    /**
     * 获取配置文件路径
     */
    private static String getConfigFile(String[] args, String taskId) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--config".equals(args[i]) || "-c".equals(args[i])) {
                return args[i + 1];
            }
        }
        
        String defaultConfig;
        if (taskId != null) {
            defaultConfig = "files/" + taskId + "/config.properties";
        } else {
            defaultConfig = "config.properties";
        }
        
        File configFile = new File(defaultConfig);
        
        if (configFile.exists()) {
            return defaultConfig;
        }
        
        throw new RuntimeException("配置文件不存在: " + defaultConfig + 
                                 "\n请提供配置文件路径作为参数，或确保 config.properties 存在");
    }
}