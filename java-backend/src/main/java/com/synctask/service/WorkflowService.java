package com.synctask.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synctask.dto.TaskCreatedMessage;
import com.synctask.entity.Workflow;
import com.synctask.entity.WorkflowLog;
import com.synctask.entity.WorkflowStatus;
import com.synctask.repository.WorkflowLogRepository;
import com.synctask.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowService {
    
    private static final Gson gson = new Gson();
    
    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowLogRepository workflowLogRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Transactional
    public Workflow createWorkflow(String name, String sourceType, String targetType, Long userId) {
        Workflow workflow = new Workflow();
        workflow.setId(UUID.randomUUID().toString());
        workflow.setName(name);
        workflow.setSourceType(sourceType != null ? sourceType : "mysql");
        workflow.setTargetType(targetType != null ? targetType : "mysql");
        workflow.setStatus(WorkflowStatus.CONFIGURING);
        workflow.setUserId(userId);
        workflow.setProgress(0);
        workflow.setIsBilling(false);

        Workflow savedWorkflow = workflowRepository.save(workflow);
        addLog(savedWorkflow.getId(), WorkflowLog.LogLevel.INFO, "任务创建成功，状态: 配置中");
        return savedWorkflow;
    }

    @Transactional
    public Workflow updateConfig(String workflowId, Long userId, String sourceConnection, String targetConnection,
                                  String migrationMode, String syncObjects, String sourceDbName,
                                  String targetDbName, String sourceType, String targetType) {
        Workflow workflow = getWorkflowById(workflowId, userId);
        
        if (workflow.getStatus() != WorkflowStatus.CONFIGURING) {
            throw new RuntimeException("只能修改配置中的任务，当前状态: " + workflow.getStatus().name());
        }
        
        if (sourceConnection != null) workflow.setSourceConnection(sourceConnection);
        if (targetConnection != null) workflow.setTargetConnection(targetConnection);
        if (migrationMode != null) workflow.setMigrationMode(migrationMode);
        if (syncObjects != null) workflow.setSyncObjects(syncObjects);
        if (sourceDbName != null) workflow.setSourceDbName(sourceDbName);
        if (targetDbName != null) workflow.setTargetDbName(targetDbName);
        if (sourceType != null) workflow.setSourceType(sourceType);
        if (targetType != null) workflow.setTargetType(targetType);
        
        addLog(workflowId, WorkflowLog.LogLevel.INFO, "任务配置已更新");
        return workflowRepository.save(workflow);
    }

    @Transactional
    public Workflow launchWorkflow(String workflowId, Long userId) {
        Workflow workflow = getWorkflowById(workflowId, userId);
        
        if (workflow.getStatus() != WorkflowStatus.CONFIGURING) {
            throw new RuntimeException("只能启动配置中的任务，当前状态: " + workflow.getStatus().name());
        }
        
        if (workflow.getSourceConnection() == null || workflow.getSourceConnection().isEmpty() ||
            workflow.getTargetConnection() == null || workflow.getTargetConnection().isEmpty()) {
            throw new RuntimeException("请先完成连接信息配置");
        }
        if (workflow.getSyncObjects() == null || workflow.getSyncObjects().isEmpty()) {
            throw new RuntimeException("请先选择同步对象");
        }
        if (workflow.getMigrationMode() == null || workflow.getMigrationMode().isEmpty()) {
            throw new RuntimeException("请先选择同步模式");
        }
        
        workflow.setStatus(WorkflowStatus.PENDING);
        workflow.setIsBilling(true);
        workflowRepository.save(workflow);
        
        addLog(workflowId, WorkflowLog.LogLevel.INFO, "任务启动中，状态: 启动中");
        
        try {
            kafkaProducerService.sendTaskCreatedMessage(workflow);
            addLog(workflowId, WorkflowLog.LogLevel.INFO, "任务消息已发送到 Kafka topic: sync-task-created，等待任务执行服务处理");
        } catch (Exception e) {
            addLog(workflowId, WorkflowLog.LogLevel.WARNING, "Kafka 消息发送失败: " + e.getMessage());
        }
        
        return workflow;
    }

    public Page<Workflow> getWorkflowsByUserId(Long userId, int page, int pageSize, String sortBy, String sortDirection) {
        String fieldName = mapSortField(sortBy);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, fieldName);
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        return workflowRepository.findByUserId(userId, pageable);
    }
    
    public Page<Workflow> getWorkflowsByUserIdAndFilters(Long userId, String keyword, String status, int page, int pageSize, String sortBy, String sortDirection) {
        String fieldName = mapSortField(sortBy);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, fieldName);
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();
        
        if (hasKeyword && hasStatus) {
            WorkflowStatus workflowStatus = WorkflowStatus.valueOf(status.toUpperCase());
            return workflowRepository.findByUserIdAndKeywordAndStatus(userId, keyword.trim(), workflowStatus, pageable);
        } else if (hasKeyword) {
            return workflowRepository.findByUserIdAndKeyword(userId, keyword.trim(), pageable);
        } else if (hasStatus) {
            WorkflowStatus workflowStatus = WorkflowStatus.valueOf(status.toUpperCase());
            return workflowRepository.findByUserIdAndStatus(userId, workflowStatus, pageable);
        } else {
            return workflowRepository.findByUserId(userId, pageable);
        }
    }
    
    public List<Workflow> getFailedWorkflowsByUserId(Long userId) {
        return workflowRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, WorkflowStatus.FAILED);
    }
    
    private String mapSortField(String sortBy) {
        switch (sortBy) {
            case "name":
                return "name";
            case "status":
                return "status";
            case "created_at":
                return "createdAt";
            case "is_billing":
                return "isBilling";
            default:
                return "createdAt";
        }
    }

    public Workflow getWorkflowById(String id, Long userId) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        if (!workflow.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此任务");
        }
        
        return workflow;
    }

    public List<WorkflowLog> getWorkflowLogs(String workflowId, Long userId) {
        Workflow workflow = getWorkflowById(workflowId, userId);
        return workflowLogRepository.findByWorkflowIdOrderByCreatedAtDesc(workflow.getId());
    }

    @Transactional
    public void pauseWorkflow(String id, Long userId) {
        Workflow workflow = getWorkflowById(id, userId);
        
        String currentStatus = workflow.getStatus().name();
        
        TaskCreatedMessage message = new TaskCreatedMessage();
        message.setTaskId(workflow.getId());
        message.setTaskName(workflow.getName());
        message.setUserId(workflow.getUserId());
        message.setSourceConnection(workflow.getSourceConnection());
        message.setTargetConnection(workflow.getTargetConnection());
        message.setMigrationMode(workflow.getMigrationMode());
        message.setCreatedAt(workflow.getCreatedAt());
        message.setMessageType("stop");
        message.setCurrentStatus(currentStatus);
        message.setSourceType(workflow.getSourceType());
        message.setTargetType(workflow.getTargetType());
        
        try {
            kafkaProducerService.sendControlMessage(message);
            addLog(workflow.getId(), WorkflowLog.LogLevel.INFO, "任务已暂停，发送停止消息到 Kafka，当前状态: " + currentStatus);
        } catch (Exception e) {
            addLog(workflow.getId(), WorkflowLog.LogLevel.WARNING, "Kafka 消息发送失败: " + e.getMessage());
        }
        
        workflow.setStatus(WorkflowStatus.PAUSED);
        workflowRepository.save(workflow);
    }

    @Transactional
    public void resumeWorkflow(String id, Long userId) {
        Workflow workflow = getWorkflowById(id, userId);
        workflow.setStatus(WorkflowStatus.STARTING);
        workflowRepository.save(workflow);
        
        TaskCreatedMessage message = new TaskCreatedMessage();
        message.setTaskId(workflow.getId());
        message.setTaskName(workflow.getName());
        message.setUserId(workflow.getUserId());
        message.setSourceConnection(workflow.getSourceConnection());
        message.setTargetConnection(workflow.getTargetConnection());
        message.setMigrationMode(workflow.getMigrationMode());
        message.setCreatedAt(workflow.getCreatedAt());
        message.setMessageType("resume");
        message.setSourceType(workflow.getSourceType());
        message.setTargetType(workflow.getTargetType());
        
        try {
            kafkaProducerService.sendControlMessage(message);
            addLog(workflow.getId(), WorkflowLog.LogLevel.INFO, "任务已恢复，发送恢复消息到 Kafka，等待任务执行服务处理");
        } catch (Exception e) {
            addLog(workflow.getId(), WorkflowLog.LogLevel.WARNING, "Kafka 恢复消息发送失败: " + e.getMessage());
        }
    }

    @Transactional
    public void stopWorkflow(String id, Long userId) {
        Workflow workflow = getWorkflowById(id, userId);
        
        TaskCreatedMessage message = new TaskCreatedMessage();
        message.setTaskId(workflow.getId());
        message.setTaskName(workflow.getName());
        message.setUserId(workflow.getUserId());
        message.setSourceConnection(workflow.getSourceConnection());
        message.setTargetConnection(workflow.getTargetConnection());
        message.setMigrationMode(workflow.getMigrationMode());
        message.setCreatedAt(workflow.getCreatedAt());
        message.setMessageType("terminate");
        message.setCurrentStatus(workflow.getStatus().name());
        message.setSourceType(workflow.getSourceType());
        message.setTargetType(workflow.getTargetType());
        
        try {
            kafkaProducerService.sendControlMessage(message);
            addLog(workflow.getId(), WorkflowLog.LogLevel.INFO, "发送终止消息到 Kafka，结束所有相关进程");
        } catch (Exception e) {
            addLog(workflow.getId(), WorkflowLog.LogLevel.WARNING, "Kafka 终止消息发送失败: " + e.getMessage());
        }
        
        workflow.setStatus(WorkflowStatus.COMPLETED);
        workflow.setCompletedAt(LocalDateTime.now());
        workflow.setIsBilling(false);
        workflowRepository.save(workflow);
        addLog(workflow.getId(), WorkflowLog.LogLevel.INFO, "任务已结束，状态: 已完成");
    }

    @Transactional
    public void deleteWorkflow(String id, Long userId) {
        Workflow workflow = getWorkflowById(id, userId);
        
        WorkflowStatus status = workflow.getStatus();
        if (status != WorkflowStatus.COMPLETED && status != WorkflowStatus.FAILED && status != WorkflowStatus.FULL_COMPLETED && status != WorkflowStatus.CONFIGURING) {
            throw new RuntimeException("只能删除已完成、失败或配置中的任务，当前状态: " + status.name());
        }
        
        workflow.setIsDeleted(true);
        workflowRepository.save(workflow);
        addLog(workflow.getId(), WorkflowLog.LogLevel.INFO, "任务已删除（软删除）");
    }

    @Transactional
    public void retryWorkflow(String id, Long userId) {
        Workflow workflow = getWorkflowById(id, userId);
        
        if (workflow.getStatus() != WorkflowStatus.FAILED) {
            throw new RuntimeException("只能重试失败的任务，当前状态: " + workflow.getStatus().name());
        }
        
        boolean fullSyncCompleted = "fullAndIncre".equals(workflow.getMigrationMode()) 
            && workflow.getProgress() != null 
            && workflow.getProgress() == 100;
        
        if (fullSyncCompleted) {
            workflow.setStatus(WorkflowStatus.STARTING);
            workflow.setIsBilling(true);
            workflow.setErrorMessage(null);
            workflow.setErrorCode(null);
            workflow.setCompletedAt(null);
            workflowRepository.save(workflow);
            
            addLog(workflow.getId(), WorkflowLog.LogLevel.INFO, "任务恢复中，全量同步已完成，将从增量位点继续同步");
            
            TaskCreatedMessage message = new TaskCreatedMessage();
            message.setTaskId(workflow.getId());
            message.setTaskName(workflow.getName());
            message.setUserId(workflow.getUserId());
            message.setSourceConnection(workflow.getSourceConnection());
            message.setTargetConnection(workflow.getTargetConnection());
            message.setMigrationMode(workflow.getMigrationMode());
            message.setSyncObjects(parseSyncObjects(workflow.getSyncObjects()));
            message.setSourceDbName(workflow.getSourceDbName());
            message.setTargetDbName(workflow.getTargetDbName());
            message.setCreatedAt(workflow.getCreatedAt());
            message.setMessageType("resume");
            message.setCurrentStatus("INCREMENT_RUNNING");
            message.setSourceType(workflow.getSourceType());
            message.setTargetType(workflow.getTargetType());
            
            try {
                kafkaProducerService.sendControlMessage(message);
                addLog(workflow.getId(), WorkflowLog.LogLevel.INFO, "任务恢复消息已发送到 Kafka（跳过全量同步，从增量位点继续）");
            } catch (Exception e) {
                addLog(workflow.getId(), WorkflowLog.LogLevel.WARNING, "Kafka 消息发送失败: " + e.getMessage());
            }
        } else {
            workflow.setStatus(WorkflowStatus.PENDING);
            workflow.setProgress(0);
            workflow.setIsBilling(true);
            workflow.setErrorMessage(null);
            workflow.setErrorCode(null);
            workflow.setCompletedAt(null);
            workflow.setTotalTables(null);
            workflow.setCompletedTables(null);
            workflow.setCurrentTable(null);
            workflow.setCurrentTableProgress(null);
            workflow.setCurrentTableRows(null);
            workflow.setCurrentTableTotalRows(null);
            workflowRepository.save(workflow);
            
            addLog(workflow.getId(), WorkflowLog.LogLevel.INFO, "任务重试中，状态重置为 PENDING");
            
            TaskCreatedMessage message = new TaskCreatedMessage();
            message.setTaskId(workflow.getId());
            message.setTaskName(workflow.getName());
            message.setUserId(workflow.getUserId());
            message.setSourceConnection(workflow.getSourceConnection());
            message.setTargetConnection(workflow.getTargetConnection());
            message.setMigrationMode(workflow.getMigrationMode());
            message.setSyncObjects(parseSyncObjects(workflow.getSyncObjects()));
            message.setSourceDbName(workflow.getSourceDbName());
            message.setCreatedAt(workflow.getCreatedAt());
            message.setMessageType("TASK_CREATED");
            message.setSourceType(workflow.getSourceType());
            message.setTargetType(workflow.getTargetType());
            
            try {
                kafkaProducerService.sendTaskCreatedMessage(workflow);
                addLog(workflow.getId(), WorkflowLog.LogLevel.INFO, "任务重试消息已发送到 Kafka，等待任务执行服务处理");
            } catch (Exception e) {
                addLog(workflow.getId(), WorkflowLog.LogLevel.WARNING, "Kafka 消息发送失败: " + e.getMessage());
            }
        }
    }
    
    private Map<String, Object> parseSyncObjects(String syncObjects) {
        if (syncObjects == null || syncObjects.isEmpty()) {
            return null;
        }
        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(syncObjects, type);
        } catch (Exception e) {
            return null;
        }
    }

    private void addLog(String workflowId, WorkflowLog.LogLevel level, String message) {
        WorkflowLog log = new WorkflowLog();
        log.setWorkflowId(workflowId);
        log.setLevel(level);
        log.setMessage(message);
        workflowLogRepository.save(log);
    }
}
