package com.synctask.service;

import com.synctask.dto.DatabaseInfo;
import com.synctask.dto.TableInfo;
import com.synctask.dto.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    private static final Pattern CONNECTION_PATTERN = Pattern.compile(
        "(?:mysql|postgresql)://([^:]+):([^@]+)@([^:]+):(\\d+)(?:/(.*))?"
    );

    public static class ParsedConnection {
        public String username;
        public String password;
        public String host;
        public int port;
        public String database;
        public String type;

        public ParsedConnection(String username, String password, String host, int port, String database, String type) {
            this.username = username;
            this.password = password;
            this.host = host;
            this.port = port;
            this.database = database;
            this.type = type;
        }
        
        public boolean isPostgresql() {
            return "postgresql".equals(type);
        }
    }

    public ParsedConnection parseConnection(String connectionStr) {
        if (connectionStr == null || connectionStr.isEmpty()) {
            throw new IllegalArgumentException("连接串不能为空");
        }

        String dbType = connectionStr.startsWith("postgresql://") ? "postgresql" : "mysql";

        Matcher matcher = CONNECTION_PATTERN.matcher(connectionStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("连接串格式不正确，正确格式: mysql://user:pass@host:port 或 postgresql://user:pass@host:port");
        }

        String username = matcher.group(1);
        String password = matcher.group(2);
        String host = matcher.group(3);
        int port = Integer.parseInt(matcher.group(4));
        String database = matcher.group(5);

        return new ParsedConnection(username, password, host, port, database, dbType);
    }

    public static class ConnectionTestResult {
        public boolean connected;
        public String errorType;
        public String errorMessage;
        public String suggestion;

        public ConnectionTestResult(boolean connected, String errorType, String errorMessage, String suggestion) {
            this.connected = connected;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
            this.suggestion = suggestion;
        }
    }

    public ConnectionTestResult testConnectionDetailed(String connectionStr, String expectedType) {
        ParsedConnection conn = parseConnection(connectionStr);
        boolean isPg = conn.isPostgresql();
        boolean expectPg = "postgresql".equalsIgnoreCase(expectedType);

        try {
            if (isPg) {
                Class.forName("org.postgresql.Driver");
            } else {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
        } catch (ClassNotFoundException e) {
            return new ConnectionTestResult(false, "DRIVER_NOT_FOUND", 
                "数据库驱动未找到: " + e.getMessage(), "请确保依赖中包含对应的数据库驱动");
        }

        if (expectPg && !isPg) {
            return new ConnectionTestResult(false, "DB_TYPE_MISMATCH",
                "期望PostgreSQL数据库，但连接串格式为MySQL", "请检查数据库类型是否正确");
        }
        if (!expectPg && isPg) {
            return new ConnectionTestResult(false, "DB_TYPE_MISMATCH",
                "期望MySQL数据库，但连接串格式为PostgreSQL", "请检查数据库类型是否正确");
        }

        String jdbcUrl;
        if (isPg) {
            jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?connectTimeout=15&socketTimeout=15&stringtype=unspecified",
                conn.host, conn.port, (conn.database != null && !conn.database.isEmpty()) ? conn.database : "postgres");
        } else {
            jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&connectTimeout=15000&socketTimeout=15000&allowPublicKeyRetrieval=true",
                conn.host, conn.port, (conn.database != null && !conn.database.isEmpty()) ? conn.database : "");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, conn.username, conn.password)) {
            if (connection.isValid(5)) {
                String dbTypeName = isPg ? "PostgreSQL" : "MySQL";
                return new ConnectionTestResult(true, null, dbTypeName + "连接成功", null);
            } else {
                return new ConnectionTestResult(false, "CONNECTION_FAILED", "连接验证失败", "请检查数据库服务器状态");
            }
        } catch (java.sql.SQLInvalidAuthorizationSpecException e) {
            return new ConnectionTestResult(false, "AUTH_FAILED",
                "认证失败：用户名或密码错误", "请检查用户名和密码是否正确");
        } catch (java.sql.SQLNonTransientConnectionException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Access denied") || msg.contains("authentication"))) {
                return new ConnectionTestResult(false, "AUTH_FAILED",
                    "认证失败：用户名或密码错误", "请检查用户名和密码是否正确");
            }
            return new ConnectionTestResult(false, "NETWORK_ERROR",
                "网络连接失败：" + e.getMessage(), "请检查数据库服务器地址和端口是否正确，以及网络是否可达");
        } catch (com.mysql.cj.exceptions.WrongArgumentException e) {
            return new ConnectionTestResult(false, "AUTH_FAILED",
                "认证失败：用户名或密码错误", "请检查用户名和密码是否正确");
        } catch (java.sql.SQLException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Access denied") || msg.contains("authentication") || msg.contains("password"))) {
                return new ConnectionTestResult(false, "AUTH_FAILED",
                    "认证失败：用户名或密码错误", "请检查用户名和密码是否正确");
            }
            if (msg != null && (msg.contains("Connection refused") || msg.contains("timed out") || msg.contains("timeout"))) {
                return new ConnectionTestResult(false, "NETWORK_ERROR",
                    "网络连接失败：" + e.getMessage(), "请检查数据库服务器地址和端口是否正确，以及网络是否可达");
            }
            return new ConnectionTestResult(false, "CONNECTION_FAILED",
                "连接失败：" + e.getMessage(), "请检查连接参数是否正确");
        } catch (Exception e) {
            if (e instanceof java.util.concurrent.TimeoutException || 
                (e.getCause() != null && e.getCause() instanceof java.util.concurrent.TimeoutException)) {
                return new ConnectionTestResult(false, "TIMEOUT",
                    "连接超时：20秒内未连接到数据库服务器", "请检查数据库服务器是否可达，以及防火墙设置");
            }
            return new ConnectionTestResult(false, "CONNECTION_FAILED",
                "连接失败：" + e.getMessage(), "请检查连接参数是否正确");
        }
    }

    public List<String> listSchemas(String connectionStr, String database) {
        ParsedConnection conn = parseConnection(connectionStr);
        if (!conn.isPostgresql()) {
            throw new IllegalArgumentException("listSchemas 仅支持PostgreSQL数据库");
        }

        List<String> schemas = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            String jdbcUrl = buildJdbcUrl(conn, database);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, conn.username, conn.password)) {
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(
                         "SELECT schema_name FROM information_schema.schemata " +
                         "WHERE schema_name NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                         "ORDER BY schema_name")) {
                    while (rs.next()) {
                        schemas.add(rs.getString(1));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询schema列表失败: {}", e.getMessage());
            throw new RuntimeException("查询schema列表失败: " + e.getMessage());
        }
        return schemas;
    }

    public List<TableInfo> listTables(String connectionStr, String database, String schema) {
        ParsedConnection conn = parseConnection(connectionStr);
        if (conn.isPostgresql()) {
            List<TableInfo> tables = new ArrayList<>();
            try {
                Class.forName("org.postgresql.Driver");
                String jdbcUrl = buildJdbcUrl(conn, database);
                try (Connection connection = DriverManager.getConnection(jdbcUrl, conn.username, conn.password)) {
                    String effectiveSchema = (schema != null && !schema.isEmpty()) ? schema : "public";
                    try (Statement stmt = connection.createStatement();
                         ResultSet rs = stmt.executeQuery(
                             "SELECT tablename FROM pg_tables WHERE schemaname = '" + effectiveSchema + "'")) {
                        while (rs.next()) {
                            String tableName = rs.getString(1);
                            long rows = getPgRowCount(connection, tableName);
                            tables.add(new TableInfo(tableName, rows, "", "TABLE"));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("查询表列表失败: {}", e.getMessage());
                throw new RuntimeException("查询表列表失败: " + e.getMessage());
            }
            return tables;
        }
        return listTables(connectionStr, database);
    }

    private String buildJdbcUrl(ParsedConnection conn, String database) {
        if (conn.isPostgresql()) {
            if (database != null && !database.isEmpty()) {
                return String.format("jdbc:postgresql://%s:%d/%s?currentSchema=public&stringtype=unspecified", conn.host, conn.port, database);
            }
            return String.format("jdbc:postgresql://%s:%d/?currentSchema=public&stringtype=unspecified", conn.host, conn.port);
        }
        if (database != null && !database.isEmpty()) {
            return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true", conn.host, conn.port, database);
        }
        return String.format("jdbc:mysql://%s:%d/?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true", conn.host, conn.port);
    }
    
    private String buildJdbcUrl(ParsedConnection conn) {
        return buildJdbcUrl(conn, conn.database);
    }

    public boolean testConnection(String connectionStr) {
        ParsedConnection conn = parseConnection(connectionStr);
        
        try {
            Connection connection;
            if (conn.isPostgresql()) {
                Class.forName("org.postgresql.Driver");
            } else {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
            connection = DriverManager.getConnection(
                    buildJdbcUrl(conn, null),
                    conn.username, conn.password);
            
            boolean valid = connection.isValid(5);
            connection.close();
            return valid;
        } catch (SQLException e) {
            logger.error("测试连接失败: {}", e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            logger.error("数据库驱动未找到: {}", e.getMessage());
            return false;
        }
    }

    public List<String> listDatabases(String connectionStr) {
        ParsedConnection conn = parseConnection(connectionStr);
        
        List<String> databases = new ArrayList<>();
        
        try {
            if (conn.isPostgresql()) {
                Class.forName("org.postgresql.Driver");
            } else {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
            
            try (Connection connection = DriverManager.getConnection(
                    buildJdbcUrl(conn, null),
                    conn.username, conn.password)) {
                
                if (conn.isPostgresql()) {
                    try (Statement stmt = connection.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false")) {
                        while (rs.next()) {
                            String dbName = rs.getString(1);
                            if (!isPgSystemDatabase(dbName)) {
                                databases.add(dbName);
                            }
                        }
                    }
                } else {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet rs = metaData.getCatalogs();
                    
                    while (rs.next()) {
                        String dbName = rs.getString("TABLE_CAT");
                        if (!isSystemDatabase(dbName)) {
                            databases.add(dbName);
                        }
                    }
                }
                
                logger.info("查询到 {} 个数据库", databases.size());
            }
        } catch (SQLException e) {
            logger.error("查询数据库列表失败: {}", e.getMessage());
            throw new RuntimeException("查询数据库列表失败: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("数据库驱动未找到: {}", e.getMessage());
            throw new RuntimeException("数据库驱动未找到: " + e.getMessage());
        }
        
        return databases;
    }
    
    private boolean isPgSystemDatabase(String dbName) {
        return "postgres".equalsIgnoreCase(dbName) ||
               "template0".equalsIgnoreCase(dbName) ||
               "template1".equalsIgnoreCase(dbName);
    }

    private boolean isSystemDatabase(String dbName) {
        return "information_schema".equalsIgnoreCase(dbName) ||
               "mysql".equalsIgnoreCase(dbName) ||
               "performance_schema".equalsIgnoreCase(dbName) ||
               "sys".equalsIgnoreCase(dbName);
    }

    public List<TableInfo> listTables(String connectionStr, String database) {
        ParsedConnection conn = parseConnection(connectionStr);
        
        List<TableInfo> tables = new ArrayList<>();
        
        try {
            if (conn.isPostgresql()) {
                Class.forName("org.postgresql.Driver");
            } else {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
            
            try (Connection connection = DriverManager.getConnection(
                    buildJdbcUrl(conn, database),
                    conn.username, conn.password)) {
                
                if (conn.isPostgresql()) {
                    try (Statement stmt = connection.createStatement();
                         ResultSet rs = stmt.executeQuery(
                             "SELECT tablename FROM pg_tables WHERE schemaname = 'public'")) {
                        while (rs.next()) {
                            String tableName = rs.getString(1);
                            long rows = getPgRowCount(connection, tableName);
                            tables.add(new TableInfo(tableName, rows, "", "TABLE"));
                        }
                    }
                } else {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet rs = metaData.getTables(database, null, "%", new String[]{"TABLE"});
                    
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        long rows = getRowCount(connection, database, tableName);
                        String size = getTableSize(connection, database, tableName);
                        String engine = getTableEngine(metaData, database, tableName);
                        
                        tables.add(new TableInfo(tableName, rows, size, engine));
                    }
                }
                
                logger.info("数据库 {} 查询到 {} 个表", database, tables.size());
            }
        } catch (SQLException e) {
            logger.error("查询表列表失败: {}", e.getMessage());
            throw new RuntimeException("查询表列表失败: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("数据库驱动未找到: {}", e.getMessage());
            throw new RuntimeException("数据库驱动未找到: " + e.getMessage());
        }
        
        return tables;
    }
    
    private long getPgRowCount(Connection connection, String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM \"" + tableName + "\"")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.warn("获取PG表 {} 行数失败: {}", tableName, e.getMessage());
        }
        return 0;
    }

    private long getRowCount(Connection connection, String database, String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `" + tableName + "`")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.warn("获取表 {} 行数失败: {}", tableName, e.getMessage());
        }
        return 0;
    }

    private String getTableSize(Connection connection, String database, String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT ROUND(data_length + index_length) as size_bytes " +
                 "FROM information_schema.tables " +
                 "WHERE table_schema = '" + database + "' AND table_name = '" + tableName + "'")) {
            if (rs.next()) {
                long bytes = rs.getLong("size_bytes");
                return formatSize(bytes);
            }
        } catch (SQLException e) {
            logger.warn("获取表 {} 大小失败: {}", tableName, e.getMessage());
        }
        return "0 B";
    }

    private String getTableEngine(DatabaseMetaData metaData, String database, String tableName) {
        try (ResultSet rs = metaData.getTables(database, null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                return rs.getString("TABLE_TYPE");
            }
        } catch (SQLException e) {
            logger.warn("获取表 {} 引擎失败: {}", tableName, e.getMessage());
        }
        return "UNKNOWN";
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    public DatabaseInfo getDatabaseWithTables(String connectionStr, String database) {
        DatabaseInfo dbInfo = new DatabaseInfo(database);
        
        try {
            List<TableInfo> tables = listTables(connectionStr, database);
            dbInfo.setTables(tables);
            dbInfo.setAccessible(true);
        } catch (Exception e) {
            dbInfo.setAccessible(false);
            dbInfo.setErrorMessage(e.getMessage());
            logger.error("获取数据库 {} 信息失败: {}", database, e.getMessage());
        }
        
        return dbInfo;
    }

    public ValidationResult validateForMigration(String sourceConnection, String targetConnection, 
                                                  String migrationMode, String sourceType, String targetType) {
        ValidationResult result = new ValidationResult();
        
        boolean sourceIsPg = "postgresql".equalsIgnoreCase(sourceType);
        boolean targetIsPg = "postgresql".equalsIgnoreCase(targetType);
        boolean bothMysql = !sourceIsPg && !targetIsPg;
        boolean mysqlToPg = !sourceIsPg && targetIsPg;
        boolean pgToMysql = sourceIsPg && !targetIsPg;
        
        ParsedConnection sourceConn = parseConnection(sourceConnection);
        ParsedConnection targetConn = parseConnection(targetConnection);
        
        try (Connection sourceDb = DriverManager.getConnection(
                buildJdbcUrl(sourceConn, sourceIsPg),
                sourceConn.username, sourceConn.password);
             Connection targetDb = DriverManager.getConnection(
                buildJdbcUrl(targetConn, targetIsPg),
                targetConn.username, targetConn.password)) {
            
            result.addItem("源库连接", "源数据库连接检查", true, 
                sourceIsPg ? "PostgreSQL源库连接成功" : "MySQL源库连接成功", "info");
            result.addItem("目标库连接", "目标数据库连接检查", true, 
                targetIsPg ? "PostgreSQL目标库连接成功" : "MySQL目标库连接成功", "info");
            
            if (bothMysql) {
                String sourceVersion = getMySQLVersion(sourceDb);
                String targetVersion = getMySQLVersion(targetDb);
                
                if ("fullAndIncre".equals(migrationMode) || "increment".equals(migrationMode)) {
                    checkBinlogEnabled(sourceDb, result);
                    checkBinlogFormat(sourceDb, result);
                    checkBinlogRowImage(sourceDb, result);
                    checkServerId(sourceDb, sourceVersion, result);
                }
                
                checkVersionCompatibility(sourceVersion, targetVersion, result);
                checkSqlModeCompatibility(sourceDb, targetDb, result);
                checkSourcePermissions(sourceDb, migrationMode, result);
                checkTargetPermissions(targetDb, targetVersion, result);
            }
            
            if (mysqlToPg) {
                if ("fullAndIncre".equals(migrationMode) || "increment".equals(migrationMode)) {
                    checkBinlogEnabled(sourceDb, result);
                    checkBinlogFormat(sourceDb, result);
                    checkBinlogRowImage(sourceDb, result);
                }
            }
            
            if (pgToMysql) {
                // PG→MySQL: 检查源端PG的WAL配置
                checkPgWalConfig(sourceDb, result);
            }
            
        } catch (SQLException e) {
            logger.error("数据库校验失败: {}", e.getMessage());
            if (!result.getCheckItems().stream().anyMatch(item -> !item.isPassed())) {
                result.addItem("连接检查", "数据库连接检查", false, "连接失败: " + e.getMessage(), "error");
            }
        }
        
        boolean allPassed = result.getCheckItems().stream().allMatch(ValidationResult.CheckItem::isPassed);
        result.setAllPassed(allPassed);
        
        return result;
    }
    
    private void checkPgWalConfig(Connection conn, ValidationResult result) {
        try {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW wal_level")) {
                if (rs.next()) {
                    String walLevel = rs.getString(1);
                    boolean passed = "logical".equalsIgnoreCase(walLevel);
                    String message = passed ? "WAL级别为logical，支持增量同步" : 
                        "WAL级别为" + walLevel + "，增量同步需要设置为logical";
                    result.addItem("WAL级别", "增量同步需要源数据库WAL级别为logical", passed, message, "error");
                }
            }
        } catch (SQLException e) {
            result.addItem("WAL级别", "增量同步需要源数据库WAL级别为logical", false, 
                "检查失败: " + e.getMessage(), "warning");
        }
    }
    
    private String buildJdbcUrl(ParsedConnection conn, boolean isPg) {
        if (isPg) {
            return String.format("jdbc:postgresql://%s:%d/%s?stringtype=unspecified", 
                conn.host, conn.port, conn.database);
        }
        return buildJdbcUrl(conn);
    }

    public ValidationResult validateForMigration(String sourceConnection, String targetConnection, String migrationMode) {
        ValidationResult result = new ValidationResult();
        
        ParsedConnection sourceConn = parseConnection(sourceConnection);
        ParsedConnection targetConn = parseConnection(targetConnection);
        
        try (Connection sourceDb = DriverManager.getConnection(
                buildJdbcUrl(sourceConn),
                sourceConn.username, sourceConn.password);
             Connection targetDb = DriverManager.getConnection(
                buildJdbcUrl(targetConn),
                targetConn.username, targetConn.password)) {
            
            String sourceVersion = getMySQLVersion(sourceDb);
            String targetVersion = getMySQLVersion(targetDb);
            
            if ("fullAndIncre".equals(migrationMode) || "increment".equals(migrationMode)) {
                checkBinlogEnabled(sourceDb, result);
                checkBinlogFormat(sourceDb, result);
                checkBinlogRowImage(sourceDb, result);
                checkServerId(sourceDb, sourceVersion, result);
            }
            
            checkVersionCompatibility(sourceVersion, targetVersion, result);
            checkSqlModeCompatibility(sourceDb, targetDb, result);
            checkSourcePermissions(sourceDb, migrationMode, result);
            checkTargetPermissions(targetDb, targetVersion, result);
            
        } catch (SQLException e) {
            logger.error("数据库校验失败: {}", e.getMessage());
            result.addItem("连接检查", "数据库连接检查", false, "连接失败: " + e.getMessage(), "error");
        }
        
        boolean allPassed = result.getCheckItems().stream().allMatch(ValidationResult.CheckItem::isPassed);
        result.setAllPassed(allPassed);
        
        return result;
    }

    private String getMySQLVersion(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
            if (rs.next()) {
                return rs.getString(1);
            }
        }
        return "unknown";
    }

    private void checkBinlogEnabled(Connection conn, ValidationResult result) {
        try {
            String logBin = getVariable(conn, "log_bin");
            boolean passed = "ON".equalsIgnoreCase(logBin) || "1".equals(logBin);
            String message = passed ? "Binlog已开启" : "Binlog未开启，增量同步需要开启binlog";
            result.addItem("Binlog开启状态", "增量同步需要源数据库开启binlog", passed, message, "error");
        } catch (SQLException e) {
            result.addItem("Binlog开启状态", "增量同步需要源数据库开启binlog", false, "检查失败: " + e.getMessage(), "error");
        }
    }

    private void checkBinlogFormat(Connection conn, ValidationResult result) {
        try {
            String format = getVariable(conn, "binlog_format");
            boolean passed = "ROW".equalsIgnoreCase(format);
            String message = passed ? "Binlog格式为ROW" : "当前Binlog格式为" + format + "，需要设置为ROW";
            result.addItem("Binlog格式", "增量同步需要Binlog格式为ROW", passed, message, "error");
        } catch (SQLException e) {
            result.addItem("Binlog格式", "增量同步需要Binlog格式为ROW", false, "检查失败: " + e.getMessage(), "error");
        }
    }

    private void checkBinlogRowImage(Connection conn, ValidationResult result) {
        try {
            String rowImage = getVariable(conn, "binlog_row_image");
            if (rowImage == null || rowImage.isEmpty()) {
                result.addItem("Binlog Row Image", "binlog_row_image需设置为FULL", true, "参数不存在(可能是MySQL 5.5及以下版本)，跳过检查", "warning");
                return;
            }
            boolean passed = "FULL".equalsIgnoreCase(rowImage);
            String message = passed ? "binlog_row_image为FULL" : "当前binlog_row_image为" + rowImage + "，需要设置为FULL";
            result.addItem("Binlog Row Image", "binlog_row_image需设置为FULL", passed, message, "error");
        } catch (SQLException e) {
            result.addItem("Binlog Row Image", "binlog_row_image需设置为FULL", false, "检查失败: " + e.getMessage(), "error");
        }
    }

    private void checkServerId(Connection conn, String version, ValidationResult result) {
        try {
            String serverIdStr = getVariable(conn, "server_id");
            if (serverIdStr == null || serverIdStr.isEmpty() || "0".equals(serverIdStr)) {
                result.addItem("Server ID", "增量同步需要设置server_id", false, "server_id未设置或为0", "error");
                return;
            }
            
            long serverId = Long.parseLong(serverIdStr);
            boolean passed;
            String message;
            
            if (isVersionAtLeast(version, "5.7.0")) {
                passed = serverId >= 1 && serverId <= 4294967296L;
                message = passed ? "server_id=" + serverId + "，符合要求" : "server_id=" + serverId + "，MySQL 5.7+需要设置在1-4294967296之间";
            } else {
                passed = serverId >= 2 && serverId <= 4294967296L;
                message = passed ? "server_id=" + serverId + "，符合要求" : "server_id=" + serverId + "，MySQL 5.6及以下需要设置在2-4294967296之间";
            }
            
            result.addItem("Server ID", "增量同步需要设置server_id", passed, message, "error");
        } catch (Exception e) {
            result.addItem("Server ID", "增量同步需要设置server_id", false, "检查失败: " + e.getMessage(), "error");
        }
    }

    private void checkVersionCompatibility(String sourceVersion, String targetVersion, ValidationResult result) {
        boolean passed = compareVersions(sourceVersion, targetVersion) <= 0;
        String message = passed 
            ? "源数据库版本(" + sourceVersion + ") <= 目标数据库版本(" + targetVersion + ")" 
            : "源数据库版本(" + sourceVersion + ") > 目标数据库版本(" + targetVersion + ")，可能导致兼容性问题";
        result.addItem("版本兼容性", "源数据库版本不能高于目标数据库版本", passed, message, "error");
    }

    private void checkSqlModeCompatibility(Connection sourceDb, Connection targetDb, ValidationResult result) {
        try {
            String sourceSqlMode = getVariable(sourceDb, "sql_mode");
            String targetSqlMode = getVariable(targetDb, "sql_mode");
            
            Set<String> sourceModes = parseSqlMode(sourceSqlMode);
            Set<String> targetModes = parseSqlMode(targetSqlMode);
            
            boolean passed = sourceModes.equals(targetModes);
            String message = passed 
                ? "sql_mode一致" 
                : "源数据库sql_mode(" + sourceSqlMode + ")与目标数据库(" + targetSqlMode + ")不一致";
            result.addItem("SQL Mode一致性", "源和目标数据库sql_mode需要一致", passed, message, "warning");
        } catch (SQLException e) {
            result.addItem("SQL Mode一致性", "源和目标数据库sql_mode需要一致", false, "检查失败: " + e.getMessage(), "error");
        }
    }

    private void checkSourcePermissions(Connection conn, String migrationMode, ValidationResult result) {
        try {
            Set<String> grantedPrivileges = getGrantedPrivileges(conn);
            
            Set<String> requiredPrivileges = new HashSet<>(Arrays.asList("SELECT", "SHOW VIEW", "EVENT"));
            
            if ("fullAndIncre".equals(migrationMode) || "increment".equals(migrationMode)) {
                requiredPrivileges.addAll(Arrays.asList("LOCK TABLES", "REPLICATION SLAVE", "REPLICATION CLIENT"));
            }
            
            Set<String> missingPrivileges = new HashSet<>();
            for (String priv : requiredPrivileges) {
                boolean hasPriv = grantedPrivileges.contains(priv) || 
                                  grantedPrivileges.contains("ALL PRIVILEGES") ||
                                  grantedPrivileges.contains("ALL");
                if (!hasPriv) {
                    missingPrivileges.add(priv);
                }
            }
            
            boolean passed = missingPrivileges.isEmpty();
            String mode = "fullAndIncre".equals(migrationMode) ? "全量+增量" : "full".equals(migrationMode) ? "全量" : "增量";
            String message = passed 
                ? mode + "同步所需权限已具备" 
                : mode + "同步缺少权限: " + String.join(", ", missingPrivileges);
            result.addItem("源数据库权限", mode + "同步需要SELECT、SHOW VIEW、EVENT等权限", passed, message, "error");
        } catch (SQLException e) {
            result.addItem("源数据库权限", "检查源数据库账号权限", false, "检查失败: " + e.getMessage(), "error");
        }
    }

    private void checkTargetPermissions(Connection conn, String targetVersion, ValidationResult result) {
        try {
            Set<String> grantedPrivileges = getGrantedPrivileges(conn);
            
            Set<String> requiredPrivileges = new HashSet<>(Arrays.asList(
                "SELECT", "CREATE", "DROP", "DELETE", "INSERT", "UPDATE", "ALTER", 
                "CREATE VIEW", "CREATE ROUTINE", "REFERENCES"
            ));
            
            if (isVersionInRange(targetVersion, "8.0.14", "8.0.18")) {
                requiredPrivileges.add("SESSION_VARIABLES_ADMIN");
            }
            
            Set<String> missingPrivileges = new HashSet<>();
            for (String priv : requiredPrivileges) {
                boolean hasPriv = grantedPrivileges.contains(priv) || 
                                  grantedPrivileges.contains("ALL PRIVILEGES") ||
                                  grantedPrivileges.contains("ALL");
                if (!hasPriv) {
                    missingPrivileges.add(priv);
                }
            }
            
            boolean passed = missingPrivileges.isEmpty();
            String message = passed 
                ? "目标数据库所需权限已具备" 
                : "目标数据库缺少权限: " + String.join(", ", missingPrivileges);
            result.addItem("目标数据库权限", "目标数据库需要SELECT、CREATE、DROP、INSERT、UPDATE、ALTER等权限", passed, message, "error");
        } catch (SQLException e) {
            result.addItem("目标数据库权限", "检查目标数据库账号权限", false, "检查失败: " + e.getMessage(), "error");
        }
    }

    private String getVariable(Connection conn, String variableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE '" + variableName + "'")) {
            if (rs.next()) {
                return rs.getString("Value");
            }
        }
        return null;
    }

    private Set<String> getGrantedPrivileges(Connection conn) throws SQLException {
        Set<String> privileges = new HashSet<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW GRANTS")) {
            while (rs.next()) {
                String grant = rs.getString(1);
                Set<String> extracted = parseGrants(grant);
                privileges.addAll(extracted);
            }
        }
        
        return privileges;
    }

    private Set<String> parseGrants(String grant) {
        Set<String> privileges = new HashSet<>();
        
        int start = grant.indexOf("GRANT ");
        int end = grant.indexOf(" ON ");
        if (start >= 0 && end > start) {
            String privStr = grant.substring(start + 6, end).trim();
            String[] privs = privStr.split(",");
            for (String priv : privs) {
                privileges.add(priv.trim().toUpperCase());
            }
        }
        
        return privileges;
    }

    private Set<String> parseSqlMode(String sqlMode) {
        Set<String> modes = new HashSet<>();
        if (sqlMode != null && !sqlMode.isEmpty()) {
            String[] parts = sqlMode.split(",");
            for (String part : parts) {
                modes.add(part.trim().toUpperCase());
            }
        }
        return modes;
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9].*", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean isVersionAtLeast(String version, String minVersion) {
        return compareVersions(version, minVersion) >= 0;
    }

    private boolean isVersionInRange(String version, String minVersion, String maxVersion) {
        return compareVersions(version, minVersion) >= 0 && compareVersions(version, maxVersion) <= 0;
    }
}
