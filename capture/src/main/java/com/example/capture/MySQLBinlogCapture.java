package com.example.capture;

import com.example.common.AbstractCapture;
import com.example.common.RetryUtil;
import com.example.common.MonitorUtil;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Base64;

/**
 * MySQL Binlog Capture implementation
 * Captures binlog events from remote MySQL server using mysql-binlog-connector-java
 */
public class MySQLBinlogCapture extends AbstractCapture<byte[]> {
    
    private String host;
    private int port;
    private String user;
    private String password;
    private String binlogFile;
    private long binlogPosition;
    private String outputDir;
    
    private BinaryLogClient client;
    private FileOutputStream outputStream;
    private String currentOutputFile;
    private AtomicLong eventCount = new AtomicLong(0);
    private AtomicBoolean connected = new AtomicBoolean(false);
    
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000;
    private static final long MAX_DELAY_MS = 30000;
    
    @Override
    protected void doInitialize() throws Exception {
        host = props.getProperty("mysql.host", "localhost");
        port = Integer.parseInt(props.getProperty("mysql.port", "3306"));
        user = props.getProperty("mysql.user", "root");
        password = props.getProperty("mysql.password", "");
        binlogFile = props.getProperty("binlog.file", "");
        binlogPosition = Long.parseLong(props.getProperty("binlog.position", "4"));
        
        outputDir = props.getProperty("output.dir", "./output");
        
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }
        
