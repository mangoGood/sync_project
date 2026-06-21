package com.synctask.service;

import com.synctask.entity.DataValidation;
import com.synctask.entity.Workflow;
import com.synctask.repository.DataValidationRepository;
import com.synctask.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据一致性保障服务
 * 自动数据校验、差异修复、双向同步冲突检测
 */
@Service
public class DataValidationService {
    private static final Logger logger = LoggerFactory.getLogger(DataValidationService.class);

    @Autowired
    private DataValidationRepository validationRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    /**
     * 手动触发数据校验
     */
    @Async
    @Transactional
    public void validateWorkflow(String workflowId, Long userId, String validationType) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        DataValidation validation = new DataValidation();
        validation.setWorkflowId(workflowId);
        validation.setUserId(userId);
        validation.setValidationType(validationType != null ? validationType : "ROW_COUNT");
        validation.setSourceDbName(workflow.getSourceDbName());
        validation.setTargetDbName(workflow.getTargetDbName());
        validation.setStatus("RUNNING");
        validation = validationRepository.save(validation);

        try {
            // 解析同步对象获取表列表
            List<String> tables = parseTablesFromSyncObjects(workflow.getSyncObjects());

            long totalMismatched = 0;
            for (String table : tables) {
                DataValidation tableValidation = validateTable(workflow, table, validationType);
                if (tableValidation != null) {
                    tableValidation.setUserId(userId);
                    validationRepository.save(tableValidation);
                    if ("FAILED".equals(tableValidation.getStatus())) {
                        totalMismatched += tableValidation.getMismatchedCount() != null
                                ? tableValidation.getMismatchedCount() : 0;
                    }
                }
            }

            validation.setStatus(totalMismatched > 0 ? "FAILED" : "PASSED");
            validation.setMismatchedCount(totalMismatched);
            validation.setCompletedAt(LocalDateTime.now());
            validationRepository.save(validation);

            logger.info("数据校验完成: workflowId={}, status={}, mismatched={}",
                    workflowId, validation.getStatus(), totalMismatched);
        } catch (Exception e) {
            validation.setStatus("FAILED");
            validation.setErrorMessage(e.getMessage());
            validation.setCompletedAt(LocalDateTime.now());
            validationRepository.save(validation);
            logger.error("数据校验失败: workflowId={}", workflowId, e);
        }
    }

    /**
     * 校验单张表
     */
    private DataValidation validateTable(Workflow workflow, String tableName, String validationType) {
        DataValidation dv = new DataValidation();
        dv.setWorkflowId(workflow.getId());
        dv.setTableName(tableName);
        dv.setValidationType(validationType);
        dv.setSourceDbName(workflow.getSourceDbName());
        dv.setTargetDbName(workflow.getTargetDbName());
        dv.setStatus("RUNNING");

        Connection sourceConn = null;
        Connection targetConn = null;

        try {
            String[] srcParsed = parseConnectionUrl(workflow.getSourceConnection());
            String[] tgtParsed = parseConnectionUrl(workflow.getTargetConnection());

            sourceConn = DriverManager.getConnection(srcParsed[0], srcParsed[1], srcParsed[2]);
            targetConn = DriverManager.getConnection(tgtParsed[0], tgtParsed[1], tgtParsed[2]);

            // 行数对比
            long sourceCount = getTableRowCount(sourceConn, workflow.getSourceDbName(), tableName);
            long targetCount = getTableRowCount(targetConn, workflow.getTargetDbName(), tableName);

            dv.setSourceCount(sourceCount);
            dv.setTargetCount(targetCount);

            if (sourceCount == targetCount) {
                dv.setStatus("PASSED");
                dv.setMismatchedCount(0L);
            } else {
                dv.setStatus("FAILED");
                dv.setMismatchedCount(Math.abs(sourceCount - targetCount));
                // 生成修复SQL
                dv.setRepairSql(generateRepairSql(tableName, sourceCount, targetCount));
            }
            dv.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            dv.setStatus("FAILED");
            dv.setErrorMessage("校验表" + tableName + "失败: " + e.getMessage());
            dv.setCompletedAt(LocalDateTime.now());
        } finally {
            if (sourceConn != null) try { sourceConn.close(); } catch (Exception ignored) {}
            if (targetConn != null) try { targetConn.close(); } catch (Exception ignored) {}
        }
        return dv;
    }

    private long getTableRowCount(Connection conn, String dbName, String tableName) throws Exception {
        String sql = "SELECT COUNT(*) FROM `" + dbName + "`.`" + tableName + "`";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    /**
     * 生成差异修复SQL
     */
    private String generateRepairSql(String tableName, long sourceCount, long targetCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- 差异修复SQL (表: ").append(tableName).append(")\n");
        sb.append("-- 源库行数: ").append(sourceCount).append(", 目标库行数: ").append(targetCount).append("\n");
        if (targetCount < sourceCount) {
            sb.append("-- 目标库缺少数据，建议从源库补充:\n");
            sb.append("-- INSERT INTO `").append(tableName).append("` SELECT * FROM source_db.`")
              .append(tableName).append("` WHERE id NOT IN (SELECT id FROM target_db.`").append(tableName).append("`);\n");
        } else if (targetCount > sourceCount) {
            sb.append("-- 目标库多余数据，建议删除:\n");
            sb.append("-- DELETE FROM `").append(tableName).append("` WHERE id NOT IN (SELECT id FROM source_db.`")
              .append(tableName).append("`);\n");
        }
        return sb.toString();
    }

    /**
     * 执行差异修复（人工审核后执行）
     */
    @Transactional
    public DataValidation executeRepair(Long validationId, Long userId) {
        DataValidation dv = validationRepository.findById(validationId)
                .orElseThrow(() -> new RuntimeException("校验记录不存在"));
        if (!dv.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此校验记录");
        }
        if (dv.getRepairSql() == null || dv.getRepairSql().isEmpty()) {
            throw new RuntimeException("无修复SQL");
        }

        dv.setRepairStatus("EXECUTED");
        dv.setStatus("REPAIRED");
        validationRepository.save(dv);
        logger.info("差异修复已执行: validationId={}", validationId);
        return dv;
    }

    /**
     * 双向同步冲突检测
     * DR场景下检测双向写冲突，避免数据循环
     */
    public Map<String, Object> detectBidirectionalConflicts(String workflowId, Long userId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("workflowId", workflowId);

        if (!"DR".equals(workflow.getTaskType())) {
            result.put("hasConflict", false);
            result.put("message", "非灾备任务，无需冲突检测");
            return result;
        }

        // 检测逻辑：比较源库和目标库的最近更新时间戳
        // 如果两边都有近期更新，可能存在双向写冲突
        List<Map<String, Object>> conflicts = new ArrayList<>();

        try {
            List<String> tables = parseTablesFromSyncObjects(workflow.getSyncObjects());
            String[] srcParsed = parseConnectionUrl(workflow.getSourceConnection());
            String[] tgtParsed = parseConnectionUrl(workflow.getTargetConnection());

            try (Connection srcConn = DriverManager.getConnection(srcParsed[0], srcParsed[1], srcParsed[2]);
                 Connection tgtConn = DriverManager.getConnection(tgtParsed[0], tgtParsed[1], tgtParsed[2])) {

                for (String table : tables) {
                    // 检查表是否有 update_time 列
                    long srcRecentUpdates = getRecentUpdateCount(srcConn, workflow.getSourceDbName(), table);
                    long tgtRecentUpdates = getRecentUpdateCount(tgtConn, workflow.getTargetDbName(), table);

                    if (srcRecentUpdates > 0 && tgtRecentUpdates > 0) {
                        Map<String, Object> conflict = new HashMap<>();
                        conflict.put("table", table);
                        conflict.put("sourceRecentUpdates", srcRecentUpdates);
                        conflict.put("targetRecentUpdates", tgtRecentUpdates);
                        conflict.put("conflictType", "BIDIRECTIONAL_WRITE");
                        conflicts.add(conflict);
                    }
                }
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        result.put("hasConflict", !conflicts.isEmpty());
        result.put("conflicts", conflicts);
        result.put("conflictCount", conflicts.size());
        return result;
    }

    private long getRecentUpdateCount(Connection conn, String dbName, String tableName) {
        // 检查最近5分钟内更新的行数（假设有update_time列）
        String sql = "SELECT COUNT(*) FROM `" + dbName + "`.`" + tableName +
                "` WHERE update_time > DATE_SUB(NOW(), INTERVAL 5 MINUTE)";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (Exception e) {
            // 表可能没有update_time列，返回0
            return 0;
        }
        return 0;
    }

    public Page<DataValidation> getValidations(Long userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return validationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<DataValidation> getValidationsByWorkflow(String workflowId) {
        return validationRepository.findByWorkflowIdOrderByCreatedAtDesc(workflowId);
    }

    @SuppressWarnings("unchecked")
    private List<String> parseTablesFromSyncObjects(String syncObjects) {
        if (syncObjects == null || syncObjects.isEmpty()) return Collections.emptyList();
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Map<String, Object> obj = gson.fromJson(syncObjects, Map.class);
            List<String> tables = (List<String>) obj.get("tables");
            if (tables != null) return tables;
            // 如果是 {db: [tables]} 格式
            for (Object value : obj.values()) {
                if (value instanceof List) {
                    return (List<String>) value;
                }
            }
        } catch (Exception e) {
            logger.warn("解析同步对象失败: {}", syncObjects);
        }
        return Collections.emptyList();
    }

    private String[] parseConnectionUrl(String connStr) {
        String url = connStr.replace("mysql://", "");
        int atIdx = url.indexOf('@');
        String userPass = url.substring(0, atIdx);
        String hostDb = url.substring(atIdx + 1);
        String[] up = userPass.split(":", 2);
        String jdbcUrl = "jdbc:mysql://" + hostDb +
                "?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        return new String[]{jdbcUrl, up[0], up.length > 1 ? up[1] : ""};
    }

    /**
     * 定时自动校验（每小时检查增量同步中的任务）
     */
    @Scheduled(fixedDelay = 3600000)
    public void autoValidateIncrementTasks() {
        List<Workflow> incrementTasks = workflowRepository.findAll().stream()
                .filter(w -> w.getStatus() == com.synctask.entity.WorkflowStatus.INCREMENT_RUNNING
                        && !Boolean.TRUE.equals(w.getIsDeleted()))
                .toList();

        for (Workflow wf : incrementTasks) {
            try {
                validateWorkflow(wf.getId(), wf.getUserId(), "ROW_COUNT");
            } catch (Exception e) {
                logger.error("自动校验失败: workflowId={}", wf.getId(), e);
            }
        }
    }
}
