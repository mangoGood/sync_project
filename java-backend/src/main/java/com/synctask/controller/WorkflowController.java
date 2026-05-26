package com.synctask.controller;

import com.synctask.entity.Workflow;
import com.synctask.entity.WorkflowLog;
import com.synctask.security.UserPrincipal;
import com.synctask.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    @Autowired
    private WorkflowService workflowService;

    @PostMapping
    public ResponseEntity<?> createWorkflow(
            @RequestBody CreateWorkflowRequest request,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String taskType = request.getTaskType() != null ? request.getTaskType() : "SYNC";
            Workflow workflow = workflowService.createWorkflow(
                    request.getName(),
                    request.getSourceType(),
                    request.getTargetType(),
                    userPrincipal.getId(),
                    taskType
            );
            return ResponseEntity.ok(new ApiResponse(true, "任务创建成功", convertToMap(workflow)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{id}/config")
    public ResponseEntity<?> updateConfig(
            @PathVariable String id,
            @RequestBody UpdateConfigRequest request,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Workflow workflow = workflowService.updateConfig(
                    id,
                    userPrincipal.getId(),
                    request.getSourceConnection(),
                    request.getTargetConnection(),
                    request.getMigrationMode(),
                    request.getSyncObjects(),
                    request.getSourceDbName(),
                    request.getTargetDbName(),
                    request.getSourceType(),
                    request.getTargetType()
            );
            return ResponseEntity.ok(new ApiResponse(true, "配置保存成功", convertToMap(workflow)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{id}/launch")
    public ResponseEntity<?> launchWorkflow(
            @PathVariable String id,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Workflow workflow = workflowService.launchWorkflow(id, userPrincipal.getId());
            return ResponseEntity.ok(new ApiResponse(true, "任务启动成功", convertToMap(workflow)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getWorkflows(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskType,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Page<Workflow> workflowPage = workflowService.getWorkflowsByUserIdAndFilters(
                    userPrincipal.getId(), keyword, status, taskType, page, pageSize, sortBy, sortDirection
            );

            List<Map<String, Object>> list = new ArrayList<>();
            for (Workflow workflow : workflowPage.getContent()) {
                list.add(convertToMap(workflow));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("list", list);
            response.put("total", workflowPage.getTotalElements());
            response.put("page", page);
            response.put("pageSize", pageSize);

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/failed")
    public ResponseEntity<?> getFailedWorkflows(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<Workflow> failedWorkflows = workflowService.getFailedWorkflowsByUserId(userPrincipal.getId());

            List<Map<String, Object>> list = new ArrayList<>();
            for (Workflow workflow : failedWorkflows) {
                list.add(convertToMap(workflow));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("list", list);
            response.put("total", failedWorkflows.size());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkflow(
            @PathVariable String id,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Workflow workflow = workflowService.getWorkflowById(id, userPrincipal.getId());
            List<WorkflowLog> logs = workflowService.getWorkflowLogs(id, userPrincipal.getId());

            Map<String, Object> response = convertToMap(workflow);
            
            List<Map<String, Object>> logList = new ArrayList<>();
            for (WorkflowLog log : logs) {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("id", log.getId());
                logMap.put("workflow_id", log.getWorkflowId());
                logMap.put("level", log.getLevel().name());
                logMap.put("message", log.getMessage());
                logMap.put("created_at", log.getCreatedAt());
                logList.add(logMap);
            }
            response.put("logs", logList);

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<?> pauseWorkflow(
            @PathVariable String id,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            workflowService.pauseWorkflow(id, userPrincipal.getId());
            return ResponseEntity.ok(new ApiResponse(true, "任务已暂停"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<?> resumeWorkflow(
            @PathVariable String id,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            workflowService.resumeWorkflow(id, userPrincipal.getId());
            return ResponseEntity.ok(new ApiResponse(true, "任务已恢复"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<?> stopWorkflow(
            @PathVariable String id,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            workflowService.stopWorkflow(id, userPrincipal.getId());
            return ResponseEntity.ok(new ApiResponse(true, "任务已结束"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkflow(
            @PathVariable String id,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            workflowService.deleteWorkflow(id, userPrincipal.getId());
            return ResponseEntity.ok(new ApiResponse(true, "任务删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retryWorkflow(
            @PathVariable String id,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            workflowService.retryWorkflow(id, userPrincipal.getId());
            return ResponseEntity.ok(new ApiResponse(true, "任务重试已启动"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{id}/failover")
    public ResponseEntity<?> failoverWorkflow(
            @PathVariable String id,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Workflow workflow = workflowService.failoverWorkflow(id, userPrincipal.getId());
            return ResponseEntity.ok(new ApiResponse(true, "主备倒换已启动", convertToMap(workflow)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    private Map<String, Object> convertToMap(Workflow workflow) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", workflow.getId());
        map.put("name", workflow.getName());
        map.put("source_connection", workflow.getSourceConnection());
        map.put("target_connection", workflow.getTargetConnection());
        map.put("status", workflow.getStatus().name());
        map.put("progress", workflow.getProgress());
        map.put("is_billing", workflow.getIsBilling());
        map.put("migration_mode", workflow.getMigrationMode());
        map.put("sync_objects", workflow.getSyncObjects());
        map.put("is_deleted", workflow.getIsDeleted());
        map.put("created_at", workflow.getCreatedAt());
        map.put("updated_at", workflow.getUpdatedAt());
        map.put("completed_at", workflow.getCompletedAt());
        map.put("error_message", workflow.getErrorMessage());
        map.put("error_code", workflow.getErrorCode());
        map.put("user_id", workflow.getUserId());
        map.put("total_tables", workflow.getTotalTables());
        map.put("completed_tables", workflow.getCompletedTables());
        map.put("current_table", workflow.getCurrentTable());
        map.put("current_table_progress", workflow.getCurrentTableProgress());
        map.put("current_table_rows", workflow.getCurrentTableRows());
        map.put("current_table_total_rows", workflow.getCurrentTableTotalRows());
        map.put("source_type", workflow.getSourceType());
        map.put("target_type", workflow.getTargetType());
        map.put("rpo_ms", workflow.getRpoMs());
        map.put("rto_ms", workflow.getRtoMs());
        map.put("task_type", workflow.getTaskType());
        map.put("dr_status", workflow.getDrStatus());
        map.put("dr_switch_count", workflow.getDrSwitchCount());
        return map;
    }

    public static class CreateWorkflowRequest {
        private String name;
        private String sourceType;
        private String targetType;
        private String taskType;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
    }

    public static class UpdateConfigRequest {
        private String sourceConnection;
        private String targetConnection;
        private String migrationMode;
        private String syncObjects;
        private String sourceDbName;
        private String targetDbName;
        private String sourceType;
        private String targetType;

        public String getSourceConnection() { return sourceConnection; }
        public void setSourceConnection(String sourceConnection) { this.sourceConnection = sourceConnection; }
        public String getTargetConnection() { return targetConnection; }
        public void setTargetConnection(String targetConnection) { this.targetConnection = targetConnection; }
        public String getMigrationMode() { return migrationMode; }
        public void setMigrationMode(String migrationMode) { this.migrationMode = migrationMode; }
        public String getSyncObjects() { return syncObjects; }
        public void setSyncObjects(String syncObjects) { this.syncObjects = syncObjects; }
        public String getSourceDbName() { return sourceDbName; }
        public void setSourceDbName(String sourceDbName) { this.sourceDbName = sourceDbName; }
        public String getTargetDbName() { return targetDbName; }
        public void setTargetDbName(String targetDbName) { this.targetDbName = targetDbName; }
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
    }

    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