        logger.info("MySQL Binlog Capture initialized - host: {}, port: {}, user: {}", host, port, user);
        MonitorUtil.setMetric("capture_type", "mysql_binlog");
        MonitorUtil.setMetric("mysql_host", host);
        MonitorUtil.setMetric("mysql_port", port);
    }
    
    @Override
    protected void doStart() throws Exception {
        MonitorUtil.recordStartTime("capture");
        
        // Create BinaryLogClient with retry
        client = RetryUtil.executeWithRetry(() -> {
            BinaryLogClient binlogClient = new BinaryLogClient(host, port, user, password);
            
            com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer eventDeserializer =
                new com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer();
            eventDeserializer.setCompatibilityMode(
                com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
            );
            binlogClient.setEventDeserializer(eventDeserializer);
            logger.info("Set CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY for binary data preservation");
            
            if (binlogFile != null && !binlogFile.isEmpty()) {
                binlogClient.setBinlogFilename(binlogFile);
                binlogClient.setBinlogPosition(binlogPosition);
                logger.info("Starting from binlog file: {}, position: {}", binlogFile, binlogPosition);
            }
            
            return binlogClient;
        }, MAX_RETRIES, INITIAL_DELAY_MS, MAX_DELAY_MS);
        
        // Create output file
        String outputFileName = "binlog-" + System.currentTimeMillis() + ".bin";
        File outputFile = new File(outputDir, outputFileName);
        currentOutputFile = outputFile.getAbsolutePath();
        outputStream = new FileOutputStream(outputFile);
        logger.info("Output file: {}", currentOutputFile);
        
        MonitorUtil.setMetric("output_file", currentOutputFile);
        
        // Register event listener
        client.registerEventListener(this::processEvent);
        
        // Register lifecycle listener
        client.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                connected.set(true);
                logger.info("Connected to MySQL server");
                MonitorUtil.incrementCounter("mysql_connections");
            }
            
            @Override
            public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
                logger.error("Communication failure with MySQL server", ex);
                MonitorUtil.incrementCounter("communication_failures");
                
                // Attempt to reconnect
                if (running) {
                    try {
                        logger.info("Attempting to reconnect...");
                        reconnect();
                    } catch (Exception e) {
                        logger.error("Reconnection failed", e);
                    }
                }
            }
            
            @Override
            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
                logger.error("Event deserialization failure", ex);
                MonitorUtil.incrementCounter("deserialization_failures");
            }
            
            @Override
            public void onDisconnect(BinaryLogClient client) {
                connected.set(false);
                logger.info("Disconnected from MySQL server");
                MonitorUtil.incrementCounter("mysql_disconnections");
            }
        });
        
        // Connect to MySQL server with retry
        RetryUtil.executeWithRetry(() -> {
            logger.info("Connecting to MySQL server...");
            client.connect(30000);
            return null;
        }, MAX_RETRIES, INITIAL_DELAY_MS, MAX_DELAY_MS);
        
        logger.info("MySQL Binlog Capture started successfully");
    }
    
    /**
     * Process binlog event
     */
    private void processEvent(Event event) {
        if (!running) {
            return;
        }
        
        try {
            EventHeader header = event.getHeader();
            EventData data = event.getData();
            
            // Update current position
            String currentFile = client.getBinlogFilename();
            long position = client.getBinlogPosition();
            String positionStr = currentFile + ":" + position;
            
            // Serialize event to bytes
            byte[] eventBytes = serializeEvent(event, currentFile, positionStr);
            
            if (eventBytes != null && eventBytes.length > 0) {
                outputStream.write(eventBytes);
                outputStream.flush();
                
                long count = eventCount.incrementAndGet();
                MonitorUtil.incrementCounter("events_captured");
                
                // Log progress every 1000 events
                if (count % 1000 == 0) {
                    logger.info("Captured {} events, current position: {}", count, currentPosition);
                    MonitorUtil.logThroughput("capture", count);
                }
            }
        } catch (IOException e) {
            logger.error("Error writing event to output file", e);
            MonitorUtil.incrementCounter("write_errors");
        }
    }
    
    /**
     * Serialize event to bytes
     */
    private static final char FIELD_SEP = '\t';
    private static final char VALUE_SEP = '\u0001';

    private byte[] serializeEvent(Event event, String binlogFile, String position) {
        try {
            EventHeader header = event.getHeader();
            EventData data = event.getData();
            StringBuilder sb = new StringBuilder();
            
            sb.append("EVENT").append(FIELD_SEP);
            sb.append(header.getEventType().name()).append(FIELD_SEP);
            sb.append(header.getTimestamp()).append(FIELD_SEP);
            sb.append(binlogFile).append(FIELD_SEP);
            sb.append(position);
            
            if (data instanceof TableMapEventData) {
                TableMapEventData tableMap = (TableMapEventData) data;
                sb.append(FIELD_SEP).append("database=").append(tableMap.getDatabase());
                sb.append(FIELD_SEP).append("table=").append(tableMap.getTable());
                sb.append(FIELD_SEP).append("table_id=").append(tableMap.getTableId());
                
                byte[] columnTypes = tableMap.getColumnTypes();
                if (columnTypes != null && columnTypes.length > 0) {
                    sb.append(FIELD_SEP).append("column_types=").append(Base64.getEncoder().encodeToString(columnTypes));
                }
                
                int[] columnMetadata = tableMap.getColumnMetadata();
                if (columnMetadata != null && columnMetadata.length > 0) {
                    StringBuilder metaSb = new StringBuilder();
                    for (int i = 0; i < columnMetadata.length; i++) {
                        if (i > 0) metaSb.append(",");
                        metaSb.append(columnMetadata[i]);
                    }
                    sb.append(FIELD_SEP).append("column_metadata=").append(metaSb);
                }
            } else if (data instanceof WriteRowsEventData) {
                WriteRowsEventData writeData = (WriteRowsEventData) data;
                sb.append(FIELD_SEP).append("table_id=").append(writeData.getTableId());
                
                List<Serializable[]> rows = writeData.getRows();
                if (rows != null && !rows.isEmpty()) {
                    sb.append(FIELD_SEP).append("row_count=").append(rows.size());
                    for (int i = 0; i < rows.size(); i++) {
                        Serializable[] row = rows.get(i);
                        if (row != null) {
                            sb.append(FIELD_SEP).append("row").append(i).append("=").append(encodeRow(row));
                        }
                    }
                }
            } else if (data instanceof UpdateRowsEventData) {
                UpdateRowsEventData updateData = (UpdateRowsEventData) data;
                sb.append(FIELD_SEP).append("table_id=").append(updateData.getTableId());
                
                List<Map.Entry<Serializable[], Serializable[]>> rows = updateData.getRows();
                if (rows != null && !rows.isEmpty()) {
                    sb.append(FIELD_SEP).append("row_count=").append(rows.size());
                    int i = 0;
                    for (Map.Entry<Serializable[], Serializable[]> entry : rows) {
                        Serializable[] oldRow = entry.getKey();
                        Serializable[] newRow = entry.getValue();
                        if (oldRow != null) {
                            sb.append(FIELD_SEP).append("old_row").append(i).append("=").append(encodeRow(oldRow));
                        }
                        if (newRow != null) {
                            sb.append(FIELD_SEP).append("new_row").append(i).append("=").append(encodeRow(newRow));
                        }
                        i++;
                    }
                }
            } else if (data instanceof DeleteRowsEventData) {
                DeleteRowsEventData deleteData = (DeleteRowsEventData) data;
                sb.append(FIELD_SEP).append("table_id=").append(deleteData.getTableId());
                
                List<Serializable[]> rows = deleteData.getRows();
                if (rows != null && !rows.isEmpty()) {
                    sb.append(FIELD_SEP).append("row_count=").append(rows.size());
                    for (int i = 0; i < rows.size(); i++) {
                        Serializable[] row = rows.get(i);
                        if (row != null) {
                            sb.append(FIELD_SEP).append("row").append(i).append("=").append(encodeRow(row));
                        }
                    }
                }
            } else if (data instanceof QueryEventData) {
                QueryEventData queryData = (QueryEventData) data;
                sb.append(FIELD_SEP).append("database=").append(queryData.getDatabase());
                sb.append(FIELD_SEP).append("sql=").append(escapeSpecialChars(queryData.getSql()));
            } else if (data instanceof XidEventData) {
                XidEventData xidData = (XidEventData) data;
                sb.append(FIELD_SEP).append("xid=").append(xidData.getXid());
            }
            
            sb.append("\n");
            
            return sb.toString().getBytes("UTF-8");
        } catch (Exception e) {
            logger.error("Error serializing event", e);
            MonitorUtil.incrementCounter("serialization_errors");
            return null;
        }
    }

    private String encodeRow(Serializable[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) sb.append(VALUE_SEP);
            sb.append(encodeValue(row[i]));
        }
        return sb.toString();
    }

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final SimpleDateFormat TIMESTAMP_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss");
    static {
        TIMESTAMP_FMT.setTimeZone(UTC);
        DATE_FMT.setTimeZone(UTC);
        TIME_FMT.setTimeZone(UTC);
    }

    private String encodeValue(Serializable value) {
        if (value == null) {
            return "N";
        } else if (value instanceof Integer) {
            return "I:" + value;
        } else if (value instanceof Long) {
            return "L:" + value;
        } else if (value instanceof Short) {
            return "SRT:" + value;
        } else if (value instanceof Byte) {
            return "BYT:" + value;
        } else if (value instanceof Float) {
            return "F:" + value;
        } else if (value instanceof Double) {
            return "D:" + value;
        } else if (value instanceof java.math.BigDecimal) {
            return "BD:" + ((java.math.BigDecimal) value).toPlainString();
        } else if (value instanceof java.sql.Timestamp) {
            synchronized (TIMESTAMP_FMT) {
                return "TS:" + escapeSpecialChars(TIMESTAMP_FMT.format((java.sql.Timestamp) value));
            }
        } else if (value instanceof java.sql.Date) {
            synchronized (DATE_FMT) {
                return "DT:" + DATE_FMT.format((java.sql.Date) value);
            }
        } else if (value instanceof java.sql.Time) {
            synchronized (TIME_FMT) {
                return "TM:" + escapeSpecialChars(TIME_FMT.format((java.sql.Time) value));
            }
        } else if (value instanceof java.util.Date) {
            synchronized (TIMESTAMP_FMT) {
                return "TS:" + escapeSpecialChars(TIMESTAMP_FMT.format((java.util.Date) value));
            }
        } else if (value instanceof Boolean) {
            return "BL:" + (((Boolean) value) ? "1" : "0");
        } else if (value instanceof byte[]) {
            return "B:" + Base64.getEncoder().encodeToString((byte[]) value);
        } else if (value instanceof String) {
            return "S:" + escapeSpecialChars((String) value);
        } else {
            return "S:" + escapeSpecialChars(value.toString());
        }
    }

    private String escapeSpecialChars(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\u0001': sb.append("\\u0001"); break;
                case '\\': sb.append("\\\\"); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }
    
    /**
     * Reconnect to MySQL server
     */
    private void reconnect() throws Exception {
        if (!running) {
            return;
        }
        
        logger.info("Attempting to reconnect to MySQL server...");
        
        RetryUtil.executeWithRetry(() -> {
            client.disconnect();
            client.connect(30000);
            return null;
        }, MAX_RETRIES, INITIAL_DELAY_MS, MAX_DELAY_MS);
        
        logger.info("Reconnected to MySQL server successfully");
    }
    
    @Override
    protected void doStop() throws Exception {
        logger.info("Stopping MySQL Binlog Capture...");
        
        if (client != null) {
            try {
                logger.info("Disconnecting from MySQL server...");
                client.disconnect();
            } catch (Exception e) {
                logger.error("Error disconnecting from MySQL server", e);
            }
        }
        
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.error("Error closing output stream", e);
            }
        }
        
        long duration = MonitorUtil.recordEndTime("capture");
        logger.info("MySQL Binlog Capture stopped. Total events captured: {}, Duration: {} ms", 
            eventCount.get(), duration);
        
        MonitorUtil.logStatistics();
    }
    
    public long getEventCount() {
        return eventCount.get();
    }
    
    public boolean isConnected() {
        return connected.get();
    }
    
    public String getCurrentOutputFile() {
        return currentOutputFile;
    }
}
