package com.migration.increment.executor;

import com.migration.binlog.core.BinlogPosition;
import com.migration.db.DatabaseConnection;
import com.migration.increment.checkpoint.CheckpointManager;
import com.migration.increment.checkpoint.PositionComparator;
import com.migration.increment.parser.SqlFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 增量同步执行器
 * 持续监听 SQL 文件目录，执行大于 checkpoint 位点的 SQL
 */
public class IncrementExecutor {
    private static final Logger logger = LoggerFactory.getLogger(IncrementExecutor.class);
    
    // 默认扫描间隔（毫秒）
    private static final long DEFAULT_SCAN_INTERVAL_MS = 5000;
    // 默认文件读取缓冲区大小
    private static final int BUFFER_SIZE = 8192;
    
    private DatabaseConnection targetConnection;
    private CheckpointManager checkpointManager;
    private SqlFileParser sqlFileParser;
    private String sqlDirectory;
    private long scanIntervalMs;
    
    // 记录每个文件的最后读取位置（文件名 -> 文件偏移量）
    private Map<String, Long> fileReadPositions;
    // 记录已处理过的 SQL 条目，避免重复执行
    private Set<String> processedEntries;
    
    private ExecutorService executorService;
    private AtomicBoolean running;
    private Future<?> watchTask;
    
    public IncrementExecutor(DatabaseConnection targetConnection, 
                            String checkpointDbPath, 
                            String sqlDirectory) {
        this(targetConnection, checkpointDbPath, sqlDirectory, DEFAULT_SCAN_INTERVAL_MS);
    }
    
