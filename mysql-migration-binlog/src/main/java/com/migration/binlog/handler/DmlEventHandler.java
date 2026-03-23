package com.migration.binlog.handler;

import com.migration.binlog.core.BinlogEvent;
import com.migration.binlog.core.BinlogPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.List;
import java.util.Map;

/**
 * DML 事件处理器
 * 处理 INSERT、UPDATE、DELETE 语句，将 SQL 写入文件
 */
public class DmlEventHandler implements BinlogEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(DmlEventHandler.class);

    private SqlFileManager sqlFileManager;

    public DmlEventHandler(String outputDirectory) {
        this.sqlFileManager = new SqlFileManager(outputDirectory);
    }

    @Override
    public boolean handle(BinlogEvent event) {
        if (!supports(event)) {
            return false;
        }

        BinlogEvent.DmlEventData dmlData = (BinlogEvent.DmlEventData) event.getData();
        String database = event.getDatabase();
        String table = event.getTable();
        BinlogPosition position = event.getPosition();

        logger.info("处理 DML 事件: {}.{}, type={}, position={}", database, table, dmlData.getDmlType(), position);

        try {
            switch (dmlData.getDmlType()) {
                case INSERT:
                    return handleInsert(database, table, dmlData.getAfterRows(), position);
                case UPDATE:
                    return handleUpdate(database, table, dmlData.getBeforeRows(), dmlData.getAfterRows(), position);
                case DELETE:
                    return handleDelete(database, table, dmlData.getBeforeRows(), position);
                default:
                    logger.warn("未知的 DML 类型: {}", dmlData.getDmlType());
                    return false;
            }
        } catch (Exception e) {
            logger.error("处理 DML 事件失败: {}.{}, type={}", database, table, dmlData.getDmlType(), e);
            return false;
        }
    }

    @Override
    public boolean supports(BinlogEvent event) {
        return event != null && event.isDmlEvent();
    }

    /**
     * 处理 INSERT 操作
     */
    private boolean handleInsert(String database, String table, List<Map<String, Serializable>> rows, BinlogPosition position) {
        if (rows == null || rows.isEmpty()) {
            logger.warn("rows 为空或 null");
            return true;
        }

        String fullTableName = database + "." + table;

        for (Map<String, Serializable> row : rows) {
            logger.debug("处理行: {}", row);
            
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(fullTableName).append(" (");

            // 构建列名
            String[] columns = row.keySet().toArray(new String[0]);
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(columns[i]);
            }

            sql.append(") VALUES (");

            // 构建值
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(formatValue(row.get(columns[i])));
            }
            sql.append(")");

            String sqlStr = sql.toString();
            logger.debug("生成的 SQL: {}", sqlStr);
            
            // 写入 SQL 文件（带位置信息）
            sqlFileManager.writeSql(sqlStr, position);
        }

        logger.debug("已处理 INSERT: {}.{}, 行数: {}", database, table, rows.size());
        return true;
    }

    /**
     * 处理 UPDATE 操作
     */
    private boolean handleUpdate(String database, String table,
                                 List<Map<String, Serializable>> beforeRows,
                                 List<Map<String, Serializable>> afterRows,
                                 BinlogPosition position) {
        if (beforeRows == null || afterRows == null || beforeRows.isEmpty()) {
            return true;
        }

        String fullTableName = database + "." + table;

        for (int i = 0; i < afterRows.size(); i++) {
            Map<String, Serializable> beforeRow = beforeRows.get(i);
            Map<String, Serializable> afterRow = afterRows.get(i);

            StringBuilder sql = new StringBuilder("UPDATE ");
            sql.append(fullTableName).append(" SET ");

            // 构建 SET 子句
            String[] columns = afterRow.keySet().toArray(new String[0]);
            for (int j = 0; j < columns.length; j++) {
                if (j > 0) sql.append(", ");
                sql.append(columns[j]).append(" = ").append(formatValue(afterRow.get(columns[j])));
            }

            // 构建 WHERE 子句
            sql.append(" WHERE ");
            String[] beforeColumns = beforeRow.keySet().toArray(new String[0]);
            for (int j = 0; j < beforeColumns.length; j++) {
                if (j > 0) sql.append(" AND ");
                sql.append(beforeColumns[j]).append(" = ").append(formatValue(beforeRow.get(beforeColumns[j])));
            }

            // 写入 SQL 文件（带位置信息）
            sqlFileManager.writeSql(sql.toString(), position);
        }

        logger.debug("已处理 UPDATE: {}.{}, 行数: {}", database, table, afterRows.size());
        return true;
    }

    /**
     * 处理 DELETE 操作
     */
    private boolean handleDelete(String database, String table, List<Map<String, Serializable>> rows, BinlogPosition position) {
        if (rows == null || rows.isEmpty()) {
            return true;
        }

        String fullTableName = database + "." + table;

        for (Map<String, Serializable> row : rows) {
            StringBuilder sql = new StringBuilder("DELETE FROM ");
            sql.append(fullTableName).append(" WHERE ");

            // 构建 WHERE 子句
            String[] columns = row.keySet().toArray(new String[0]);
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) sql.append(" AND ");
                sql.append(columns[i]).append(" = ").append(formatValue(row.get(columns[i])));
            }

            // 写入 SQL 文件（带位置信息）
            sqlFileManager.writeSql(sql.toString(), position);
        }

        logger.debug("已处理 DELETE: {}.{}, 行数: {}", database, table, rows.size());
        return true;
    }

    /**
     * 格式化值 - 支持 MySQL 所有数据类型
     */
    private String formatValue(Serializable value) {
        if (value == null) {
            return "NULL";
        }
        
        // 数值类型
        if (value instanceof Number) {
            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).toPlainString();
            } else if (value instanceof BigInteger) {
                return value.toString();
            } else if (value instanceof Double || value instanceof Float) {
                // 处理浮点数精度问题
                return new BigDecimal(value.toString()).toPlainString();
            }
            return value.toString();
        }
        
        // 布尔类型
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0";
        }
        
        // 时间类型 - 关键修复
        if (value instanceof java.sql.Timestamp) {
            java.sql.Timestamp ts = (java.sql.Timestamp) value;
            // 格式化为 MySQL 兼容格式，去掉末尾的 .0
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatted = sdf.format(ts);
            int nanos = ts.getNanos();
            if (nanos > 0) {
                // 有微秒/纳秒，添加小数部分
                String nanosStr = String.format("%09d", nanos);
                // 去掉末尾的 0
                nanosStr = nanosStr.replaceAll("0+$", "");
                formatted += "." + nanosStr;
            }
            return "'" + formatted + "'";
        }
        
        if (value instanceof java.sql.Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return "'" + sdf.format((java.sql.Date) value) + "'";
        }
        
        if (value instanceof java.sql.Time) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            return "'" + sdf.format((java.sql.Time) value) + "'";
        }
        
        // Java 8+ 时间类型
        if (value instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) value;
            String formatted = ldt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            int nano = ldt.getNano();
            if (nano > 0) {
                String nanosStr = String.format("%09d", nano).replaceAll("0+$", "");
                formatted += "." + nanosStr;
            }
            return "'" + formatted + "'";
        }
        
        if (value instanceof LocalDate) {
            return "'" + ((LocalDate) value).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) + "'";
        }
        
        if (value instanceof LocalTime) {
            return "'" + ((LocalTime) value).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "'";
        }
        
        if (value instanceof ZonedDateTime) {
            ZonedDateTime zdt = (ZonedDateTime) value;
            String formatted = zdt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return "'" + formatted + "'";
        }
        
        // 字节数组 - BINARY, VARBINARY, BLOB
        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            StringBuilder hex = new StringBuilder("0x");
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        
        // 字符串类型，需要转义
        String strValue = value.toString();
        strValue = strValue.replace("\\", "\\\\")
                           .replace("'", "\\'")
                           .replace("\n", "\\n")
                           .replace("\r", "\\r")
                           .replace("\t", "\\t")
                           .replace("\0", "\\0");
        return "'" + strValue + "'";
    }

    /**
     * 获取 SQL 文件管理器
     */
    public SqlFileManager getSqlFileManager() {
        return sqlFileManager;
    }

    /**
     * 关闭处理器
     */
    public void close() {
        if (sqlFileManager != null) {
            sqlFileManager.close();
        }
    }
}
