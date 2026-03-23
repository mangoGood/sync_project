package com.migration.binlog.handler;

import com.migration.binlog.core.BinlogPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SQL 文件管理器
 * 管理 SQL 语句的写入和文件分割
 * 
 * 文件格式：
 * -- Binlog SQL Export
 * -- Generated at: 2026-03-01T10:50:12
 * -- File: binlog_sql_20260301_105012_0001.sql
 * 
 * [POSITION] binlog.000011:1042
 * [GTID] 3E11FA47-71CA-11E1-9E33-C80AA9429562:1-5
 * INSERT INTO db.table (id, name) VALUES (1, 'test');
 * 
 * [POSITION] binlog.000011:1250
 * [GTID] 3E11FA47-71CA-11E1-9E33-C80AA9429562:6
 * UPDATE db.table SET name = 'updated' WHERE id = 1;
 */
public class SqlFileManager {
    private static final Logger logger = LoggerFactory.getLogger(SqlFileManager.class);
    
    private static final int MAX_SQL_PER_FILE = 10000;
    private static final String FILE_PREFIX = "binlog_sql_";
    private static final String FILE_SUFFIX = ".sql";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    private final String outputDirectory;
    private final AtomicInteger fileSequence;
    private final AtomicLong sqlCount;
    
    private BufferedWriter currentWriter;
    private String currentFileName;
    private int currentFileSqlCount;
    
    public SqlFileManager(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.fileSequence = new AtomicInteger(0);
        this.sqlCount = new AtomicLong(0);
        this.currentFileSqlCount = 0;
        
        // 确保输出目录存在
        createDirectoryIfNotExists();
        
        // 初始化第一个文件
        try {
            createNewFile();
        } catch (IOException e) {
            logger.error("初始化 SQL 文件失败", e);
            throw new RuntimeException("无法初始化 SQL 文件", e);
        }
    }
    
    /**
     * 确保输出目录存在
     */
    private void createDirectoryIfNotExists() {
        Path path = Paths.get(outputDirectory);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                logger.info("创建输出目录: {}", outputDirectory);
            } catch (IOException e) {
                logger.error("创建输出目录失败: {}", outputDirectory, e);
                throw new RuntimeException("无法创建输出目录", e);
            }
        }
    }
    
    /**
     * 创建新的 SQL 文件
     */
    private void createNewFile() throws IOException {
        // 关闭当前文件
        closeCurrentFile();
        
        // 生成文件名
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        int sequence = fileSequence.incrementAndGet();
        currentFileName = String.format("%s%s_%04d%s", FILE_PREFIX, timestamp, sequence, FILE_SUFFIX);
        
        String fullPath = Paths.get(outputDirectory, currentFileName).toString();
        
        // 创建新文件
        File file = new File(fullPath);
        currentWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(file, true), StandardCharsets.UTF_8));
        currentFileSqlCount = 0;
        
        // 写入文件头
        currentWriter.write("-- Binlog SQL Export");
        currentWriter.newLine();
        currentWriter.write("-- Generated at: " + LocalDateTime.now());
        currentWriter.newLine();
        currentWriter.write("-- File: " + currentFileName);
        currentWriter.newLine();
        currentWriter.write("-- Format: [POSITION] filename:position, [GTID] gtid_value, SQL statement");
        currentWriter.newLine();
        currentWriter.newLine();
        
        // 立即刷新，确保文件头写入磁盘
        currentWriter.flush();
        
        logger.info("创建新的 SQL 文件: {}", fullPath);
    }
    
    /**
     * 关闭当前文件
     */
    private void closeCurrentFile() throws IOException {
        if (currentWriter != null) {
            currentWriter.flush();
            currentWriter.close();
            currentWriter = null;
            logger.info("关闭 SQL 文件: {}, 包含 {} 条 SQL 语句", currentFileName, currentFileSqlCount);
        }
    }
    
    /**
     * 写入 SQL 语句（带位置信息）
     */
    public synchronized void writeSql(String sql, BinlogPosition position) {
        if (sql == null || sql.trim().isEmpty()) {
            logger.warn("尝试写入空的 SQL 语句");
            return;
        }
        
        logger.debug("写入 SQL: {}, position: {}", sql, position);
        
        try {
            // 检查是否需要创建新文件
            if (currentFileSqlCount >= MAX_SQL_PER_FILE) {
                createNewFile();
            }
            
            // 写入位置信息
            if (position != null) {
                // 写入 POSITION
                currentWriter.write("[POSITION] ");
                currentWriter.write(position.getFilename());
                currentWriter.write(":");
                currentWriter.write(String.valueOf(position.getPosition()));
                currentWriter.newLine();
                
                // 写入 GTID（如果为空则记录为空字符串）
                currentWriter.write("[GTID] ");
                if (position.getGtid() != null && !position.getGtid().isEmpty()) {
                    currentWriter.write(position.getGtid());
                }
                currentWriter.newLine();
            }
            
            // 写入 SQL 语句
            currentWriter.write(sql);
            if (!sql.endsWith(";")) {
                currentWriter.write(";");
            }
            currentWriter.newLine();
            currentWriter.newLine();  // 空行分隔
            
            // 立即刷新，确保写入磁盘
            currentWriter.flush();
            
            currentFileSqlCount++;
            sqlCount.incrementAndGet();
            
            logger.debug("SQL 已写入文件，当前文件 SQL 数量: {}", currentFileSqlCount);
            
        } catch (IOException e) {
            logger.error("写入 SQL 语句失败: {}", sql, e);
        }
    }
    
    /**
     * 写入 SQL 语句（不带位置信息，保持向后兼容）
     */
    public synchronized void writeSql(String sql) {
        writeSql(sql, null);
    }
    
    /**
     * 获取当前文件序号
     */
    public int getCurrentFileSequence() {
        return fileSequence.get();
    }
    
    /**
     * 获取当前文件 SQL 数量
     */
    public int getCurrentFileSqlCount() {
        return currentFileSqlCount;
    }
    
    /**
     * 获取总 SQL 数量
     */
    public long getTotalSqlCount() {
        return sqlCount.get();
    }
    
    /**
     * 获取当前文件名
     */
    public String getCurrentFileName() {
        return currentFileName;
    }
    
    /**
     * 关闭管理器
     */
    public void close() {
        try {
            closeCurrentFile();
            logger.info("SQL 文件管理器已关闭，总共写入 {} 条 SQL 语句，生成 {} 个文件", 
                    sqlCount.get(), fileSequence.get());
        } catch (IOException e) {
            logger.error("关闭 SQL 文件管理器失败", e);
        }
    }
}