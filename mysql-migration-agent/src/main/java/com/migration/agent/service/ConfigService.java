package com.migration.agent.service;

import com.google.gson.Gson;
import com.migration.agent.model.TaskMessage;
import com.migration.agent.util.ConnectionStringParser;
import com.migration.agent.util.ConnectionStringParser.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigService {
        private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private final Gson gson = new Gson();
    
    public void updateConfig(TaskMessage taskMessage) throws IOException {
        String taskId = taskMessage.getTaskId();
        logger.info("Updating config file for task: {}", taskId);
        
        File taskDir = new File("files/" + taskId);
        if (!taskDir.exists()) {
            boolean created = taskDir.mkdirs();
            logger.info("Task directory created: {}, success: {}", taskDir.getAbsolutePath(), created);
        }
        
        File checkpointDir = new File(taskDir, "checkpoint");
        if (!checkpointDir.exists()) {
            checkpointDir.mkdirs();
            logger.info("Checkpoint directory created: {}", checkpointDir.getAbsolutePath());
        }
        
        File logsDir = new File(taskDir, "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
            logger.info("Logs directory created: {}", logsDir.getAbsolutePath());
        }
        
        File binlogOutputDir = new File(taskDir, "binlog_output");
        if (!binlogOutputDir.exists()) {
            binlogOutputDir.mkdirs();
            logger.info("Binlog output directory created: {}", binlogOutputDir.getAbsolutePath());
        }
        
        File sqlOutputDir = new File(taskDir, "sql_output");
        if (!sqlOutputDir.exists()) {
            sqlOutputDir.mkdirs();
            logger.info("SQL output directory created: {}", sqlOutputDir.getAbsolutePath());
        }
        
        Properties props = new Properties();
        
        File configFile = new File(taskDir, "config.properties");
        logger.info("Config file path: {}", configFile.getAbsolutePath());
        
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                props.load(input);
            }
            logger.info("Loaded existing config file");
        } else {
            logger.info("Creating new config file");
        }
        
        if (taskMessage.getSourceConnection() != null && !taskMessage.getSourceConnection().isEmpty()) {
            ConnectionInfo sourceInfo = ConnectionStringParser.parse(taskMessage.getSourceConnection());
            if (sourceInfo != null) {
                props.setProperty("source.db.host", sourceInfo.getHost());
                props.setProperty("source.db.port", String.valueOf(sourceInfo.getPort()));
                props.setProperty("source.db.database", sourceInfo.getDatabase());
                props.setProperty("source.db.username", sourceInfo.getUsername());
                props.setProperty("source.db.password", sourceInfo.getPassword());
                logger.info("Source database config updated: {}:{}", sourceInfo.getHost(), sourceInfo.getPort());
            }
        } else if (taskMessage.getSource() != null) {
            TaskMessage.DatabaseConfig source = taskMessage.getSource();
            props.setProperty("source.db.host", source.getHost());
            props.setProperty("source.db.port", String.valueOf(source.getPort()));
            props.setProperty("source.db.database", source.getDatabase());
            props.setProperty("source.db.username", source.getUsername());
            props.setProperty("source.db.password", source.getPassword());
            logger.info("Source database config updated from DatabaseConfig: {}:{}", source.getHost(), source.getPort());
        }
        
        if (taskMessage.getTargetConnection() != null && !taskMessage.getTargetConnection().isEmpty()) {
            ConnectionInfo targetInfo = ConnectionStringParser.parse(taskMessage.getTargetConnection());
            if (targetInfo != null) {
                props.setProperty("target.db.host", targetInfo.getHost());
                props.setProperty("target.db.port", String.valueOf(targetInfo.getPort()));
                props.setProperty("target.db.database", targetInfo.getDatabase());
                props.setProperty("target.db.username", targetInfo.getUsername());
                props.setProperty("target.db.password", targetInfo.getPassword());
                logger.info("Target database config updated: {}:{}", targetInfo.getHost(), targetInfo.getPort());
            }
        } else if (taskMessage.getTarget() != null) {
            TaskMessage.DatabaseConfig target = taskMessage.getTarget();
            props.setProperty("target.db.host", target.getHost());
            props.setProperty("target.db.port", String.valueOf(target.getPort()));
            props.setProperty("target.db.database", target.getDatabase());
            props.setProperty("target.db.username", target.getUsername());
            props.setProperty("target.db.password", target.getPassword());
            logger.info("Target database config updated from DatabaseConfig: {}:{}", target.getHost(), target.getPort());
        }
        
        if (taskMessage.getSyncObjects() != null && !taskMessage.getSyncObjects().isEmpty()) {
            String syncObjectsJson = gson.toJson(taskMessage.getSyncObjects());
            props.setProperty("migration.sync.objects", syncObjectsJson);
            logger.info("Sync objects config updated: {}", syncObjectsJson);
            
            StringBuilder includedDatabases = new StringBuilder();
            StringBuilder includedTables = new StringBuilder();
            
            for (Map.Entry<String, Object> dbEntry : taskMessage.getSyncObjects().entrySet()) {
                String dbName = dbEntry.getKey();
                if (includedDatabases.length() > 0) {
                    includedDatabases.append(",");
                }
                includedDatabases.append(dbName);
                
                Object value = dbEntry.getValue();
                if (value instanceof List) {
                    List<?> tables = (List<?>) value;
                    for (Object table : tables) {
                        if (includedTables.length() > 0) {
                            includedTables.append(",");
                        }
                        includedTables.append(dbName).append(".").append(table.toString());
                    }
                } else if (value instanceof Map) {
                    Map<?, ?> dbValue = (Map<?, ?>) value;
                    Object tablesObj = dbValue.get("tables");
                    if (tablesObj instanceof List) {
                        List<?> tables = (List<?>) tablesObj;
                        for (Object table : tables) {
                            if (includedTables.length() > 0) {
                                includedTables.append(",");
                            }
                            includedTables.append(dbName).append(".").append(table.toString());
                        }
                    }
                }
            }
            
            if (includedDatabases.length() > 0) {
                props.setProperty("migration.included.databases", includedDatabases.toString());
                logger.info("Included databases: {}", includedDatabases.toString());
            }
            if (includedTables.length() > 0) {
                props.setProperty("migration.included.tables", includedTables.toString());
                logger.info("Included tables: {}", includedTables.toString());
            }
        }
        
        if (taskMessage.getSourceDbName() != null && !taskMessage.getSourceDbName().isEmpty()) {
            props.setProperty("source.db.name", taskMessage.getSourceDbName());
            logger.info("Source database name: {}", taskMessage.getSourceDbName());
        }
        
        // 设置不记录 checkpoint，因为 agent 已经在启动前记录了
        props.setProperty("migration.record.checkpoint", "false");
        logger.info("Checkpoint recording disabled (already recorded by agent before startup)");
        
        try (OutputStream output = new FileOutputStream(configFile)) {
            props.store(output, "Updated by Migration Agent for task: " + taskId);
        }
        
        createLogbackConfig(taskDir, taskId);
        
        logger.info("Config file updated successfully for task: {}", taskId);
    }
    
    private void createLogbackConfig(File taskDir, String taskId) throws IOException {
        File logbackFile = new File(taskDir, "logback.xml");
        
        String logbackContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <property name=\"LOG_PATH\" value=\"files/" + taskId + "/logs\"/>\n" +
            "    <property name=\"LOG_FILE\" value=\"migration\"/>\n" +
            "\n" +
            "    <appender name=\"CONSOLE\" class=\"ch.qos.logback.core.ConsoleAppender\">\n" +
            "        <encoder>\n" +
            "            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>\n" +
            "            <charset>UTF-8</charset>\n" +
            "        </encoder>\n" +
            "    </appender>\n" +
            "\n" +
            "    <appender name=\"FILE\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n" +
            "        <file>${LOG_PATH}/${LOG_FILE}.log</file>\n" +
            "        <encoder>\n" +
            "            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>\n" +
            "            <charset>UTF-8</charset>\n" +
            "        </encoder>\n" +
            "        <rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">\n" +
            "            <fileNamePattern>${LOG_PATH}/${LOG_FILE}.%d{yyyy-MM-dd}.%i.log</fileNamePattern>\n" +
            "            <timeBasedFileNamingAndTriggeringPolicy class=\"ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP\">\n" +
            "                <maxFileSize>100MB</maxFileSize>\n" +
            "            </timeBasedFileNamingAndTriggeringPolicy>\n" +
            "            <maxHistory>30</maxHistory>\n" +
            "            <totalSizeCap>10GB</totalSizeCap>\n" +
            "        </rollingPolicy>\n" +
            "    </appender>\n" +
            "\n" +
            "    <root level=\"INFO\">\n" +
            "        <appender-ref ref=\"CONSOLE\"/>\n" +
            "        <appender-ref ref=\"FILE\"/>\n" +
            "    </root>\n" +
            "\n" +
            "    <logger name=\"com.migration\" level=\"DEBUG\"/>\n" +
            "</configuration>";
        
        try (OutputStream output = new FileOutputStream(logbackFile)) {
            output.write(logbackContent.getBytes(StandardCharsets.UTF_8));
        }
        
        logger.info("Logback config created for task: {}", taskId);
    }
}