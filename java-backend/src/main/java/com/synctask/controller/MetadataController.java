package com.synctask.controller;

import com.synctask.dto.ConnectionRequest;
import com.synctask.dto.ContentCompareSession;
import com.synctask.dto.DatabaseInfo;
import com.synctask.dto.TableInfo;
import com.synctask.dto.ValidationResult;
import com.synctask.service.ContentCompareService;
import com.synctask.service.MetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private ContentCompareService contentCompareService;

    @PostMapping("/test-connection")
    public ResponseEntity<?> testConnection(@RequestBody Map<String, String> request) {
        try {
            String connectionStr = request.get("sourceConnection");
            String expectedType = request.get("dbType");
            
            logger.info("测试数据库连接: expectedType={}", expectedType);
            
            MetadataService.ConnectionTestResult result = metadataService.testConnectionDetailed(connectionStr, expectedType);
            
            Map<String, Object> data = new HashMap<>();
            data.put("connected", result.connected);
            if (!result.connected) {
                data.put("errorType", result.errorType);
                data.put("errorMessage", result.errorMessage);
                data.put("suggestion", result.suggestion);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("测试连接失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateForMigration(@RequestBody Map<String, String> request) {
        try {
            String sourceConnection = request.get("sourceConnection");
            String targetConnection = request.get("targetConnection");
            String migrationMode = request.get("migrationMode");
            String sourceType = request.get("sourceType");
            String targetType = request.get("targetType");
            
            logger.info("校验数据库同步条件: mode={}, sourceType={}, targetType={}", migrationMode, sourceType, targetType);
            
            ValidationResult result = metadataService.validateForMigration(
                sourceConnection, targetConnection, migrationMode, sourceType, targetType);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result
            ));
        } catch (Exception e) {
            logger.error("校验失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/databases")
    public ResponseEntity<?> listDatabases(@RequestBody ConnectionRequest request) {
        try {
            logger.info("查询数据库列表: {}", maskConnection(request.getSourceConnection()));
            
            List<String> databases = metadataService.listDatabases(request.getSourceConnection());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("databases", databases));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("查询数据库列表失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/tables")
    public ResponseEntity<?> listTables(@RequestBody Map<String, String> request) {
        try {
            String connectionStr = request.get("sourceConnection");
            String database = request.get("database");
            String schema = request.get("schema");
            
            logger.info("查询表列表: database={}, schema={}", database, schema);
            
            List<TableInfo> tables = metadataService.listTables(connectionStr, database, schema);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("database", database, "tables", tables));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("查询表列表失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/schemas")
    public ResponseEntity<?> listSchemas(@RequestBody Map<String, String> request) {
        try {
            String connectionStr = request.get("sourceConnection");
            String database = request.get("database");
            
            logger.info("查询schema列表: database={}", database);
            
            List<String> schemas = metadataService.listSchemas(connectionStr, database);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("database", database, "schemas", schemas));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("查询schema列表失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/database-info")
    public ResponseEntity<?> getDatabaseInfo(@RequestBody Map<String, String> request) {
        try {
            String connectionStr = request.get("sourceConnection");
            String database = request.get("database");
            
            logger.info("获取数据库信息: database={}", database);
            
            DatabaseInfo dbInfo = metadataService.getDatabaseWithTables(connectionStr, database);
            
            return ResponseEntity.ok(Map.of("success", true, "data", dbInfo));
        } catch (Exception e) {
            logger.error("获取数据库信息失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    private String maskConnection(String connectionStr) {
        if (connectionStr == null) return "null";
        return connectionStr.replaceAll(":[^:@]+@", ":****@");
    }

    @PostMapping("/compare-content/start")
    public ResponseEntity<?> startContentCompare(@RequestBody Map<String, Object> request) {
        try {
            String sourceConnection = (String) request.get("sourceConnection");
            String targetConnection = (String) request.get("targetConnection");
            String sourceType = (String) request.get("sourceType");
            String targetType = (String) request.get("targetType");
            @SuppressWarnings("unchecked")
            Map<String, List<String>> syncObjects = (Map<String, List<String>>) request.get("syncObjects");

            logger.info("启动内容对比: sourceType={}, targetType={}", sourceType, targetType);

            ContentCompareSession session = contentCompareService.startCompare(
                sourceConnection, targetConnection, sourceType, targetType, syncObjects);

            return ResponseEntity.ok(Map.of("success", true, "data", session));
        } catch (Exception e) {
            logger.error("启动内容对比失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/compare-content/{sessionId}/checksum")
    public ResponseEntity<?> runChecksumPhase(@PathVariable String sessionId) {
        try {
            logger.info("执行阶段1校验和对比: sessionId={}", sessionId);
            ContentCompareSession session = contentCompareService.runPhase1Checksum(sessionId);
            return ResponseEntity.ok(Map.of("success", true, "data", session));
        } catch (Exception e) {
            logger.error("校验和对比失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/compare-content/{sessionId}")
    public ResponseEntity<?> getCompareResult(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int tableIndex,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            ContentCompareSession session = contentCompareService.getSession(sessionId);
            ContentCompareSession.TableCompareTask task = contentCompareService.findDiffs(sessionId, tableIndex, limit);

            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", session.getSessionId());
            data.put("status", session.getStatus());
            data.put("sourceType", session.getSourceType());
            data.put("targetType", session.getTargetType());

            Map<String, Object> tableResult = new HashMap<>();
            tableResult.put("sourceTable", task.getSourceTable());
            tableResult.put("targetTable", task.getTargetTable());
            tableResult.put("checksumMatch", task.getChecksumMatch());
            tableResult.put("sourceRowCount", task.getSourceRowCount());
            tableResult.put("targetRowCount", task.getTargetRowCount());
            tableResult.put("totalDiffsFound", task.getTotalDiffsFound());
            tableResult.put("scanCompleted", task.isScanCompleted());
            tableResult.put("status", task.getStatus());

            if (task.getCursor() != null) {
                Map<String, Object> cursorInfo = new HashMap<>();
                cursorInfo.put("scannedRows", task.getCursor().getScannedRows());
                cursorInfo.put("totalRows", task.getCursor().getTotalRows());
                if (task.getCursor().getTotalRows() > 0) {
                    cursorInfo.put("scanProgress", String.format("%.1f%%",
                        (double) task.getCursor().getScannedRows() / task.getCursor().getTotalRows() * 100));
                }
                tableResult.put("cursor", cursorInfo);
            }

            tableResult.put("diffs", task.getDiffs());

            data.put("currentTable", tableResult);

            java.util.List<Map<String, Object>> tableSummaries = new java.util.ArrayList<>();
            for (int i = 0; i < session.getTables().size(); i++) {
                ContentCompareSession.TableCompareTask t = session.getTables().get(i);
                Map<String, Object> summary = new HashMap<>();
                summary.put("index", i);
                summary.put("sourceTable", t.getSourceTable());
                summary.put("targetTable", t.getTargetTable());
                summary.put("checksumMatch", t.getChecksumMatch());
                summary.put("sourceRowCount", t.getSourceRowCount());
                summary.put("targetRowCount", t.getTargetRowCount());
                summary.put("totalDiffsFound", t.getTotalDiffsFound());
                summary.put("status", t.getStatus());
                tableSummaries.add(summary);
            }
            data.put("tables", tableSummaries);

            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception e) {
            logger.error("获取对比结果失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/compare-content/{sessionId}")
    public ResponseEntity<?> deleteCompareSession(@PathVariable String sessionId) {
        try {
            contentCompareService.deleteSession(sessionId);
            return ResponseEntity.ok(Map.of("success", true, "message", "对比会话已删除"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
