package com.migration.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 迁移配置类
 */
public class MigrationConfig {
    private DatabaseConfig sourceConfig;
    private DatabaseConfig targetConfig;
    private int batchSize;
    private boolean dropTables;
    private boolean createTables;
    private boolean migrateData;
    private boolean continueOnError;
    private boolean enableResume;
    private boolean enableIncremental;
    private boolean recordCheckpoint;
    private Set<String> includedDatabases;
    private Set<String> includedTables;
    private String checkpointDbPath;
    private String taskId;

    public MigrationConfig(String configFile) throws IOException {
        loadConfig(configFile);
    }
    
    public MigrationConfig(String configFile, String taskId) throws IOException {
        this.taskId = taskId;
        loadConfig(configFile);
    }

    private void loadConfig(String configFile) throws IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(configFile)) {
            props.load(input);
        }

        // 加载源数据库配置
        sourceConfig = new DatabaseConfig(
            props.getProperty("source.db.host", "localhost"),
            Integer.parseInt(props.getProperty("source.db.port", "3306")),
            props.getProperty("source.db.database"),
            props.getProperty("source.db.username"),
            props.getProperty("source.db.password")
        );

        // 加载目标数据库配置
        targetConfig = new DatabaseConfig(
            props.getProperty("target.db.host", "localhost"),
            Integer.parseInt(props.getProperty("target.db.port", "3306")),
            props.getProperty("target.db.database"),
            props.getProperty("target.db.username"),
            props.getProperty("target.db.password")
        );

        // 加载迁移配置
        batchSize = Integer.parseInt(props.getProperty("migration.batch.size", "1000"));
        dropTables = Boolean.parseBoolean(props.getProperty("migration.drop.tables", "false"));
        createTables = Boolean.parseBoolean(props.getProperty("migration.create.tables", "true"));
        migrateData = Boolean.parseBoolean(props.getProperty("migration.migrate.data", "true"));
        continueOnError = Boolean.parseBoolean(props.getProperty("migration.continue.on.error", "false"));
        enableResume = Boolean.parseBoolean(props.getProperty("migration.enable.resume", "true"));
        enableIncremental = Boolean.parseBoolean(props.getProperty("migration.enable.incremental", "false"));
        recordCheckpoint = Boolean.parseBoolean(props.getProperty("migration.record.checkpoint", "true"));
        
        // 加载增量迁移配置
        includedDatabases = parseStringSet(props.getProperty("migration.included.databases", ""));
        includedTables = parseStringSet(props.getProperty("migration.included.tables", ""));
        
        // 加载 checkpoint 数据库路径，支持按任务 ID 隔离
        String defaultCheckpointPath = taskId != null ? 
            "./files/" + taskId + "/checkpoint/checkpoint" : "./checkpoint/checkpoint";
        checkpointDbPath = props.getProperty("migration.checkpoint.db.path", defaultCheckpointPath);
    }
    
    /**
     * 解析字符串集合（逗号分隔）
     */
    private Set<String> parseStringSet(String value) {
        Set<String> result = new HashSet<>();
        if (value != null && !value.trim().isEmpty()) {
            String[] parts = value.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    public DatabaseConfig getSourceConfig() {
        return sourceConfig;
    }

    public DatabaseConfig getTargetConfig() {
        return targetConfig;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public boolean isDropTables() {
        return dropTables;
    }

    public boolean isCreateTables() {
        return createTables;
    }

    public boolean isMigrateData() {
        return migrateData;
    }

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public boolean isEnableResume() {
        return enableResume;
    }
    
    public boolean isEnableIncremental() {
        return enableIncremental;
    }
    
    public boolean isRecordCheckpoint() {
        return recordCheckpoint;
    }
    
    public Set<String> getIncludedDatabases() {
        return includedDatabases;
    }
    
    public Set<String> getIncludedTables() {
        return includedTables;
    }
    
    public String getCheckpointDbPath() {
        return checkpointDbPath;
    }
    
    public String getTaskId() {
        return taskId;
    }
}
