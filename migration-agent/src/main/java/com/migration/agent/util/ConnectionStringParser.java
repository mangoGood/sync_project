package com.migration.agent.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionStringParser {
    private static final Pattern MYSQL_PATTERN = 
        Pattern.compile("mysql://([^:]+):([^@]+)@([^:]+):(\\d+)(?:/(.+))?");
    
    private static final Pattern PG_PATTERN = 
        Pattern.compile("postgresql://([^:]+):([^@]+)@([^:]+):(\\d+)(?:/(.+))?");
    
    private static final Pattern JDBC_MYSQL_PATTERN =
        Pattern.compile("jdbc:mysql://([^:]+):(\\d+)/([^?]+)(?:\\?.*)?");
    
    private static final Pattern JDBC_PG_PATTERN =
        Pattern.compile("jdbc:postgresql://([^:]+):(\\d+)/([^?]+)(?:\\?.*)?");

    public static class ConnectionInfo {
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;
        private String type = "mysql";
        
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public String getDatabase() {
            return database;
        }
        
        public void setDatabase(String database) {
            this.database = database;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        @Override
        public String toString() {
            String protocol = "postgresql".equals(type) ? "postgresql" : "mysql";
            return String.format("%s://%s:***@%s:%d/%s", protocol, username, host, port, database);
        }
    }
    
    public static ConnectionInfo parse(String connectionString) {
        if (connectionString == null || connectionString.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = connectionString.trim();
        
        if (trimmed.startsWith("jdbc:postgresql://")) {
            return parseJdbcUrl(trimmed, JDBC_PG_PATTERN, "postgresql");
        }
        if (trimmed.startsWith("jdbc:mysql://")) {
            return parseJdbcUrl(trimmed, MYSQL_PATTERN, "mysql");
        }
        
        Pattern pattern;
        if (trimmed.startsWith("postgresql://")) {
            pattern = PG_PATTERN;
        } else {
            pattern = MYSQL_PATTERN;
        }
        
        Matcher matcher = pattern.matcher(trimmed);
        
        if (!matcher.matches()) {
            String expectedFormat = pattern == PG_PATTERN 
                ? "postgresql://user:pass@host:port/db" 
                : "mysql://user:pass@host:port/db";
            throw new IllegalArgumentException("Invalid connection string format. Expected: " + expectedFormat);
        }
        
        ConnectionInfo info = new ConnectionInfo();
        info.setUsername(matcher.group(1));
        info.setPassword(matcher.group(2));
        info.setHost(matcher.group(3));
        info.setPort(Integer.parseInt(matcher.group(4)));
        info.setDatabase(matcher.group(5) != null ? matcher.group(5) : "");
        info.setType(trimmed.startsWith("postgresql://") ? "postgresql" : "mysql");
        
        return info;
    }

    private static ConnectionInfo parseJdbcUrl(String url, Pattern pattern, String dbType) {
        Pattern jdbcPattern = "postgresql".equals(dbType) ? JDBC_PG_PATTERN : JDBC_MYSQL_PATTERN;
        Matcher matcher = jdbcPattern.matcher(url);
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid JDBC connection string format: " + url);
        }
        
        ConnectionInfo info = new ConnectionInfo();
        info.setHost(matcher.group(1));
        info.setPort(Integer.parseInt(matcher.group(2)));
        info.setDatabase(matcher.group(3));
        info.setType(dbType);

        String queryPart = "";
        int queryIdx = url.indexOf('?');
        if (queryIdx >= 0) {
            queryPart = url.substring(queryIdx + 1);
        }
        
        for (String param : queryPart.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();
                if ("user".equals(key) || "username".equals(key)) {
                    info.setUsername(value);
                } else if ("password".equals(key)) {
                    info.setPassword(value);
                }
            }
        }
        
        return info;
    }
}