    public IncrementExecutor(DatabaseConnection targetConnection, 
                            String checkpointDbPath, 
                            String sqlDirectory,
                            long scanIntervalMs) {
        this.targetConnection = targetConnection;
        this.checkpointManager = new CheckpointManager(checkpointDbPath);
        this.sqlFileParser = new SqlFileParser();
        this.sqlDirectory = sqlDirectory;
        this.scanIntervalMs = scanIntervalMs;
        this.fileReadPositions = new ConcurrentHashMap<>();
        this.processedEntries = ConcurrentHashMap.newKeySet();
        this.executorService = Executors.newSingleThreadExecutor();
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * 启动持续监听模式
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("启动增量同步监听模式...");
            logger.info("SQL 目录: {}", sqlDirectory);
            logger.info("扫描间隔: {} ms", scanIntervalMs);
            
            // 先执行一次历史 SQL
            executeHistoricalSql();
            
            // 启动监听任务
            watchTask = executorService.submit(this::watchDirectory);
            
            logger.info("增量同步监听已启动");
        } else {
            logger.warn("增量同步已经在运行中");
        }
    }
    
    /**
     * 停止监听
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("正在停止增量同步监听...");
            
            if (watchTask != null) {
                watchTask.cancel(true);
            }
            
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            logger.info("增量同步监听已停止");
        }
    }
    
    /**
     * 执行历史 SQL（启动时执行一次）
     */
    private void executeHistoricalSql() {
        logger.info("执行历史 SQL...");
        
        try {
            // 加载 checkpoint
            BinlogPosition checkpoint = checkpointManager.loadCheckpoint();
            if (checkpoint == null) {
                logger.warn("未找到 checkpoint，将执行所有 SQL");
            } else {
                logger.info("当前 checkpoint: {}", checkpoint);
            }
            
            // 扫描目录中的所有 SQL 文件
            File dir = new File(sqlDirectory);
            if (!dir.exists() || !dir.isDirectory()) {
                logger.warn("SQL 目录不存在: {}", sqlDirectory);
                return;
            }
            
            File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
            if (files == null || files.length == 0) {
                logger.info("目录中没有 SQL 文件");
                return;
            }
            
            // 按文件名排序
            Arrays.sort(files, Comparator.comparing(File::getName));
            
            for (File file : files) {
                if (!running.get()) {
                    break;
                }
                processFile(file, checkpoint);
            }
            
        } catch (Exception e) {
            logger.error("执行历史 SQL 失败", e);
        }
    }
    
    /**
     * 持续监听目录
     */
    private void watchDirectory() {
        while (running.get()) {
            try {
                // 扫描新文件或更新的文件
                scanForNewFiles();
                
                // 等待下一次扫描
                Thread.sleep(scanIntervalMs);
                
            } catch (InterruptedException e) {
                logger.info("监听线程被中断");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("扫描目录时发生错误", e);
            }
        }
    }
    
    /**
     * 扫描新文件或更新的文件
     */
    private void scanForNewFiles() {
        try {
            File dir = new File(sqlDirectory);
            if (!dir.exists() || !dir.isDirectory()) {
                return;
            }
            
            File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
            if (files == null) {
                return;
            }
            
            // 加载当前 checkpoint
            BinlogPosition checkpoint = checkpointManager.loadCheckpoint();
            
            for (File file : files) {
                if (!running.get()) {
                    break;
                }
                
                String filename = file.getName();
                long lastModified = file.lastModified();
                long lastReadPosition = fileReadPositions.getOrDefault(filename, 0L);
                long fileSize = file.length();
                
                // 如果文件有更新（大小变化或修改时间变化）
                if (fileSize > lastReadPosition) {
                    logger.debug("检测到文件更新: {}, 上次读取位置: {}, 当前大小: {}", 
                            filename, lastReadPosition, fileSize);
                    processFileIncremental(file, checkpoint, lastReadPosition);
                }
            }
            
        } catch (Exception e) {
            logger.error("扫描文件失败", e);
        }
    }
    
    /**
     * 处理文件（首次处理，从头读取）
     */
    private void processFile(File file, BinlogPosition checkpoint) {
        processFileIncremental(file, checkpoint, 0);
    }
    
    /**
     * 增量处理文件（从指定位置开始读取）
     */
    private void processFileIncremental(File file, BinlogPosition checkpoint, long startPosition) {
        String filename = file.getName();
        logger.info("处理文件: {}, 从位置: {} 开始", filename, startPosition);
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // 跳转到上次读取的位置
            raf.seek(startPosition);
            
            StringBuilder content = new StringBuilder();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = raf.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
            
            // 记录当前读取位置
            long currentPosition = raf.getFilePointer();
            fileReadPositions.put(filename, currentPosition);
            
            // 解析并执行 SQL
            if (content.length() > 0) {
                List<SqlFileParser.SqlEntry> entries = parseContent(filename, content.toString());
                executeEntries(entries, checkpoint);
            }
            
        } catch (IOException e) {
            logger.error("处理文件失败: {}", filename, e);
        }
    }
    
    /**
     * 解析内容中的 SQL 条目
     */
    private List<SqlFileParser.SqlEntry> parseContent(String filename, String content) {
        List<SqlFileParser.SqlEntry> entries = new ArrayList<>();
        
        String[] lines = content.split("\n");
        String currentFilename = null;
        long currentPosition = -1;
        String currentGtid = null;
        StringBuilder currentSql = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            
            // 跳过空行和注释
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            
            // 解析 POSITION
            if (line.startsWith("[POSITION]")) {
                // 如果已有 SQL，保存之前的条目
                if (currentSql.length() > 0 && currentFilename != null) {
                    entries.add(new SqlFileParser.SqlEntry(
                            currentFilename, currentPosition, currentGtid, 
                            currentSql.toString().trim()));
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
            entries.add(new SqlFileParser.SqlEntry(
                    currentFilename, currentPosition, currentGtid, 
                    currentSql.toString().trim()));
        }
        
        return entries;
    }
    
    /**
     * 执行 SQL 条目列表
     */
    private void executeEntries(List<SqlFileParser.SqlEntry> entries, BinlogPosition checkpoint) {
        int executedCount = 0;
        BinlogPosition lastPosition = null;
        
        for (SqlFileParser.SqlEntry entry : entries) {
            if (!running.get()) {
                break;
            }
            
            // 生成唯一标识，避免重复执行
            String entryId = entry.getFilename() + ":" + entry.getPosition() + ":" + entry.getSql().hashCode();
            
            if (processedEntries.contains(entryId)) {
                logger.debug("跳过已执行的 SQL: {}:{}", entry.getFilename(), entry.getPosition());
                continue;
            }
            
            // 检查是否大于 checkpoint
            if (PositionComparator.isAfterCheckpoint(entry, checkpoint)) {
                try {
                    executeSql(entry.getSql());
                    processedEntries.add(entryId);
                    executedCount++;
                    lastPosition = entry.toBinlogPosition();
                    
                    // 每执行成功一条 SQL 就更新 checkpoint
                    checkpointManager.saveCheckpoint(lastPosition);
                    
                    logger.info("执行 SQL 成功: {}:{}, 已更新位点", entry.getFilename(), entry.getPosition());
                    
                } catch (SQLException e) {
                    logger.error("执行 SQL 失败: {}", entry.getSql(), e);
                    // 继续执行下一条
                }
            } else {
                logger.debug("跳过 SQL（已在 checkpoint 之前）: {}:{}", 
                        entry.getFilename(), entry.getPosition());
                processedEntries.add(entryId);
            }
        }
        
        if (executedCount > 0) {
            logger.info("本次共执行 {} 条 SQL", executedCount);
        }
    }
    
    /**
     * 执行单条 SQL
     */
    private void executeSql(String sql) throws SQLException {
        try (Statement stmt = targetConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        stop();
        checkpointManager.close();
        if (targetConnection != null) {
            targetConnection.close();
        }
    }
}
