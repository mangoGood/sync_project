package com.migration.capture;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventHeaderV4;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.migration.common.AbstractCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class MySQLBinlogCapture extends AbstractCapture<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(MySQLBinlogCapture.class);

    private static final char FIELD_SEP = '\001';
    private static final char RECORD_SEP = '\n';
    private static final String HEARTBEAT_TABLE = "__sync_heartbeat";
    private static final long HEARTBEAT_INTERVAL_MS = 5000;

    private String host;
    private int port;
    private String user;
    private String password;
    private String binlogFile;
    private long binlogPosition;
    private String outputDir;
    private String taskId;
    private long serverId;
    private String heartbeatDatabase;

    private BinaryLogClient client;
    private BufferedWriter writer;
    private final AtomicLong eventCounter = new AtomicLong(0);
    private final AtomicLong fileCounter = new AtomicLong(0);
    private long maxEventsPerFile = 10000;
    private long currentFileEvents = 0;

    private volatile String currentBinlogFile;
    private volatile long currentBinlogPosition;

    private Thread heartbeatThread;
    private Connection heartbeatConnection;
    private volatile long clockOffsetMs = 0;
    private volatile long lastRpoMs = -1;
    private volatile long lastHeartbeatSourceTs = -1;
    private volatile long lastSourceEventTs = -1;
    private volatile long lastRpoReportTime = 0;
    private static final long RPO_REPORT_INTERVAL_MS = 3000;
    private final Map<Long, String> tableIdToNameMap = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    protected void doInitialize() throws Exception {
        host = props.getProperty("source.db.host", "localhost");
        port = Integer.parseInt(props.getProperty("source.db.port", "3306"));
        user = props.getProperty("source.db.username", "root");
        password = props.getProperty("source.db.password", "");
        binlogFile = props.getProperty("capture.binlog.file", "");
        binlogPosition = Long.parseLong(props.getProperty("capture.binlog.position", "4"));
        outputDir = props.getProperty("capture.output.dir", "binlog_output");
        taskId = props.getProperty("task.id", "unknown");
        maxEventsPerFile = Long.parseLong(props.getProperty("capture.max.events.per.file", "10000"));
        serverId = Long.parseLong(props.getProperty("capture.server.id", "65535"));

        heartbeatDatabase = props.getProperty("source.db.database", "");
        if (heartbeatDatabase == null || heartbeatDatabase.isEmpty()) {
            String jdbcUrl = props.getProperty("source.db.jdbc.url", "");
            if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
                String urlPart = jdbcUrl.contains("?") ? jdbcUrl.substring(0, jdbcUrl.indexOf('?')) : jdbcUrl;
                int lastSlash = urlPart.lastIndexOf('/');
                if (lastSlash > 0 && lastSlash < urlPart.length() - 1) {
                    String dbFromUrl = urlPart.substring(lastSlash + 1);
                    if (!dbFromUrl.isEmpty()) {
                        heartbeatDatabase = dbFromUrl;
                    }
                }
            }
        }
        if (heartbeatDatabase == null || heartbeatDatabase.isEmpty()) {
            String syncObjects = props.getProperty("migration.sync.objects", "");
            if (syncObjects == null || syncObjects.isEmpty()) {
                syncObjects = props.getProperty("sync.objects", "");
            }
            if (syncObjects != null && !syncObjects.isEmpty() && syncObjects.startsWith("{")) {
                int firstQuote = syncObjects.indexOf('"');
                int secondQuote = syncObjects.indexOf('"', firstQuote + 1);
                if (firstQuote >= 0 && secondQuote > firstQuote) {
                    heartbeatDatabase = syncObjects.substring(firstQuote + 1, secondQuote);
                }
            }
        }
        if (heartbeatDatabase == null || heartbeatDatabase.isEmpty()) {
            String tables = props.getProperty("source.db.tables", "");
            if (tables != null && !tables.isEmpty()) {
                String firstTable = tables.split(",")[0].trim();
                if (firstTable.contains(".")) {
                    heartbeatDatabase = firstTable.substring(0, firstTable.indexOf('.'));
                }
            }
        }

        if (binlogFile.isEmpty()) {
            binlogFile = null;
            binlogPosition = 0;
        }

        logger.info("Capture initialized - host={}:{} user={} outputDir={} taskId={} binlogFile={} binlogPosition={} heartbeatDatabase={}",
                host, port, user, outputDir, taskId, binlogFile, binlogPosition, heartbeatDatabase);
    }

    @Override
    protected void doStart() throws Exception {
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        openNewOutputFile();

        client = new BinaryLogClient(host, port, user, password);
        client.setServerId(serverId);
        if (binlogFile != null && !binlogFile.isEmpty()) {
            client.setBinlogFilename(binlogFile);
            client.setBinlogPosition(binlogPosition);
            logger.info("Starting capture from binlog position: {}:{}", binlogFile, binlogPosition);
        } else {
            logger.info("Starting capture from latest binlog position");
        }

        client.registerEventListener(this::processEvent);

        client.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                logger.info("Connected to MySQL binlog stream");
            }

            @Override
            public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
                logger.error("Communication failure with MySQL: {}", ex.getMessage());
            }

            @Override
            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
                logger.error("Event deserialization failure: {}", ex.getMessage());
            }

            @Override
            public void onDisconnect(BinaryLogClient client) {
                logger.info("Disconnected from MySQL binlog stream");
            }
        });

        initClockOffset();
        startHeartbeat();

        client.connect();

        logger.info("Binlog capture started successfully for task: {}", taskId);
    }

    @Override
    protected void doStop() throws Exception {
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
        }
        if (heartbeatConnection != null) {
            try {
                heartbeatConnection.close();
            } catch (Exception e) {
                logger.warn("Error closing heartbeat connection: {}", e.getMessage());
            }
        }

        if (client != null) {
            try {
                client.disconnect();
            } catch (Exception e) {
                logger.warn("Error disconnecting binlog client: {}", e.getMessage());
            }
        }

        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (Exception e) {
                logger.warn("Error closing writer: {}", e.getMessage());
            }
        }

        savePosition();
        logger.info("Binlog capture stopped. Total events captured: {}, lastRpoMs: {}", eventCounter.get(), lastRpoMs);
    }

    private void processEvent(Event event) {
        if (!running) {
            return;
        }

        try {
            EventHeaderV4 header = (EventHeaderV4) event.getHeader();
            currentBinlogFile = client.getBinlogFilename();
            currentBinlogPosition = client.getBinlogPosition();

            String eventType = event.getHeader().getEventType().name();
            long timestamp = header.getTimestamp();
            long serverId = header.getServerId();

            EventData eventData = event.getData();
            if (eventData instanceof TableMapEventData) {
                TableMapEventData tableMap = (TableMapEventData) eventData;
                tableIdToNameMap.put(tableMap.getTableId(), tableMap.getDatabase() + "." + tableMap.getTable());
            }

            if (isHeartbeatEvent(eventData)) {
                long now = System.currentTimeMillis();
                lastRpoMs = now - timestamp + clockOffsetMs;
                lastHeartbeatSourceTs = timestamp;
                lastSourceEventTs = timestamp;
                writeRpoMetric(lastRpoMs);
                if (eventCounter.get() % 5000 == 0) {
                    logger.info("RPO heartbeat detected: sourceTstamp={}, rpoMs={}, clockOffsetMs={}", timestamp, lastRpoMs, clockOffsetMs);
                }
                return;
            }

            boolean isDataEvent = (eventData instanceof WriteRowsEventData)
                    || (eventData instanceof UpdateRowsEventData)
                    || (eventData instanceof DeleteRowsEventData);
            if (isDataEvent) {
                lastSourceEventTs = timestamp;
                long now = System.currentTimeMillis();
                long currentRpoMs = now - timestamp + clockOffsetMs;
                if (currentRpoMs >= 0 && (eventCounter.get() % 100 == 0 || now - lastRpoReportTime > RPO_REPORT_INTERVAL_MS)) {
                    lastRpoMs = currentRpoMs;
                    lastRpoReportTime = now;
                    writeRpoMetric(currentRpoMs);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(eventType).append(FIELD_SEP);
            sb.append(currentBinlogFile).append(FIELD_SEP);
            sb.append(currentBinlogPosition).append(FIELD_SEP);
            sb.append(timestamp).append(FIELD_SEP);
            sb.append(serverId).append(FIELD_SEP);

            String eventDataStr;
            if (eventData != null) {
                eventDataStr = serializeEventData(eventType, eventData);
            } else {
                eventDataStr = "";
            }
            eventDataStr = eventDataStr.replace("\n", " ").replace("\r", " ");
            sb.append(eventDataStr);

            sb.append(RECORD_SEP);

            writer.write(sb.toString());
            writer.flush();

            long count = eventCounter.incrementAndGet();
            currentFileEvents++;

            if (currentFileEvents >= maxEventsPerFile) {
                rotateOutputFile();
            }

            if (count % 1000 == 0) {
                logger.info("Captured {} events, current position: {}:{}", count, currentBinlogFile, currentBinlogPosition);
                savePosition();
            }
        } catch (Exception e) {
            logger.error("Error processing binlog event: {}", e.getMessage(), e);
        }
    }

    private synchronized void openNewOutputFile() throws IOException {
        if (writer != null) {
            writer.flush();
            writer.close();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String fileName = String.format("binlog_%s_%04d.cap", timestamp, fileCounter.get());

        File outputFile = new File(outputDir, fileName);
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8));
        currentFileEvents = 0;

        logger.info("Opened new capture output file: {}", outputFile.getAbsolutePath());
    }

    private synchronized void rotateOutputFile() throws IOException {
        fileCounter.incrementAndGet();
        openNewOutputFile();
        logger.info("Rotated to new capture output file after {} events", maxEventsPerFile);
    }

    private String serializeEventData(String eventType, EventData eventData) {
        try {
            if (eventData instanceof WriteRowsEventData) {
                return serializeWriteRows((WriteRowsEventData) eventData);
            } else if (eventData instanceof UpdateRowsEventData) {
                return serializeUpdateRows((UpdateRowsEventData) eventData);
            } else if (eventData instanceof DeleteRowsEventData) {
                return serializeDeleteRows((DeleteRowsEventData) eventData);
            } else if (eventData instanceof TableMapEventData) {
                return eventData.toString();
            } else {
                return eventData.toString();
            }
        } catch (Exception e) {
            logger.warn("Failed to serialize event data, falling back to toString(): {}", e.getMessage());
            return eventData.toString();
        }
    }

    private String serializeWriteRows(WriteRowsEventData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("WriteRowsEventData{tableId=").append(data.getTableId());
        sb.append(", includedColumns={").append(data.getIncludedColumns().toString()).append("}");
        sb.append(", rows=[");
        List<Serializable[]> rows = data.getRows();
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0) sb.append(", ");
            sb.append("[");
            Serializable[] row = rows.get(r);
            for (int i = 0; i < row.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(serializeValue(row[i]));
            }
            sb.append("]");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String serializeUpdateRows(UpdateRowsEventData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("UpdateRowsEventData{tableId=").append(data.getTableId());
        sb.append(", includedColumnsBeforeUpdate={").append(data.getIncludedColumnsBeforeUpdate().toString()).append("}");
        sb.append(", includedColumns={").append(data.getIncludedColumns().toString()).append("}");
        sb.append(", rows=[");
        List<Map.Entry<Serializable[], Serializable[]>> rows = data.getRows();
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0) sb.append(", ");
            Map.Entry<Serializable[], Serializable[]> entry = rows.get(r);
            sb.append("before=[");
            Serializable[] before = entry.getKey();
            for (int i = 0; i < before.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(serializeValue(before[i]));
            }
            sb.append("], after=[");
            Serializable[] after = entry.getValue();
            for (int i = 0; i < after.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(serializeValue(after[i]));
            }
            sb.append("]");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String serializeDeleteRows(DeleteRowsEventData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("DeleteRowsEventData{tableId=").append(data.getTableId());
        sb.append(", includedColumns={").append(data.getIncludedColumns().toString()).append("}");
        sb.append(", rows=[");
        List<Serializable[]> rows = data.getRows();
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0) sb.append(", ");
            sb.append("[");
            Serializable[] row = rows.get(r);
            for (int i = 0; i < row.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(serializeValue(row[i]));
            }
            sb.append("]");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String serializeValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof byte[]) {
            return "0x" + bytesToHex((byte[]) value);
        }
        if (value instanceof java.sql.Timestamp) {
            java.sql.Timestamp ts = (java.sql.Timestamp) value;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return sdf.format(ts);
        }
        if (value instanceof java.sql.Date) {
            return value.toString();
        }
        if (value instanceof java.sql.Time) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return sdf.format((java.sql.Time) value);
        }
        if (value instanceof java.util.Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return sdf.format((java.util.Date) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void savePosition() {
        if (currentBinlogFile == null) return;

        File positionFile = new File(outputDir, "capture_position.properties");
        Properties posProps = new Properties();
        posProps.setProperty("binlog.file", currentBinlogFile);
        posProps.setProperty("binlog.position", String.valueOf(currentBinlogPosition));
        posProps.setProperty("last.update", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        try (FileOutputStream fos = new FileOutputStream(positionFile)) {
            posProps.store(fos, "Capture position for task: " + taskId);
        } catch (IOException e) {
            logger.warn("Failed to save capture position: {}", e.getMessage());
        }
    }

    public String getCurrentBinlogFile() {
        return currentBinlogFile;
    }

    public long getCurrentBinlogPosition() {
        return currentBinlogPosition;
    }

    public long getEventCount() {
        return eventCounter.get();
    }

    public long getLastRpoMs() {
        return lastRpoMs;
    }

    private void initClockOffset() {
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&serverTimezone=UTC";
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT UNIX_TIMESTAMP(NOW(3))*1000")) {
                if (rs.next()) {
                    long sourceDbTime = rs.getLong(1);
                    long localTime = System.currentTimeMillis();
                    clockOffsetMs = sourceDbTime - localTime;
                    logger.info("Clock offset calculated: sourceDbTime={}, localTime={}, offsetMs={}", sourceDbTime, localTime, clockOffsetMs);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to calculate clock offset, assuming clocks are synchronized: {}", e.getMessage());
            clockOffsetMs = 0;
        }
    }

    private void startHeartbeat() {
        try {
            String db = (heartbeatDatabase != null && !heartbeatDatabase.isEmpty()) ? heartbeatDatabase : "mysql";
            String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&serverTimezone=UTC";
            heartbeatConnection = DriverManager.getConnection(url, user, password);

            try (Statement stmt = heartbeatConnection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS " + HEARTBEAT_TABLE + " (id INT PRIMARY KEY, ts BIGINT, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
                logger.info("Heartbeat table ensured: {}.{}", db, HEARTBEAT_TABLE);
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize heartbeat table: {}", e.getMessage());
            return;
        }

        heartbeatThread = new Thread(() -> {
            logger.info("Heartbeat thread started, interval={}ms", HEARTBEAT_INTERVAL_MS);
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL_MS);
                    if (!running) break;

                    try (Statement stmt = heartbeatConnection.createStatement()) {
                        stmt.execute("INSERT INTO " + HEARTBEAT_TABLE + " (id, ts) VALUES (1, UNIX_TIMESTAMP(NOW(3))*1000) " +
                                "ON DUPLICATE KEY UPDATE ts = UNIX_TIMESTAMP(NOW(3))*1000");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.warn("Heartbeat write failed: {}", e.getMessage());
                    try {
                        if (heartbeatConnection != null && !heartbeatConnection.isValid(2)) {
                            String db = (heartbeatDatabase != null && !heartbeatDatabase.isEmpty()) ? heartbeatDatabase : "mysql";
                            String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&serverTimezone=UTC";
                            heartbeatConnection = DriverManager.getConnection(url, user, password);
                            logger.info("Heartbeat connection re-established");
                        }
                    } catch (Exception ex) {
                        logger.warn("Heartbeat connection reconnect failed: {}", ex.getMessage());
                    }
                }
            }
            logger.info("Heartbeat thread stopped");
        }, "Heartbeat-" + taskId);
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    private boolean isHeartbeatEvent(EventData eventData) {
        try {
            if (eventData instanceof UpdateRowsEventData) {
                UpdateRowsEventData data = (UpdateRowsEventData) eventData;
                String tableName = tableIdToNameMap.get(data.getTableId());
                return tableName != null && tableName.endsWith("." + HEARTBEAT_TABLE);
            } else if (eventData instanceof WriteRowsEventData) {
                WriteRowsEventData data = (WriteRowsEventData) eventData;
                String tableName = tableIdToNameMap.get(data.getTableId());
                return tableName != null && tableName.endsWith("." + HEARTBEAT_TABLE);
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    private void writeRpoMetric(long rpoMs) {
        String metricsDir = outputDir;
        if (metricsDir == null) {
            metricsDir = "binlog_output";
        }
        File dir = new File(metricsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File metricFile = new File(dir, "rpo_metric");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(metricFile, false))) {
            pw.println(System.currentTimeMillis() + "|" + rpoMs + "|" + lastSourceEventTs);
        } catch (IOException e) {
            logger.warn("Failed to write RPO metric: {}", e.getMessage());
        }
    }
}
