package com.migration.agent.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.migration.agent.AgentMain;
import com.migration.agent.model.TaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Executors;

public class AgentHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(AgentHttpServer.class);
    private static final int DEFAULT_PORT = 8083;

    private final AgentMain agentMain;
    private final Gson gson;

    public AgentHttpServer(AgentMain agentMain) {
        this.agentMain = agentMain;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                if (json.isJsonArray()) {
                    com.google.gson.JsonArray arr = json.getAsJsonArray();
                    return LocalDateTime.of(
                        arr.get(0).getAsInt(), arr.get(1).getAsInt(), arr.get(2).getAsInt(),
                        arr.size() > 3 ? arr.get(3).getAsInt() : 0,
                        arr.size() > 4 ? arr.get(4).getAsInt() : 0,
                        arr.size() > 5 ? arr.get(5).getAsInt() : 0);
                }
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            })
            .create();
    }
    private HttpServer server;

    public void start() {
        start(DEFAULT_PORT);
    }

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(4));

            server.createContext("/api/agent/failover", this::handleFailover);
            server.createContext("/api/agent/start-increment", this::handleStartIncrement);
            server.createContext("/api/agent/status", this::handleStatus);
            server.createContext("/api/agent/health", this::handleHealth);

            server.start();
            logger.info("Agent HTTP Server started on port {}", port);
        } catch (IOException e) {
            logger.error("Failed to start Agent HTTP Server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            logger.info("Agent HTTP Server stopped");
        }
    }

    private void handleFailover(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Map.of("success", false, "message", "Method not allowed"));
            return;
        }

        try {
            String requestBody = readRequestBody(exchange);
            logger.info("Received failover request: {}", requestBody);

            TaskMessage taskMessage = gson.fromJson(requestBody, TaskMessage.class);
            if (taskMessage.getTaskId() == null || taskMessage.getTaskId().isEmpty()) {
                sendResponse(exchange, 400, Map.of("success", false, "message", "taskId is required"));
                return;
            }

            if (taskMessage.getMessageType() == null) {
                taskMessage.setMessageType("failover");
            }

            if (agentMain.isFailoverInProgress(taskMessage.getTaskId())) {
                logger.warn("Failover already in progress for task: {}, rejecting duplicate request", taskMessage.getTaskId());
                sendResponse(exchange, 409, Map.of("success", false, "message", "Failover already in progress for task: " + taskMessage.getTaskId()));
                return;
            }

            new Thread(() -> agentMain.handleFailoverDirect(taskMessage)).start();

            sendResponse(exchange, 200, Map.of(
                "success", true,
                "message", "Failover initiated for task: " + taskMessage.getTaskId(),
                "taskId", taskMessage.getTaskId()
            ));
        } catch (Exception e) {
            logger.error("Error handling failover request", e);
            sendResponse(exchange, 500, Map.of("success", false, "message", e.getMessage()));
        }
    }

    private void handleStartIncrement(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Map.of("success", false, "message", "Method not allowed"));
            return;
        }

        try {
            String requestBody = readRequestBody(exchange);
            logger.info("Received start-increment request: {}", requestBody);

            TaskMessage taskMessage = gson.fromJson(requestBody, TaskMessage.class);
            if (taskMessage.getTaskId() == null || taskMessage.getTaskId().isEmpty()) {
                sendResponse(exchange, 400, Map.of("success", false, "message", "taskId is required"));
                return;
            }

            agentMain.startIncrementDirect(taskMessage);

            sendResponse(exchange, 200, Map.of(
                "success", true,
                "message", "Increment sync started for task: " + taskMessage.getTaskId(),
                "taskId", taskMessage.getTaskId()
            ));
        } catch (Exception e) {
            logger.error("Error handling start-increment request", e);
            sendResponse(exchange, 500, Map.of("success", false, "message", e.getMessage()));
        }
    }

    private void handleStatus(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Map.of("success", false, "message", "Method not allowed"));
            return;
        }

        try {
            Map<String, Object> status = agentMain.getAgentStatus();
            sendResponse(exchange, 200, status);
        } catch (Exception e) {
            logger.error("Error handling status request", e);
            sendResponse(exchange, 500, Map.of("success", false, "message", e.getMessage()));
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, Map.of("status", "UP"));
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String response = gson.toJson(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
