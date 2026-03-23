package com.migration.agent.checkpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class CheckpointManager {
    private static final Logger logger = LoggerFactory.getLogger(CheckpointManager.class);
    
    private static final String DB_URL_PREFIX = "jdbc:h2:file:";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    
    private String dbPath;
    private Connection connection;
    
    public CheckpointManager(String dbPath) {
        this.dbPath = dbPath;
        initDatabase();
    }
    
    private void initDatabase() {
        try {
            String url = DB_URL_PREFIX + dbPath;
            connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
            
            String createTableSql = "CREATE TABLE IF NOT EXISTS checkpoint (" +
                    "id INT PRIMARY KEY," +
                    "filename VARCHAR(255)," +
                    "position BIGINT," +
                    "gtid VARCHAR(255)," +
                    "timestamp BIGINT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSql);
            }
            
            logger.info("Checkpoint 数据库初始化成功: {}", dbPath);
        } catch (SQLException e) {
            logger.error("初始化 Checkpoint 数据库失败", e);
            throw new RuntimeException("无法初始化 Checkpoint 数据库", e);
        }
    }
    
    public BinlogPositionInfo loadCheckpoint() {
        String sql = "SELECT filename, position, gtid, timestamp FROM checkpoint WHERE id = 1";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String filename = rs.getString("filename");
                long position = rs.getLong("position");
                String gtid = rs.getString("gtid");
                long timestamp = rs.getLong("timestamp");
                
                BinlogPositionInfo binlogPosition = new BinlogPositionInfo(filename, position, gtid, timestamp);
                logger.info("加载 Checkpoint 成功: {}", binlogPosition);
                return binlogPosition;
            }
        } catch (SQLException e) {
            logger.error("加载 Checkpoint 失败", e);
        }
        
        logger.info("未找到 Checkpoint 记录");
        return null;
    }
    
    public void saveCheckpoint(BinlogPositionInfo position) {
        if (position == null) {
            logger.warn("尝试保存空的位点信息");
            return;
        }
        
        String sql = "MERGE INTO checkpoint (id, filename, position, gtid, timestamp) " +
                "VALUES (1, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, position.getFilename());
            stmt.setLong(2, position.getPosition());
            stmt.setString(3, position.getGtid());
            stmt.setLong(4, position.getTimestamp());
            
            stmt.executeUpdate();
            logger.info("保存 Checkpoint 成功: {}", position);
        } catch (SQLException e) {
            logger.error("保存 Checkpoint 失败", e);
            throw new RuntimeException("无法保存 Checkpoint", e);
        }
    }
    
    public BinlogPositionInfo getCurrentPositionFromSource(String sourceHost, int sourcePort, 
                                                           String sourceUser, String sourcePassword) {
        String filename = null;
        long position = -1;
        String gtid = null;
        
        String url = "jdbc:mysql://" + sourceHost + ":" + sourcePort + "?useSSL=false&serverTimezone=UTC";
        
        try (Connection conn = DriverManager.getConnection(url, sourceUser, sourcePassword)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW MASTER STATUS")) {
                if (rs.next()) {
                    filename = rs.getString("File");
                    position = rs.getLong("Position");
                    logger.info("当前 binlog 位置: {}:{}", filename, position);
                }
            }
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT @@global.gtid_executed")) {
                if (rs.next()) {
                    gtid = rs.getString(1);
                    logger.info("当前 GTID: {}", gtid);
                }
            } catch (SQLException e) {
                logger.warn("获取 GTID 失败，可能未开启 GTID 模式: {}", e.getMessage());
            }
            
        } catch (SQLException e) {
            logger.error("获取 binlog position 失败", e);
            throw new RuntimeException("无法获取 binlog position", e);
        }
        
        return new BinlogPositionInfo(filename, position, gtid, System.currentTimeMillis());
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Checkpoint 数据库连接已关闭");
            } catch (SQLException e) {
                logger.error("关闭 Checkpoint 数据库连接失败", e);
            }
        }
    }
    
    public static class BinlogPositionInfo {
        private String filename;
        private long position;
        private String gtid;
        private long timestamp;
        
        public BinlogPositionInfo(String filename, long position, String gtid, long timestamp) {
            this.filename = filename;
            this.position = position;
            this.gtid = gtid;
            this.timestamp = timestamp;
        }
        
        public String getFilename() { return filename; }
        public long getPosition() { return position; }
        public String getGtid() { return gtid; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("BinlogPositionInfo{filename='%s', position=%d, gtid='%s', timestamp=%d}",
                    filename, position, gtid, timestamp);
        }
    }
}
