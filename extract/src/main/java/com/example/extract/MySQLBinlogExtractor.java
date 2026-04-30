package com.example.extract;

import com.example.common.AbstractExtractor;
import com.example.thl.THLEvent;
import com.example.thl.pipeline.Pipeline;
import com.example.thl.pipeline.PipelineConfig;
import com.example.thl.pipeline.PipelineContext;
import com.example.thl.pipeline.PipelineContextImpl;
import com.github.shyiko.mysql.binlog.event.*;

import java.io.*;
import java.sql.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.math.BigInteger;
import java.util.Base64;

/**
 * MySQL Binlog Extractor implementation
 * Parses binlog data and generates THL events
 * References: tungsten-replicator MySQLExtractor
 */
public class MySQLBinlogExtractor extends AbstractExtractor<byte[], THLEvent> {
    
    private String inputDir;
    private String outputDir;
    private long seqno = 1;
    private String seqnoFile;
    
    private String sourceHost;
    private int sourcePort;
    private String sourceUser;
    private String sourcePassword;
    private Connection sourceConnection;
    
    private Map<Long, Map<String, String>> tableMapCache = new HashMap<>();
    private Map<String, List<String>> tableSchemaCache = new HashMap<>();
    private Map<String, List<String>> tableColumnTypeCache = new HashMap<>();
    private Map<String, Map<String, List<String>>> enumSetValuesCache = new HashMap<>();
    private Map<String, List<String>> primaryKeyCache = new HashMap<>();
    
    private Pipeline pipeline;
    
    @Override
    protected void doInitialize() throws Exception {
        inputDir = props.getProperty("input.dir", "./output");
        outputDir = props.getProperty("output.dir", "./thl");
        
        seqnoFile = outputDir + "/.extractor_seqno";
        
        sourceHost = props.getProperty("source.mysql.host", "localhost");
        sourcePort = Integer.parseInt(props.getProperty("source.mysql.port", "3306"));
        sourceUser = props.getProperty("source.mysql.user", "root");
        sourcePassword = props.getProperty("source.mysql.password", "");
        
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }
        
        loadSeqno();
        
        connectToSourceDatabase();
        
        PipelineContext pipelineContext = new PipelineContextImpl(props);
        ((PipelineContextImpl) pipelineContext).setSourceConnection(sourceConnection);
        pipeline = PipelineConfig.loadFromProperties(props, pipelineContext);
        if (pipeline != null) {
            pipeline.prepare();
            logger.info("Pipeline initialized with {} filters", pipeline.getFilters().size());
        }
        
