package com.synctask.service;

import com.synctask.dto.TaskStatusUpdate;
import com.synctask.entity.Workflow;
import com.synctask.entity.WorkflowLog;
import com.synctask.entity.WorkflowStatus;
import com.synctask.repository.WorkflowLogRepository;
import com.synctask.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowLogRepository workflowLogRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private long serviceStartTime;

    @PostConstruct
    public void init() {
        serviceStartTime = System.currentTimeMillis();
        logger.info("KafkaConsumerService 初始化，启动时间戳: {}", serviceStartTime);
    }

    @KafkaListener(topics = "${spring.kafka.topics.task-status}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeTaskStatusMessage(Map<String, Object> messageMap) {
        logger.info("收到任务状态消息: {}", messageMap);

        try {
            Long messageTimestamp = getLongValue(messageMap, "timestamp");
            if (messageTimestamp != null && messageTimestamp < serviceStartTime) {
                logger.info("忽略旧消息: 消息时间戳 {} 早于服务启动时间 {}", messageTimestamp, serviceStartTime);
                return;
            }
            
            String taskId = getStringValue(messageMap, "taskId");
            String status = getStringValue(messageMap, "status");
            Integer progress = getIntegerValue(messageMap, "progress");
            String errorMessage = getStringValue(messageMap, "errorMessage");
            Boolean isBilling = getBooleanValue(messageMap, "isBilling");
            
            Integer totalTables = getIntegerValue(messageMap, "totalTables");
            Integer completedTables = getIntegerValue(messageMap, "completedTables");
            String currentTable = getStringValue(messageMap, "currentTable");
            Integer currentTableProgress = getIntegerValue(messageMap, "currentTableProgress");
            Long currentTableRows = getLongValue(messageMap, "currentTableRows");
            Long currentTableTotalRows = getLongValue(messageMap, "currentTableTotalRows");

            logger.info("解析消息: taskId={}, status={}, progress={}", taskId, status, progress);

            Workflow workflow = workflowRepository.findById(taskId).orElse(null);
            if (workflow == null) {
                logger.warn("任务不存在: taskId={}", taskId);
                return;
            }

            WorkflowStatus newStatus = WorkflowStatus.valueOf(status.toUpperCase());
            WorkflowStatus oldStatus = workflow.getStatus();
            
            if (oldStatus == WorkflowStatus.COMPLETED || oldStatus == WorkflowStatus.FAILED) {
                logger.info("任务已处于终态 {}，忽略状态更新消息: newStatus={}", oldStatus, newStatus);
                return;
            }
            
            workflow.setStatus(newStatus);

            if (progress != null) {
                workflow.setProgress(progress);
            }

            if (errorMessage != null) {
                workflow.setErrorMessage(errorMessage);
            }

            if (isBilling != null) {
                workflow.setIsBilling(isBilling);
            }
            
            if (totalTables != null) {
                workflow.setTotalTables(totalTables);
            }
            if (completedTables != null) {
                workflow.setCompletedTables(completedTables);
            }
            if (currentTable != null) {
                workflow.setCurrentTable(currentTable);
            }
            if (currentTableProgress != null) {
                workflow.setCurrentTableProgress(currentTableProgress);
            }
            if (currentTableRows != null) {
                workflow.setCurrentTableRows(currentTableRows);
            }
            if (currentTableTotalRows != null) {
                workflow.setCurrentTableTotalRows(currentTableTotalRows);
            }

            String migrationMode = workflow.getMigrationMode();
            boolean isFullAndIncre = "fullAndIncre".equals(migrationMode);
            
            if (newStatus == WorkflowStatus.COMPLETED || newStatus == WorkflowStatus.FAILED) {
                workflow.setCompletedAt(LocalDateTime.now());
                workflow.setIsBilling(false);
            } else if (newStatus == WorkflowStatus.FULL_COMPLETED) {
                if (isFullAndIncre) {
                    // 全量+增量模式：全量完成后继续增量同步，保持计费
                    workflow.setIsBilling(true);
                } else {
                    // 仅全量模式：全量完成后任务结束，停止计费
                    workflow.setCompletedAt(LocalDateTime.now());
                    workflow.setIsBilling(false);
                }
            } else if (newStatus == WorkflowStatus.INCREMENT_RUNNING) {
                workflow.setIsBilling(true);
            }

            workflowRepository.save(workflow);
            logger.info("任务状态已更新: taskId={}, status={}", workflow.getId(), workflow.getStatus());

            String logMessage = buildStatusLogMessage(newStatus, oldStatus, progress, errorMessage, 
                totalTables, completedTables, currentTable, currentTableProgress);
            WorkflowLog.LogLevel logLevel = determineLogLevel(newStatus);
            
            addLog(workflow.getId(), logLevel, logMessage);

            TaskStatusUpdate update = new TaskStatusUpdate();
            update.setTaskId(workflow.getId());
            update.setStatus(workflow.getStatus().name());
            update.setProgress(workflow.getProgress());
            update.setUserId(workflow.getUserId());
            update.setMigrationMode(workflow.getMigrationMode());
            update.setTotalTables(workflow.getTotalTables());
            update.setCompletedTables(workflow.getCompletedTables());
            update.setCurrentTable(workflow.getCurrentTable());
            update.setCurrentTableProgress(workflow.getCurrentTableProgress());
            update.setCurrentTableRows(workflow.getCurrentTableRows());
            update.setCurrentTableTotalRows(workflow.getCurrentTableTotalRows());

            messagingTemplate.convertAndSend("/topic/task-status", update);
            messagingTemplate.convertAndSendToUser(
                workflow.getUserId().toString(), 
                "/queue/task-status", 
                update
            );
            logger.info("任务状态更新已推送到 WebSocket: taskId={}, userId={}", workflow.getId(), workflow.getUserId());

        } catch (Exception e) {
            logger.error("处理任务状态消息失败: {}", messageMap, e);
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.valueOf(String.valueOf(value));
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.valueOf(String.valueOf(value));
    }

    private Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.valueOf(String.valueOf(value));
    }

    private String buildStatusLogMessage(WorkflowStatus newStatus, WorkflowStatus oldStatus, Integer progress, String errorMessage,
            Integer totalTables, Integer completedTables, String currentTable, Integer currentTableProgress) {
        switch (newStatus) {
            case STARTING:
                return "任务启动中";
            case FULL_MIGRATING:
                StringBuilder msg = new StringBuilder("全量同步中");
                if (totalTables != null && completedTables != null) {
                    msg.append(String.format("，表进度: %d/%d", completedTables, totalTables));
                }
                if (currentTable != null) {
                    msg.append(String.format("，当前表: %s", currentTable));
                    if (currentTableProgress != null) {
                        msg.append(String.format(" (%d%%)", currentTableProgress));
                    }
                }
                return msg.toString();
            case FULL_COMPLETED:
                return "全量同步完成";
            case INCREMENT_RUNNING:
                return "增量同步中";
            case COMPLETED:
                return "任务执行完成";
            case FAILED:
                return errorMessage != null && !errorMessage.isEmpty() 
                    ? String.format("任务执行失败: %s", errorMessage)
                    : "任务执行失败";
            case PAUSED:
                return "任务已暂停";
            default:
                return String.format("任务状态更新为: %s", newStatus.name());
        }
    }

    private WorkflowLog.LogLevel determineLogLevel(WorkflowStatus status) {
        switch (status) {
            case FAILED:
                return WorkflowLog.LogLevel.ERROR;
            case PAUSED:
                return WorkflowLog.LogLevel.WARNING;
            default:
                return WorkflowLog.LogLevel.INFO;
        }
    }

    private void addLog(String workflowId, WorkflowLog.LogLevel level, String message) {
        WorkflowLog log = new WorkflowLog();
        log.setWorkflowId(workflowId);
        log.setLevel(level);
        log.setMessage(message);
        workflowLogRepository.save(log);
        logger.info("已添加任务日志: workflowId={}, level={}, message={}", workflowId, level, message);
    }
}
