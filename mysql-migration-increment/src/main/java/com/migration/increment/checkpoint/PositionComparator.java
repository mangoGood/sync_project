package com.migration.increment.checkpoint;

import com.migration.binlog.core.BinlogPosition;
import com.migration.increment.parser.SqlFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 位点比较器
 * 支持 file position 和 GTID 两种模式的位点比较
 */
public class PositionComparator {
    private static final Logger logger = LoggerFactory.getLogger(PositionComparator.class);
    
    /**
     * 比较 SQL 条目和 checkpoint 位点
     * 
     * @param entry SQL 条目
     * @param checkpoint Checkpoint 位点
     * @return true 如果 entry 的位点大于 checkpoint 的位点
     */
    public static boolean isAfterCheckpoint(SqlFileParser.SqlEntry entry, BinlogPosition checkpoint) {
        if (checkpoint == null) {
            // 如果没有 checkpoint，所有 SQL 都需要执行
            return true;
        }
        
        if (entry == null) {
            return false;
        }
        
        // 优先使用 GTID 比较（如果两者都有 GTID）
        String entryGtid = entry.getGtid();
        String checkpointGtid = checkpoint.getGtid();
        
        if (entryGtid != null && !entryGtid.isEmpty() && 
            checkpointGtid != null && !checkpointGtid.isEmpty()) {
            return compareByGtid(entryGtid, checkpointGtid, entry, checkpoint);
        }
        
        // 使用 file position 比较
        return compareByFilePosition(entry, checkpoint);
    }
    
    /**
     * 通过 GTID 比较
     * GTID 格式: UUID:transaction_id 或 UUID:1-5
     */
    private static boolean compareByGtid(String entryGtid, String checkpointGtid, SqlFileParser.SqlEntry entry, BinlogPosition checkpoint) {
        try {
            // 解析 entry GTID
            String[] entryParts = entryGtid.split(":");
            String[] checkpointParts = checkpointGtid.split(":");
            
            if (entryParts.length != 2 || checkpointParts.length != 2) {
                logger.warn("GTID 格式不正确: entry={}, checkpoint={}", entryGtid, checkpointGtid);
                return compareByFilePosition(entry, checkpoint);
            }
            
            // 比较 UUID
            if (!entryParts[0].equals(checkpointParts[0])) {
                // 不同 UUID，认为是不同的 server，需要执行
                logger.warn("GTID UUID 不同: entry={}, checkpoint={}", entryParts[0], checkpointParts[0]);
                return true;
            }
            
            // 解析 transaction ID
            long entryTxId = parseTransactionId(entryParts[1]);
            long checkpointTxId = parseTransactionId(checkpointParts[1]);
            
            if (entryTxId > checkpointTxId) {
                return true;
            } else if (entryTxId < checkpointTxId) {
                return false;
            }
            
            // GTID 相同时，继续比较 position
            // 同一个 GTID 事务可能包含多个 SQL，每个 SQL 有不同的 position
            return compareByFilePosition(entry, checkpoint);
            
        } catch (Exception e) {
            logger.error("GTID 比较失败: entry={}, checkpoint={}", entryGtid, checkpointGtid, e);
            return compareByFilePosition(entry, checkpoint);
        }
    }
    
    /**
     * 解析 transaction ID
     * 支持格式: 5 或 1-5（取最大值）
     */
    private static long parseTransactionId(String txIdStr) {
        if (txIdStr.contains("-")) {
            // 范围格式，取最大值
            String[] range = txIdStr.split("-");
            return Long.parseLong(range[1]);
        }
        return Long.parseLong(txIdStr);
    }
    
    /**
     * 通过 file position 比较
     */
    private static boolean compareByFilePosition(SqlFileParser.SqlEntry entry, BinlogPosition checkpoint) {
        // 比较文件名
        int filenameCompare = entry.getFilename().compareTo(checkpoint.getFilename());
        if (filenameCompare > 0) {
            // entry 的文件名更大（更新）
            return true;
        } else if (filenameCompare < 0) {
            // entry 的文件名更小（更旧）
            return false;
        }
        
        // 文件名相同，比较 position
        return entry.getPosition() > checkpoint.getPosition();
    }
}