        logger.info("MySQL Binlog Extractor initialized - input: {}, output: {}, seqno: {}", inputDir, outputDir, seqno);
        logger.info("Connected to source database: {}:{}", sourceHost, sourcePort);
    }
    
    private void connectToSourceDatabase() throws SQLException {
        String url = "jdbc:mysql://" + sourceHost + ":" + sourcePort + "/?useSSL=false&serverTimezone=UTC";
        sourceConnection = DriverManager.getConnection(url, sourceUser, sourcePassword);
        logger.info("Successfully connected to source MySQL database");
    }
    
    private List<String> getTableColumns(String database, String table) {
        String cacheKey = database + "." + table;
        
        if (tableSchemaCache.containsKey(cacheKey)) {
            return tableSchemaCache.get(cacheKey);
        }
        
        List<String> columns = new ArrayList<>();
        
        try {
            String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                        "ORDER BY ORDINAL_POSITION";
            
            try (PreparedStatement stmt = sourceConnection.prepareStatement(sql)) {
                stmt.setString(1, database);
                stmt.setString(2, table);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        columns.add(rs.getString("COLUMN_NAME"));
                    }
                }
            }
            
            if (columns.isEmpty()) {
                logger.warn("No columns found for table: {}.{}", database, table);
            } else {
                logger.debug("Found {} columns for table: {}.{}", columns.size(), database, table);
            }
            
            tableSchemaCache.put(cacheKey, columns);
            
        } catch (SQLException e) {
            logger.error("Error fetching table schema for {}.{}: {}", database, table, e.getMessage());
        }
        
        return columns;
    }
    
    private List<String> getTableColumnTypes(String database, String table) {
        String cacheKey = database + "." + table;
        
        if (tableColumnTypeCache.containsKey(cacheKey)) {
            return tableColumnTypeCache.get(cacheKey);
        }
        
        List<String> columnTypes = new ArrayList<>();
        Map<String, List<String>> enumSetValues = new HashMap<>();
        
        try {
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                        "ORDER BY ORDINAL_POSITION";
            
            try (PreparedStatement stmt = sourceConnection.prepareStatement(sql)) {
                stmt.setString(1, database);
                stmt.setString(2, table);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        columnTypes.add(rs.getString("DATA_TYPE"));
                        String columnType = rs.getString("COLUMN_TYPE");
                        String columnName = rs.getString("COLUMN_NAME");
                        if (columnType != null && columnType.startsWith("enum(")) {
                            List<String> values = parseEnumSetValues(columnType);
                            enumSetValues.put(columnName, values);
                        } else if (columnType != null && columnType.startsWith("set(")) {
                            List<String> values = parseEnumSetValues(columnType);
                            enumSetValues.put(columnName, values);
                        }
                    }
                }
            }
            
            if (columnTypes.isEmpty()) {
                logger.warn("No column types found for table: {}.{}", database, table);
            } else {
                logger.debug("Found {} column types for table: {}.{}", columnTypes.size(), database, table);
            }
            
            tableColumnTypeCache.put(cacheKey, columnTypes);
            if (!enumSetValues.isEmpty()) {
                enumSetValuesCache.put(cacheKey, enumSetValues);
                logger.debug("Cached enum/set values for table: {}.{}: {}", database, table, enumSetValues.keySet());
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching column types for {}.{}: {}", database, table, e.getMessage());
        }
        
        return columnTypes;
    }
    
    private List<String> parseEnumSetValues(String columnType) {
        List<String> values = new ArrayList<>();
        int start = columnType.indexOf('(');
        int end = columnType.lastIndexOf(')');
        if (start < 0 || end < 0) return values;
        
        String inner = columnType.substring(start + 1, end);
        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;
        
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '\'' && (i == 0 || inner.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
                if (!inQuote && sb.length() > 0) {
                    values.add(sb.toString());
                    sb = new StringBuilder();
                }
            } else if (inQuote) {
                if (c == '\\' && i + 1 < inner.length()) {
                    char next = inner.charAt(i + 1);
                    if (next == '\'' || next == '\\') {
                        sb.append(next);
                        i++;
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            }
        }
        
        return values;
    }

    private List<String> getTablePrimaryKeys(String database, String table) {
        String cacheKey = database + "." + table;

        if (primaryKeyCache.containsKey(cacheKey)) {
            return primaryKeyCache.get(cacheKey);
        }

        List<String> pkColumns = new ArrayList<>();

        try {
            String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                        "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND CONSTRAINT_NAME = 'PRIMARY' " +
                        "ORDER BY ORDINAL_POSITION";

            try (PreparedStatement stmt = sourceConnection.prepareStatement(sql)) {
                stmt.setString(1, database);
                stmt.setString(2, table);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        pkColumns.add(rs.getString("COLUMN_NAME"));
                    }
                }
            }

            if (pkColumns.isEmpty()) {
                logger.warn("No primary key found for table: {}.{}", database, table);
            } else {
                logger.debug("Found primary key columns for table: {}.{}: {}", database, table, pkColumns);
            }

            primaryKeyCache.put(cacheKey, pkColumns);

        } catch (SQLException e) {
            logger.error("Error fetching primary key for {}.{}: {}", database, table, e.getMessage());
        }

        return pkColumns;
    }

    public void close() {
        saveSeqno();
        if (sourceConnection != null) {
            try {
                sourceConnection.close();
                logger.info("Closed source database connection");
            } catch (SQLException e) {
                logger.error("Error closing source database connection", e);
            }
        }
    }
    
    private void loadSeqno() {
        File file = new File(seqnoFile);
        if (!file.exists()) {
            logger.info("No seqno file found, starting from 1");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                seqno = Long.parseLong(line.trim()) + 1;
                logger.info("Loaded seqno from file, starting from: {}", seqno);
            }
        } catch (Exception e) {
            logger.warn("Error loading seqno file, starting from 1: {}", e.getMessage());
            seqno = 1;
        }
    }
    
    public void saveSeqno() {
        File file = new File(seqnoFile);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(seqno - 1));
        } catch (IOException e) {
            logger.error("Error saving seqno file", e);
        }
    }
    
    public long getCurrentSeqno() {
        return seqno;
    }
    
    public void setSeqno(long value) {
        this.seqno = value;
        saveSeqno();
        logger.info("Set seqno to: {}", value);
    }
    
    public void resetSeqno() {
        this.seqno = 1;
        File file = new File(seqnoFile);
        if (file.exists()) {
            file.delete();
        }
        logger.info("Reset seqno to 1");
    }
    
    private static final char FIELD_SEP = '\t';
    private static final char VALUE_SEP = '\u0001';

    @Override
    protected THLEvent doExtract(byte[] input) throws Exception {
        String eventStr = new String(input, "UTF-8");
        
        String[] parts = eventStr.split(String.valueOf(FIELD_SEP));
        if (parts.length < 5) {
            if (eventStr.contains(":")) {
                return doExtractLegacy(input);
            }
            logger.warn("Invalid event format: {}", eventStr);
            return null;
        }
        
        if (!"EVENT".equals(parts[0])) {
            logger.warn("Invalid event format (missing EVENT prefix): {}", eventStr);
            return null;
        }
        
        String eventType = parts[1];
        long timestamp = Long.parseLong(parts[2]);
        String binlogFile = parts[3];
        String positionStr = parts[4];
        long position = 0;
        int lastColon = positionStr.lastIndexOf(':');
        if (lastColon > 0) {
            position = Long.parseLong(positionStr.substring(lastColon + 1).trim());
        } else {
            position = Long.parseLong(positionStr.trim());
        }
        
        THLEvent thlEvent = new THLEvent();
        thlEvent.setSeqno(seqno++);
        thlEvent.setEventId(binlogFile + ":" + position);
        thlEvent.setSourceId("mysql");
        thlEvent.setSourceTstamp(new Timestamp(timestamp));
        thlEvent.addMetadata("event_type", eventType);
        thlEvent.addMetadata("binlog_file", binlogFile);
        thlEvent.addMetadata("binlog_position", position);
        
        String database = null;
        String table = null;
        Long tableId = null;
        String sql = null;
        List<Object[]> rows = null;
        List<Map.Entry<Object[], Object[]>> updateRows = null;
        
        for (int i = 5; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("database=")) {
                database = part.substring("database=".length());
                thlEvent.addMetadata("database_name", database);
            } else if (part.startsWith("table=")) {
                table = part.substring("table=".length());
                thlEvent.addMetadata("table_name", table);
            } else if (part.startsWith("table_id=")) {
                tableId = Long.parseLong(part.substring("table_id=".length()));
                thlEvent.addMetadata("table_id", tableId);
            } else if (part.startsWith("sql=")) {
                sql = part.substring("sql=".length());
                sql = unescapeSpecialChars(sql);
                thlEvent.addMetadata("sql", sql);
            } else if (part.startsWith("row_count=")) {
                int rowCount = Integer.parseInt(part.substring("row_count=".length()));
                thlEvent.addMetadata("row_count", rowCount);
            } else if (part.startsWith("row") && isRowPart(part)) {
                if (rows == null) {
                    rows = new ArrayList<>();
                }
                int eqIdx = part.indexOf('=');
                Object[] row = parseRow(part.substring(eqIdx + 1));
                rows.add(row);
                thlEvent.addMetadata("rows", rows);
            } else if (part.startsWith("old_row") && isRowPart(part.substring(4))) {
                if (updateRows == null) {
                    updateRows = new ArrayList<>();
                }
                int eqIdx = part.indexOf('=');
                Object[] oldRow = parseRow(part.substring(eqIdx + 1));
                thlEvent.addMetadata("old_row", oldRow);
            } else if (part.startsWith("new_row") && isRowPart(part.substring(4))) {
                int eqIdx = part.indexOf('=');
                Object[] newRow = parseRow(part.substring(eqIdx + 1));
                thlEvent.addMetadata("new_row", newRow);
                if (updateRows == null) {
                    updateRows = new ArrayList<>();
                }
                Object[] oldRow = (Object[]) thlEvent.getMetadata("old_row");
                updateRows.add(new AbstractMap.SimpleEntry<>(oldRow, newRow));
                thlEvent.addMetadata("rows", updateRows);
            } else if (part.startsWith("column_types=")) {
                thlEvent.addMetadata("column_types", part.substring("column_types=".length()));
            } else if (part.startsWith("column_metadata=")) {
                thlEvent.addMetadata("column_metadata", part.substring("column_metadata=".length()));
            }
        }
        
        if (eventType.equals("TABLE_MAP") && tableId != null && database != null && table != null) {
            Map<String, String> tableInfo = new HashMap<>();
            tableInfo.put("database", database);
            tableInfo.put("table", table);
            tableMapCache.put(tableId, tableInfo);
            logger.debug("Cached table mapping: table_id={}, database={}, table={}", tableId, database, table);
            
            List<String> columns = getTableColumns(database, table);
            tableInfo.put("columns", String.join(",", columns));
            
            List<String> columnTypes = getTableColumnTypes(database, table);
            tableInfo.put("column_types", String.join(",", columnTypes));
            
            String cacheKey = database + "." + table;
            Map<String, List<String>> enumSetValues = enumSetValuesCache.get(cacheKey);
            if (enumSetValues != null && !enumSetValues.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, List<String>> entry : enumSetValues.entrySet()) {
                    if (sb.length() > 0) sb.append(";");
                    sb.append(entry.getKey()).append("=");
                    sb.append(String.join(",", entry.getValue()));
                }
                tableInfo.put("enum_set_values", sb.toString());
            }
        }
        
        if ((eventType.equals("EXT_WRITE_ROWS") || eventType.equals("EXT_UPDATE_ROWS") || 
             eventType.equals("EXT_DELETE_ROWS") || eventType.equals("WRITE_ROWS") ||
             eventType.equals("UPDATE_ROWS") || eventType.equals("DELETE_ROWS")) && tableId != null) {
            
            Map<String, String> tableInfo = tableMapCache.get(tableId);
            if (tableInfo != null) {
                if (database == null) {
                    database = tableInfo.get("database");
                    thlEvent.addMetadata("database_name", database);
                }
                if (table == null) {
                    table = tableInfo.get("table");
                    thlEvent.addMetadata("table_name", table);
                }
                
                String columnsStr = tableInfo.get("columns");
                if (columnsStr != null && !columnsStr.isEmpty()) {
                    thlEvent.addMetadata("column_names", columnsStr);
                    logger.debug("Added column names for event {}: {}", eventType, columnsStr);
                }
                
                String columnTypesStr = tableInfo.get("column_types");
                if (columnTypesStr != null && !columnTypesStr.isEmpty()) {
                    thlEvent.addMetadata("mysql_column_types", columnTypesStr);
                    logger.debug("Added column types for event {}: {}", eventType, columnTypesStr);
                }
                
                String enumSetValuesStr = tableInfo.get("enum_set_values");
                if (enumSetValuesStr != null && !enumSetValuesStr.isEmpty()) {
                    thlEvent.addMetadata("enum_set_values", enumSetValuesStr);
                    logger.debug("Added enum/set values for event {}: {}", eventType, enumSetValuesStr);
                }
                
                if (columnTypesStr != null && !columnTypesStr.isEmpty()) {
                    String[] colTypes = columnTypesStr.split(",");
                    String[] colNames = columnsStr != null ? columnsStr.split(",") : new String[0];
                    Object rowsObj = thlEvent.getMetadata("rows");
                    if (rowsObj instanceof List) {
                        List<?> rowList = (List<?>) rowsObj;
                        if (!rowList.isEmpty()) {
                            Object first = rowList.get(0);
                            if (first instanceof Object[]) {
                                List<Object[]> convertedRows = new ArrayList<>();
                                for (Object rowObj : rowList) {
                                    Object[] row = (Object[]) rowObj;
                                    convertedRows.add(convertRowByColumnTypes(row, colTypes, colNames, enumSetValuesStr));
                                }
                                thlEvent.addMetadata("rows", convertedRows);
                            } else if (first instanceof Map.Entry) {
                                List<Map.Entry<Object[], Object[]>> convertedRows = new ArrayList<>();
                                for (Object rowObj : rowList) {
                                    @SuppressWarnings("unchecked")
                                    Map.Entry<Object[], Object[]> entry = (Map.Entry<Object[], Object[]>) rowObj;
                                    Object[] convertedOld = convertRowByColumnTypes(entry.getKey(), colTypes, colNames, enumSetValuesStr);
                                    Object[] convertedNew = convertRowByColumnTypes(entry.getValue(), colTypes, colNames, enumSetValuesStr);
                                    convertedRows.add(new AbstractMap.SimpleEntry<>(convertedOld, convertedNew));
                                }
                                thlEvent.addMetadata("rows", convertedRows);
                                
                                Object oldRowObj = thlEvent.getMetadata("old_row");
                                Object newRowObj = thlEvent.getMetadata("new_row");
                                if (oldRowObj instanceof Object[] && newRowObj instanceof Object[]) {
                                    thlEvent.addMetadata("old_row", convertRowByColumnTypes((Object[]) oldRowObj, colTypes, colNames, enumSetValuesStr));
                                    thlEvent.addMetadata("new_row", convertRowByColumnTypes((Object[]) newRowObj, colTypes, colNames, enumSetValuesStr));
                                }
                            }
                        }
                    }
                }
                
                if (database != null && table != null) {
                    List<String> pkCols = getTablePrimaryKeys(database, table);
                    if (!pkCols.isEmpty()) {
                        String pkStr = String.join(",", pkCols);
                        thlEvent.addMetadata("primary_key_columns", pkStr);
                        logger.debug("Added primary key columns for event {}: {}", eventType, pkStr);
                    }
                }
                
                logger.debug("Resolved table info for event {}: database={}, table={}", 
                    eventType, database, table);
            } else {
                logger.warn("Table mapping not found for table_id: {}", tableId);
            }
        }
        
        if ("QUERY".equals(eventType) && sql != null && !sql.isEmpty()) {
            String ddlDatabase = DdlDatabaseAnltrExtractor.extractDatabase(
                    sql, database, props.getProperty("default.database", ""));
            if (ddlDatabase != null && !ddlDatabase.isEmpty()) {
                thlEvent.addMetadata("ddl_database", ddlDatabase);
                logger.debug("Set ddl_database for QUERY event: seqno={}, ddl_database={}, sql={}",
                        thlEvent.getSeqno(), ddlDatabase, sql.substring(0, Math.min(sql.length(), 100)));
            }
        }

        thlEvent.setData(input);
        
        lastExtractedPosition = binlogFile + ":" + position;
        
        logger.debug("Extracted THL event: seqno={}, eventId={}, type={}, database={}, table={}", 
            thlEvent.getSeqno(), thlEvent.getEventId(), eventType, database, table);
        
        return thlEvent;
    }

    private THLEvent doExtractLegacy(byte[] input) throws Exception {
        String eventStr = new String(input, "UTF-8");
        
        String[] parts = eventStr.split(":");
        if (parts.length < 6) {
            logger.warn("Invalid legacy event format: {}", eventStr);
            return null;
        }
        
        String eventType = parts[1];
        long timestamp = Long.parseLong(parts[2]);
        String binlogFile = parts[3];
        long position = Long.parseLong(parts[5].trim());
        
        THLEvent thlEvent = new THLEvent();
        thlEvent.setSeqno(seqno++);
        thlEvent.setEventId(binlogFile + ":" + position);
        thlEvent.setSourceId("mysql");
        thlEvent.setSourceTstamp(new Timestamp(timestamp));
        thlEvent.addMetadata("event_type", eventType);
        thlEvent.addMetadata("binlog_file", binlogFile);
        thlEvent.addMetadata("binlog_position", position);
        
        for (int i = 6; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("database=")) {
                thlEvent.addMetadata("database_name", part.substring("database=".length()));
            } else if (part.startsWith("table=")) {
                thlEvent.addMetadata("table_name", part.substring("table=".length()));
            } else if (part.startsWith("table_id=")) {
                thlEvent.addMetadata("table_id", Long.parseLong(part.substring("table_id=".length())));
            } else if (part.startsWith("sql=")) {
                StringBuilder sqlBuilder = new StringBuilder(part.substring("sql=".length()));
                for (int j = i + 1; j < parts.length; j++) {
                    sqlBuilder.append(":").append(parts[j]);
                }
                thlEvent.addMetadata("sql", unescapeSpecialChars(sqlBuilder.toString()));
                break;
            } else if (part.startsWith("xid=")) {
                thlEvent.addMetadata("xid", Long.parseLong(part.substring("xid=".length())));
            }
        }

        if ("QUERY".equals(eventType)) {
            String legacySql = (String) thlEvent.getMetadata("sql");
            String legacyDatabase = (String) thlEvent.getMetadata("database_name");
            if (legacySql != null && !legacySql.isEmpty()) {
                String ddlDatabase = DdlDatabaseAnltrExtractor.extractDatabase(
                        legacySql, legacyDatabase, props.getProperty("default.database", ""));
                if (ddlDatabase != null && !ddlDatabase.isEmpty()) {
                    thlEvent.addMetadata("ddl_database", ddlDatabase);
                    logger.debug("Set ddl_database for legacy QUERY event: seqno={}, ddl_database={}",
                            thlEvent.getSeqno(), ddlDatabase);
                }
            }
        }

        thlEvent.setData(input);
        return thlEvent;
    }

    private Object[] parseRow(String rowStr) {
        if (rowStr == null || rowStr.isEmpty()) {
            return new Object[0];
        }
        
        if (rowStr.contains(String.valueOf(VALUE_SEP))) {
            return parseRowNewFormat(rowStr);
        }
        
        return parseRowLegacyFormat(rowStr);
    }

    private Object[] parseRowNewFormat(String rowStr) {
        String[] valueStrs = rowStr.split(String.valueOf(VALUE_SEP));
        Object[] row = new Object[valueStrs.length];
        
        for (int i = 0; i < valueStrs.length; i++) {
            row[i] = decodeValue(valueStrs[i]);
        }
        
        return row;
    }

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final SimpleDateFormat TIMESTAMP_PARSE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_PARSE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_PARSE_FMT = new SimpleDateFormat("HH:mm:ss");
    static {
        TIMESTAMP_PARSE_FMT.setTimeZone(UTC);
        DATE_PARSE_FMT.setTimeZone(UTC);
        TIME_PARSE_FMT.setTimeZone(UTC);
    }

    private Object decodeValue(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
        
        if (encoded.equals("N")) {
            return null;
        }
        
        int colonIdx = encoded.indexOf(':');
        if (colonIdx < 0) {
            return unescapeSpecialChars(encoded);
        }
        
        String type = encoded.substring(0, colonIdx);
        String value = encoded.substring(colonIdx + 1);
        
        try {
            switch (type) {
                case "I":
                    return Integer.parseInt(value);
                case "L":
                    return Long.parseLong(value);
                case "SRT":
                    return Short.parseShort(value);
                case "BYT":
                    return Byte.parseByte(value);
                case "F":
                    return Float.parseFloat(value);
                case "D":
                    return Double.parseDouble(value);
                case "BD":
                    return new java.math.BigDecimal(value);
                case "TS":
                    return parseTimestamp(unescapeSpecialChars(value));
                case "DT":
                    return parseDate(unescapeSpecialChars(value));
                case "TM":
                    return parseTime(unescapeSpecialChars(value));
                case "BL":
                    return "1".equals(value);
                case "B":
                    return Base64.getDecoder().decode(value);
                case "S":
                    return unescapeSpecialChars(value);
                default:
                    return unescapeSpecialChars(value);
            }
        } catch (Exception e) {
            logger.warn("Error decoding value type={}, value={}: {}", type, value, e.getMessage());
            return unescapeSpecialChars(value);
        }
    }

    private java.sql.Timestamp parseTimestamp(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            synchronized (TIMESTAMP_PARSE_FMT) {
                java.util.Date date = TIMESTAMP_PARSE_FMT.parse(value);
                return new java.sql.Timestamp(date.getTime());
            }
        } catch (ParseException e) {
            logger.warn("Failed to parse timestamp '{}': {}", value, e.getMessage());
            return java.sql.Timestamp.valueOf(value);
        }
    }

    private java.sql.Date parseDate(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            synchronized (DATE_PARSE_FMT) {
                java.util.Date date = DATE_PARSE_FMT.parse(value);
                return new java.sql.Date(date.getTime());
            }
        } catch (ParseException e) {
            logger.warn("Failed to parse date '{}': {}", value, e.getMessage());
            return java.sql.Date.valueOf(value);
        }
    }

    private java.sql.Time parseTime(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            synchronized (TIME_PARSE_FMT) {
                java.util.Date date = TIME_PARSE_FMT.parse(value);
                return new java.sql.Time(date.getTime());
            }
        } catch (ParseException e) {
            logger.warn("Failed to parse time '{}': {}", value, e.getMessage());
            return java.sql.Time.valueOf(value);
        }
    }

    private boolean isRowPart(String part) {
        if (part.startsWith("row_count=")) return false;
        int eqIdx = part.indexOf('=');
        if (eqIdx < 0) return false;
        String prefix = part.substring(0, eqIdx);
        if (prefix.startsWith("row")) {
            String numPart = prefix.substring(3);
            return !numPart.isEmpty() && numPart.chars().allMatch(Character::isDigit);
        }
        return false;
    }

    private String unescapeSpecialChars(String s) {
        if (s == null || !s.contains("\\")) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case 'n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case 'u':
                        if (i + 5 < s.length() && s.substring(i + 2, i + 6).equals("0001")) {
                            sb.append('\u0001');
                            i += 5;
                        } else {
                            sb.append(c);
                        }
                        break;
                    default: sb.append(c); break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Object[] convertRowByColumnTypes(Object[] row, String[] columnTypes, String[] colNames, String enumSetValuesStr) {
        if (row == null || columnTypes == null) return row;
        
        Map<String, List<String>> enumSetMap = parseEnumSetValuesStr(enumSetValuesStr);
        
        Object[] converted = new Object[row.length];
        for (int i = 0; i < row.length; i++) {
            String colType = i < columnTypes.length ? columnTypes[i] : null;
            String colName = i < colNames.length ? colNames[i] : null;
            List<String> enumValues = colName != null ? enumSetMap.get(colName) : null;
            converted[i] = convertValueByColumnType(row[i], colType, enumValues);
        }
        return converted;
    }
    
    private Map<String, List<String>> parseEnumSetValuesStr(String enumSetValuesStr) {
        Map<String, List<String>> result = new HashMap<>();
        if (enumSetValuesStr == null || enumSetValuesStr.isEmpty()) return result;
        
        String[] entries = enumSetValuesStr.split(";");
        for (String entry : entries) {
            int eqIdx = entry.indexOf('=');
            if (eqIdx > 0) {
                String name = entry.substring(0, eqIdx);
                String valuesStr = entry.substring(eqIdx + 1);
                List<String> values = Arrays.asList(valuesStr.split(","));
                result.put(name, values);
            }
        }
        return result;
    }

    private Object convertValueByColumnType(Object value, String columnType, List<String> enumValues) {
        if (value == null || columnType == null) return value;
        
        switch (columnType) {
            case "char":
            case "varchar":
                if (value instanceof byte[]) {
                    try {
                        return new String((byte[]) value, "UTF-8");
                    } catch (Exception e) {
                        logger.warn("Failed to convert byte[] to String for {} column: {}", columnType, e.getMessage());
                        return value;
                    }
                }
                return value;
                
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext":
                if (value instanceof byte[]) {
                    try {
                        return new String((byte[]) value, "UTF-8");
                    } catch (Exception e) {
                        logger.warn("Failed to convert byte[] to String for {} column: {}", columnType, e.getMessage());
                        return value;
                    }
                }
                return value;
                
            case "binary":
            case "varbinary":
                if (value instanceof String) {
                    try {
                        return ((String) value).getBytes("ISO-8859-1");
                    } catch (Exception e) {
                        logger.warn("Failed to convert String to byte[] for {} column: {}", columnType, e.getMessage());
                        return value;
                    }
                }
                if (value instanceof byte[]) {
                    return value;
                }
                return value;
                
            case "enum":
                if (value instanceof Integer) {
                    int idx = (Integer) value;
                    if (enumValues != null && idx >= 1 && idx <= enumValues.size()) {
                        return enumValues.get(idx - 1);
                    }
                    return value;
                }
                if (value instanceof byte[]) {
                    try {
                        return new String((byte[]) value, "UTF-8");
                    } catch (Exception e) {
                        logger.warn("Failed to convert byte[] to String for enum column: {}", e.getMessage());
                        return value;
                    }
                }
                return value;
                
            case "set":
                if (value instanceof Long) {
                    long bits = (Long) value;
                    if (enumValues != null && bits > 0) {
                        List<String> selected = new ArrayList<>();
                        for (int i = 0; i < enumValues.size(); i++) {
                            if ((bits & (1L << i)) != 0) {
                                selected.add(enumValues.get(i));
                            }
                        }
                        return String.join(",", selected);
                    }
                    return value;
                }
                if (value instanceof byte[]) {
                    try {
                        return new String((byte[]) value, "UTF-8");
                    } catch (Exception e) {
                        logger.warn("Failed to convert byte[] to String for set column: {}", e.getMessage());
                        return value;
                    }
                }
                return value;
                
            case "json":
                if (value instanceof byte[]) {
                    try {
                        return com.github.shyiko.mysql.binlog.event.deserialization.json.JsonBinary.parseAsString((byte[]) value);
                    } catch (Exception e) {
                        logger.warn("Failed to parse JSON binary data: {}", e.getMessage());
                        try {
                            return new String((byte[]) value, "UTF-8");
                        } catch (Exception e2) {
                            return value;
                        }
                    }
                }
                return value;
                
            default:
                return value;
        }
    }

    private Object[] parseRowLegacyFormat(String rowStr) {
        if (rowStr.startsWith("[") && rowStr.endsWith("]")) {
            rowStr = rowStr.substring(1, rowStr.length() - 1);
        }
        
        if (rowStr.trim().isEmpty()) {
            return new Object[0];
        }
        
        String[] parts = rowStr.split(", ");
        Object[] row = new Object[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            String value = parts[i].trim();
            if (value.equals("null")) {
                row[i] = null;
            } else if (value.startsWith("'") && value.endsWith("'")) {
                row[i] = value.substring(1, value.length() - 1);
            } else {
                try {
                    if (value.contains(".")) {
                        row[i] = Double.parseDouble(value);
                    } else {
                        row[i] = Long.parseLong(value);
                    }
                } catch (NumberFormatException e) {
                    row[i] = value;
                }
            }
        }
        
        return row;
    }
    
    /**
     * Process all binlog files in the input directory
     * 
     * @throws Exception if processing fails
     */
    public void processAllFiles() throws Exception {
        File inputDirFile = new File(inputDir);
        File[] binlogFiles = inputDirFile.listFiles((dir, name) -> 
            name.startsWith("binlog-") && name.endsWith(".bin"));
        
        if (binlogFiles == null || binlogFiles.length == 0) {
            logger.warn("No binlog files found in directory: {}", inputDir);
            return;
        }
        
        logger.info("Found {} binlog files to process", binlogFiles.length);
        
        // Sort files by name to ensure correct order
        Arrays.sort(binlogFiles);
        
        // Create THL output file
        String outputFileName = "thl-" + System.currentTimeMillis() + ".thl";
        File outputFile = new File(outputDir, outputFileName);
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            for (File binlogFile : binlogFiles) {
                logger.info("Processing binlog file: {}", binlogFile.getName());
                processBinlogFile(binlogFile, oos);
            }
        }
        
        logger.info("THL file created: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Process a single binlog file
     * 
     * @param binlogFile binlog file
     * @param oos object output stream for THL events
     * @throws Exception if processing fails
     */
    private void processBinlogFile(File binlogFile, ObjectOutputStream oos) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(binlogFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                byte[] eventBytes = line.getBytes("UTF-8");
                THLEvent event = extract(eventBytes);
                if (event != null) {
                    if (pipeline != null) {
                        event = pipeline.process(event);
                    }
                    if (event != null) {
                        oos.writeObject(event);
                    }
                }
            }
        }
    }
    
    /**
     * Parse row data from binlog event
     * 
     * @param eventData row event data
     * @return parsed row data
     */
    private Map<String, Object> parseRowData(EventData eventData) {
        Map<String, Object> rowData = new HashMap<>();
        
        if (eventData instanceof WriteRowsEventData) {
            WriteRowsEventData writeData = (WriteRowsEventData) eventData;
            rowData.put("operation", "INSERT");
            rowData.put("table_id", writeData.getTableId());
            rowData.put("rows", writeData.getRows());
        } else if (eventData instanceof UpdateRowsEventData) {
            UpdateRowsEventData updateData = (UpdateRowsEventData) eventData;
            rowData.put("operation", "UPDATE");
            rowData.put("table_id", updateData.getTableId());
            rowData.put("rows", updateData.getRows());
        } else if (eventData instanceof DeleteRowsEventData) {
            DeleteRowsEventData deleteData = (DeleteRowsEventData) eventData;
            rowData.put("operation", "DELETE");
            rowData.put("table_id", deleteData.getTableId());
            rowData.put("rows", deleteData.getRows());
        }
        
        return rowData;
    }
    
    /**
     * Parse table map event
     * 
     * @param eventData table map event data
     * @return parsed table metadata
     */
    private Map<String, Object> parseTableMapEvent(TableMapEventData eventData) {
        Map<String, Object> tableInfo = new HashMap<>();
        tableInfo.put("table_id", eventData.getTableId());
        tableInfo.put("database", eventData.getDatabase());
        tableInfo.put("table", eventData.getTable());
        return tableInfo;
    }
    
    /**
     * Parse query event
     * 
     * @param eventData query event data
     * @return parsed query information
     */
    private Map<String, Object> parseQueryEvent(QueryEventData eventData) {
        Map<String, Object> queryInfo = new HashMap<>();
        queryInfo.put("database", eventData.getDatabase());
        queryInfo.put("sql", eventData.getSql());
        queryInfo.put("operation", "QUERY");
        return queryInfo;
    }
    
    /**
     * Get event type name
     * 
     * @param eventType event type
     * @return event type name
     */
    private String getEventTypeName(EventType eventType) {
        String typeName = eventType.name();
        
        if (typeName.contains("WRITE_ROWS")) {
            return "INSERT";
        } else if (typeName.contains("UPDATE_ROWS")) {
            return "UPDATE";
        } else if (typeName.contains("DELETE_ROWS")) {
            return "DELETE";
        } else if (typeName.equals("QUERY")) {
            return "QUERY";
        } else if (typeName.equals("TABLE_MAP")) {
            return "TABLE_MAP";
        } else if (typeName.equals("XID")) {
            return "COMMIT";
        } else if (typeName.equals("ROTATE")) {
            return "ROTATE";
        } else {
            return typeName;
        }
    }
}
