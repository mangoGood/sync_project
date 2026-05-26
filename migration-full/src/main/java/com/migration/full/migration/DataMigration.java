package com.migration.full.migration;

import com.migration.db.DatabaseConnection;
import com.migration.model.ColumnInfo;
import com.migration.model.TableInfo;
import com.migration.model.TypeMapper;
import com.migration.full.progress.MigrationProgress;
import com.migration.full.progress.ProgressManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataMigration {
    private static final Logger logger = LoggerFactory.getLogger(DataMigration.class);
    
    private DatabaseConnection sourceConnection;
    private DatabaseConnection targetConnection;
    private int batchSize;
    private boolean continueOnError;
    private ProgressManager progressManager;
    private boolean isPostgresql;
    private boolean sourceIsPostgresql;
    private boolean targetIsPostgresql;

    public DataMigration(DatabaseConnection sourceConnection, DatabaseConnection targetConnection, 
                        int batchSize, boolean continueOnError, ProgressManager progressManager) {
        this.sourceConnection = sourceConnection;
        this.targetConnection = targetConnection;
        this.batchSize = batchSize;
        this.continueOnError = continueOnError;
        this.progressManager = progressManager;
        this.sourceIsPostgresql = "postgresql".equalsIgnoreCase(sourceConnection.getConfig().getDbType());
        this.targetIsPostgresql = "postgresql".equalsIgnoreCase(targetConnection.getConfig().getDbType());
        this.isPostgresql = targetIsPostgresql;
    }

    public void migrateAllData(List<TableInfo> tables) throws SQLException {
        logger.info("开始迁移数据，共 {} 个表", tables.size());
        
        int totalSuccessCount = 0;
        int totalFailCount = 0;
        
        for (TableInfo table : tables) {
            try {
                int[] result = migrateTableData(table);
                totalSuccessCount += result[0];
                totalFailCount += result[1];
                logger.info("表 {} 数据迁移完成，成功: {}, 失败: {}", 
                           table.getTableName(), result[0], result[1]);
            } catch (SQLException e) {
                logger.error("表 {} 数据迁移失败", table.getTableName(), e);
                if (progressManager != null && progressManager.isEnabled()) {
                    progressManager.failMigration(table.getTableName(), e.getMessage());
                }
                if (!continueOnError) {
                    throw e;
                }
            }
        }
        
        logger.info("数据迁移完成，总成功: {}, 总失败: {}", totalSuccessCount, totalFailCount);
    }

    public int[] migrateTableData(TableInfo table) throws SQLException {
        String tableName = table.getTableName();
        
        long totalRows = getTableRowCount(tableName);
        logger.info("开始迁移表 {} 的数据，总行数: {}", tableName, totalRows);
        
        if (totalRows == 0) {
            logger.info("表 {} 没有数据，跳过", tableName);
            return new int[]{0, 0};
        }
        
        List<String> columns = getColumnNames(table);
        String columnList = String.join(", ", columns);
        
        String primaryKeyColumn = getPrimaryKeyColumn(table);
        
        return migrateDataBatch(table, columnList, totalRows, primaryKeyColumn);
    }

    private int[] migrateDataBatch(TableInfo table, String columnList, long totalRows, String primaryKeyColumn) throws SQLException {
        String tableName = table.getTableName();
        int successCount = 0;
        int failCount = 0;
        
        MigrationProgress progress = null;
        Long lastMigratedId = null;
        long startOffset = 0;
        
        if (progressManager != null && progressManager.isEnabled()) {
            try {
                progress = progressManager.startMigration(tableName, totalRows);
                if (progress != null && progress.getLastMigratedId() != 0) {
                    lastMigratedId = progress.getLastMigratedId();
                    startOffset = progress.getMigratedRows();
                    logger.info("从上次中断位置继续迁移，已迁移: {}, 最后ID: {}", startOffset, lastMigratedId);
                }
            } catch (SQLException e) {
                logger.error("获取迁移进度失败", e);
            }
        }
        
        String selectSql;
        String sourceQuoteColumnList = buildSourceQuotedColumnList(table);
        if (lastMigratedId != null && primaryKeyColumn != null) {
            selectSql = "SELECT " + sourceQuoteColumnList + " FROM " + sourceQuoteIdentifier(tableName) + 
                       " WHERE " + sourceQuoteIdentifier(primaryKeyColumn) + " > ? ORDER BY " + sourceQuoteIdentifier(primaryKeyColumn);
        } else {
            selectSql = "SELECT " + sourceQuoteColumnList + " FROM " + sourceQuoteIdentifier(tableName);
        }
        
        String insertSql = "INSERT INTO " + quoteIdentifier(tableName) + " (" + columnList + ") VALUES (" + 
                          String.join(", ", createPlaceholders(table.getColumns().size())) + ")";
        
        Connection sourceConn = sourceConnection.getConnection();
        PreparedStatement selectStmt = sourceConn.prepareStatement(selectSql);
        Connection targetConn = targetConnection.getConnection();
        PreparedStatement insertStmt = targetConn.prepareStatement(insertSql);
        
        try {
            if (lastMigratedId != null && primaryKeyColumn != null) {
                selectStmt.setLong(1, lastMigratedId);
            }
            
            ResultSet rs = selectStmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            int batchCount = 0;
            long processedRows = startOffset;
            Long currentLastId = lastMigratedId;
            
            while (rs.next()) {
                try {
                    if (targetConn.isClosed()) {
                        logger.warn("目标数据库连接已关闭，重新建立连接");
                        targetConn = targetConnection.getConnection();
                        insertStmt = targetConn.prepareStatement(insertSql);
                    }
                    
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = readColumnValue(rs, i, metaData, table);

                        if (sourceIsPostgresql && !targetIsPostgresql) {
                            value = convertPgToMysqlValue(value, metaData.getColumnTypeName(i), rs, i);
                        } else if (!sourceIsPostgresql && targetIsPostgresql) {
                            value = convertMysqlToPgValue(value, metaData.getColumnTypeName(i));
                        }

                        insertStmt.setObject(i, value);
                    }
                    
                    if (primaryKeyColumn != null) {
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            if (columnName.equals(primaryKeyColumn)) {
                                Object idValue = rs.getObject(i);
                                if (idValue instanceof Number) {
                                    currentLastId = ((Number) idValue).longValue();
                                }
                                break;
                            }
                        }
                    }
                    
                    insertStmt.addBatch();
                    batchCount++;
                    
                    if (batchCount >= batchSize) {
                        int[] results = insertStmt.executeBatch();
                        successCount += countSuccess(results);
                        failCount += countFailures(results);
                        batchCount = 0;
                        
                        if (progressManager != null && progressManager.isEnabled()) {
                            progressManager.updateProgress(tableName, processedRows, currentLastId);
                        }
                    }
                    
                    processedRows++;
                    
                    if (processedRows % 10000 == 0) {
                        logger.info("表 {} 已处理 {}/{} 行", tableName, processedRows, totalRows);
                    }
                    
                } catch (SQLException e) {
                    if (isDuplicateKeyError(e)) {
                        logger.warn("主键冲突，跳过该行，表: {}, 行: {}", tableName, processedRows);
                    } else {
                        failCount++;
                        logger.error("插入数据失败，表: {}, 行: {}", tableName, processedRows, e);
                        
                        if (progressManager != null && progressManager.isEnabled()) {
                            try {
                                progressManager.updateProgress(tableName, processedRows, currentLastId);
                            } catch (SQLException ex) {
                                logger.error("更新进度失败", ex);
                            }
                        }
                        
                        if (!continueOnError) {
                            throw e;
                        }
                        
                        try {
                            if (targetConn.isClosed()) {
                                targetConn = targetConnection.getConnection();
                            }
                            insertStmt = targetConn.prepareStatement(insertSql);
                        } catch (SQLException ex2) {
                            logger.error("重建目标连接失败", ex2);
                        }
                    }
                }
            }
            
            if (batchCount > 0) {
                try {
                    int[] results = insertStmt.executeBatch();
                    successCount += countSuccess(results);
                    failCount += countFailures(results);
                    
                    if (progressManager != null && progressManager.isEnabled()) {
                        progressManager.updateProgress(tableName, processedRows, currentLastId);
                    }
                } catch (SQLException e) {
                    if (isDuplicateKeyError(e)) {
                        logger.warn("批处理中存在主键冲突，表: {}", tableName);
                    } else {
                        logger.error("执行最后一批数据失败，表: {}", tableName, e);
                        if (!continueOnError) {
                            throw e;
                        }
                    }
                }
            }
            
            logger.info("表 {} 数据迁移完成，成功: {}, 失败: {}", tableName, successCount, failCount);
            
            if (progressManager != null && progressManager.isEnabled()) {
                try {
                    progressManager.completeMigration(tableName);
                } catch (SQLException e) {
                    logger.error("标记迁移完成失败", e);
                }
            }
            
            rs.close();
        } finally {
            try { selectStmt.close(); } catch (SQLException e) { /* ignore */ }
            try { insertStmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        
        return new int[]{successCount, failCount};
    }

    private Object readColumnValue(ResultSet rs, int i, ResultSetMetaData metaData, TableInfo table) throws SQLException {
        int columnType = metaData.getColumnType(i);
        String columnTypeName = metaData.getColumnTypeName(i);

        if (sourceIsPostgresql && columnTypeName != null) {
            String lowerType = columnTypeName.toLowerCase().trim();
            if ("json".equals(lowerType) || "jsonb".equals(lowerType)) {
                return rs.getString(i);
            }
            if ("bytea".equals(lowerType)) {
                return rs.getBytes(i);
            }
            if ("uuid".equals(lowerType)) {
                return rs.getString(i);
            }
            if (lowerType.endsWith("[]")) {
                return rs.getString(i);
            }
        }

        if (columnType == Types.TIME) {
            return rs.getString(i);
        } else if (columnType == Types.BIGINT && columnTypeName != null
                && columnTypeName.toLowerCase().contains("unsigned")) {
            return rs.getBigDecimal(i);
        } else if (columnType == Types.DATE && "YEAR".equalsIgnoreCase(columnTypeName)) {
            Object value = rs.getObject(i);
            if (value instanceof java.sql.Date) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime((java.sql.Date) value);
                return cal.get(java.util.Calendar.YEAR);
            } else if (value == null) {
                return null;
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return value;
        } else {
            return rs.getObject(i);
        }
    }

    private Object convertPgToMysqlValue(Object value, String pgTypeName, ResultSet rs, int colIndex) throws SQLException {
        if (value == null) {
            return null;
        }

        String lowerType = pgTypeName.toLowerCase().trim();

        if (TypeMapper.isPgBooleanType(lowerType)) {
            if (value instanceof Boolean) {
                return ((Boolean) value) ? 1 : 0;
            }
            if (value instanceof String) {
                String strVal = ((String) value).trim().toLowerCase();
                if ("t".equals(strVal) || "true".equals(strVal) || "1".equals(strVal)) return 1;
                if ("f".equals(strVal) || "false".equals(strVal) || "0".equals(strVal)) return 0;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0 ? 1 : 0;
            }
            return 0;
        }

        if (TypeMapper.isPgArrayType(lowerType)) {
            if (value instanceof java.sql.Array) {
                Object[] array = (Object[]) ((java.sql.Array) value).getArray();
                return Arrays.toString(array);
            }
            if (value instanceof String) {
                return value;
            }
            return value.toString();
        }

        if (TypeMapper.isPgUuidType(lowerType)) {
            return value.toString();
        }

        if (TypeMapper.isPgJsonbType(lowerType) || TypeMapper.isPgJsonType(lowerType)) {
            return value.toString();
        }

        if (TypeMapper.isPgTimestampTzType(lowerType)) {
            if (value instanceof java.sql.Timestamp) {
                return value;
            }
            return value.toString();
        }

        if (TypeMapper.isPgTimetzType(lowerType)) {
            if (value instanceof java.sql.Time) {
                return value;
            }
            if (value instanceof String) {
                String strVal = (String) value;
                int plusIdx = strVal.lastIndexOf('+');
                int minusIdx = strVal.lastIndexOf('-');
                int tzIdx = -1;
                if (plusIdx > 0) tzIdx = plusIdx;
                else if (minusIdx > strVal.indexOf(':')) tzIdx = minusIdx;
                if (tzIdx > 0) {
                    return strVal.substring(0, tzIdx).trim();
                }
                return strVal;
            }
            return value.toString();
        }

        if (TypeMapper.isPgIntervalType(lowerType)) {
            return value.toString();
        }

        if (TypeMapper.isPgNetworkType(lowerType)) {
            return value.toString();
        }

        if (TypeMapper.isPgGeometryType(lowerType)) {
            return value.toString();
        }

        if (lowerType.equals("bytea")) {
            if (value instanceof byte[]) {
                return value;
            }
            return value.toString();
        }

        if (lowerType.equals("serial") || lowerType.equals("bigserial")) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return value;
        }

        return value;
    }

    private String buildSourceQuotedColumnList(TableInfo table) {
        List<String> columns = new ArrayList<>();
        for (ColumnInfo column : table.getColumns()) {
            columns.add(sourceQuoteIdentifier(column.getColumnName()));
        }
        return String.join(", ", columns);
    }

    private long getTableRowCount(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + sourceQuoteIdentifier(tableName);
        
        try (Statement stmt = sourceConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        
        return 0;
    }

    private List<String> getColumnNames(TableInfo table) {
        List<String> columns = new ArrayList<>();
        for (var column : table.getColumns()) {
            columns.add(quoteIdentifier(column.getColumnName()));
        }
        return columns;
    }

    private String getPrimaryKeyColumn(TableInfo table) {
        for (var column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                return column.getColumnName();
            }
        }
        return null;
    }

    private String[] createPlaceholders(int count) {
        String[] placeholders = new String[count];
        for (int i = 0; i < count; i++) {
            placeholders[i] = "?";
        }
        return placeholders;
    }

    private int countSuccess(int[] results) {
        int count = 0;
        for (int result : results) {
            if (result >= 0) {
                count++;
            }
        }
        return count;
    }

    private int countFailures(int[] results) {
        int count = 0;
        for (int result : results) {
            if (result < 0) {
                count++;
            }
        }
        return count;
    }
    
    private boolean isDuplicateKeyError(SQLException e) {
        int errorCode = e.getErrorCode();
        String sqlState = e.getSQLState();
        
        if (errorCode == 1062 || "23000".equals(sqlState)) {
            return true;
        }
        
        String message = e.getMessage();
        if (message != null && (message.contains("Duplicate entry") || 
            message.contains("duplicate key value") || 
            message.contains("PRIMARY") || message.contains("UNIQUE"))) {
            return true;
        }
        
        return false;
    }

    private String quoteIdentifier(String identifier) {
        if (isPostgresql) {
            return "\"" + identifier + "\"";
        }
        return "`" + identifier + "`";
    }

    private Object convertMysqlToPgValue(Object value, String mysqlTypeName) {
        if (value == null) {
            return null;
        }

        if (mysqlTypeName == null) {
            return value;
        }

        String lowerType = mysqlTypeName.toLowerCase().trim();

        if (lowerType.startsWith("tinyint") && lowerType.contains("(1)")) {
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
            if (value instanceof Boolean) {
                return value;
            }
        }

        if (lowerType.startsWith("json")) {
            return value.toString();
        }

        if (lowerType.startsWith("year")) {
            if (value instanceof java.sql.Date) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime((java.sql.Date) value);
                return cal.get(java.util.Calendar.YEAR);
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }

        if (lowerType.startsWith("datetime") || lowerType.startsWith("timestamp")) {
            if (value instanceof java.sql.Timestamp) {
                return value;
            }
            if (value instanceof java.util.Date) {
                return new java.sql.Timestamp(((java.util.Date) value).getTime());
            }
        }

        return value;
    }

    private String sourceQuoteIdentifier(String identifier) {
        if (sourceIsPostgresql) {
            return "\"" + identifier + "\"";
        }
        return "`" + identifier + "`";
    }
}
