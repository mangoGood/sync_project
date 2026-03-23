package com.migration.increment.parser;

import com.migration.binlog.core.BinlogPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL 文件解析器
 * 解析 binlog SQL 文件，提取 POSITION、GTID 和 SQL 语句
 */
public class SqlFileParser {
    private static final Logger logger = LoggerFactory.getLogger(SqlFileParser.class);
    
    /**
     * SQL 条目
     */
    public static class SqlEntry {
        private String filename;
        private long position;
        private String gtid;
        private String sql;
        
        public SqlEntry(String filename, long position, String gtid, String sql) {
            this.filename = filename;
            this.position = position;
            this.gtid = gtid;
            this.sql = sql;
        }
        
        public String getFilename() {
            return filename;
        }
        
        public long getPosition() {
            return position;
        }
        
        public String getGtid() {
            return gtid;
        }
        
        public String getSql() {
            return sql;
        }
        
        /**
         * 转换为 BinlogPosition
         */
        public BinlogPosition toBinlogPosition() {
            return new BinlogPosition(filename, position, gtid);
        }
        
        @Override
        public String toString() {
            return String.format("SqlEntry{filename='%s', position=%d, gtid='%s', sql='%s...'}",
                    filename, position, gtid, sql.substring(0, Math.min(50, sql.length())));
        }
    }
    
    /**
     * 解析 SQL 文件
     */
    public List<SqlEntry> parseFile(String filePath) throws IOException {
        List<SqlEntry> entries = new ArrayList<>();
        File file = new File(filePath);
        
        if (!file.exists()) {
            logger.warn("SQL 文件不存在: {}", filePath);
            return entries;
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            String currentFilename = null;
            long currentPosition = -1;
            String currentGtid = null;
            StringBuilder currentSql = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // 跳过空行和注释
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                // 解析 POSITION
                if (line.startsWith("[POSITION]")) {
                    // 如果已有 SQL，保存之前的条目
                    if (currentSql.length() > 0 && currentFilename != null) {
                        entries.add(new SqlEntry(currentFilename, currentPosition, currentGtid, currentSql.toString().trim()));
                        currentSql = new StringBuilder();
                    }
                    
                    String positionStr = line.substring("[POSITION]".length()).trim();
                    String[] parts = positionStr.split(":");
                    if (parts.length == 2) {
                        currentFilename = parts[0];
                        try {
                            currentPosition = Long.parseLong(parts[1]);
                        } catch (NumberFormatException e) {
                            logger.warn("解析 position 失败: {}", positionStr);
                        }
                    }
                    continue;
                }
                
                // 解析 GTID
                if (line.startsWith("[GTID]")) {
                    currentGtid = line.substring("[GTID]".length()).trim();
                    // 如果是空字符串，设为 null
                    if (currentGtid.isEmpty()) {
                        currentGtid = null;
                    }
                    continue;
                }
                
                // 累积 SQL 语句
                currentSql.append(line).append(" ");
            }
            
            // 保存最后一个条目
            if (currentSql.length() > 0 && currentFilename != null) {
                entries.add(new SqlEntry(currentFilename, currentPosition, currentGtid, currentSql.toString().trim()));
            }
        }
        
        logger.info("解析 SQL 文件完成: {}, 共 {} 条 SQL", filePath, entries.size());
        return entries;
    }
    
    /**
     * 解析目录中的所有 SQL 文件
     */
    public List<SqlEntry> parseDirectory(String directoryPath) throws IOException {
        List<SqlEntry> allEntries = new ArrayList<>();
        File dir = new File(directoryPath);
        
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("SQL 目录不存在: {}", directoryPath);
            return allEntries;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (files != null) {
            for (File file : files) {
                allEntries.addAll(parseFile(file.getAbsolutePath()));
            }
        }
        
        // 按 position 排序
        allEntries.sort((a, b) -> {
            int filenameCompare = a.getFilename().compareTo(b.getFilename());
            if (filenameCompare != 0) {
                return filenameCompare;
            }
            return Long.compare(a.getPosition(), b.getPosition());
        });
        
        logger.info("解析 SQL 目录完成: {}, 共 {} 条 SQL", directoryPath, allEntries.size());
        return allEntries;
    }
}
