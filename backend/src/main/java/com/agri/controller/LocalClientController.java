package com.agri.controller;

import com.agri.model.LocalClient;
import com.agri.model.LocalDataset;
import com.agri.service.LocalClientService;
import com.agri.service.LocalDatasetService;
import com.agri.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/local-client")
public class LocalClientController extends TextWebSocketHandler {

    @Autowired
    private LocalClientService localClientService;
    
    @Autowired
    private LocalDatasetService localDatasetService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    private static final Map<String, Long> clientUsers = new ConcurrentHashMap<>();

    @GetMapping("/list")
    public ResponseEntity<?> getClientList(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        List<LocalClient> clients = localClientService.getByUserId(userId);
        return ResponseEntity.ok(Map.of("code", 200, "data", clients));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClientInfo(@PathVariable String clientId) {
        LocalClient client = localClientService.getByClientId(clientId);
        if (client == null) {
            return ResponseEntity.ok(Map.of("code", 404, "message", "客户端不存在"));
        }
        return ResponseEntity.ok(Map.of("code", 200, "data", client));
    }

    @GetMapping("/{clientId}/datasets")
    public ResponseEntity<?> getClientDatasets(@PathVariable String clientId) {
        List<LocalDataset> datasets = localDatasetService.getByClientId(clientId);
        return ResponseEntity.ok(Map.of("code", 200, "data", datasets));
    }

    @PostMapping("/{clientId}/command")
    public ResponseEntity<?> sendCommand(
            @PathVariable String clientId,
            @RequestBody Map<String, Object> command) {
        
        WebSocketSession session = clientSessions.get(clientId);
        if (session == null || !session.isOpen()) {
            return ResponseEntity.ok(Map.of("code", 400, "message", "客户端未连接"));
        }
        
        try {
            String commandJson = objectMapper.writeValueAsString(command);
            session.sendMessage(new TextMessage(commandJson));
            return ResponseEntity.ok(Map.of("code", 200, "message", "命令已发送"));
        } catch (IOException e) {
            return ResponseEntity.ok(Map.of("code", 500, "message", "发送失败: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String clientId = extractClientId(session);
        Long userId = extractUserId(session);
        
        if (clientId != null) {
            clientSessions.put(clientId, session);
            if (userId != null) {
                clientUsers.put(clientId, userId);
            }
            localClientService.updateClientStatus(clientId, "connected");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        
        String clientId = extractClientId(session);
        String type = (String) data.get("type");
        
        switch (type) {
            case "client_register":
                handleClientRegister(session, clientId, data);
                break;
            case "ping":
                handlePing(session, clientId);
                break;
            case "dataset_change":
                handleDatasetChange(clientId, data);
                break;
            case "training_progress":
                handleTrainingProgress(clientId, data);
                break;
            default:
                sendResponse(session, data.get("id"), Map.of("status", "ok"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String clientId = extractClientId(session);
        if (clientId != null) {
            clientSessions.remove(clientId);
            clientUsers.remove(clientId);
            localClientService.updateClientStatus(clientId, "disconnected");
        }
    }

    private void handleClientRegister(WebSocketSession session, String clientId, Map<String, Object> data) {
        Long userId = clientUsers.get(clientId);
        Map<String, Object> info = (Map<String, Object>) data.get("info");
        localClientService.registerClient(userId, clientId, info);
        
        sendResponse(session, data.get("id"), Map.of("status", "ok", "message", "注册成功"));
    }

    private void handlePing(WebSocketSession session, String clientId) {
        localClientService.updateHeartbeat(clientId);
        sendResponse(session, null, Map.of("status", "ok", "timestamp", System.currentTimeMillis()));
    }

    private void handleDatasetChange(String clientId, Map<String, Object> data) {
        String event = (String) data.get("event");
        String path = (String) data.get("path");
        
        Map<String, Object> datasetInfo = (Map<String, Object>) data.get("dataset");
        if (datasetInfo != null) {
            try {
                LocalDataset dataset = new LocalDataset();
                dataset.setClientId(clientId);
                dataset.setName((String) datasetInfo.get("name"));
                dataset.setPath(path);
                dataset.setSize(((Number) datasetInfo.get("size")).longValue());
                dataset.setFileType((String) datasetInfo.get("file_type"));
                dataset.setChecksum((String) datasetInfo.get("checksum"));
                
                if (datasetInfo.get("row_count") != null) {
                    dataset.setRowCount(((Number) datasetInfo.get("row_count")).intValue());
                }
                if (datasetInfo.get("column_count") != null) {
                    dataset.setColumnCount(((Number) datasetInfo.get("column_count")).intValue());
                }
                if (datasetInfo.get("columns") != null) {
                    dataset.setColumns(objectMapper.writeValueAsString(datasetInfo.get("columns")));
                }
                
                localDatasetService.syncDataset(dataset);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleTrainingProgress(String clientId, Map<String, Object> data) {
        // 可以在这里处理训练进度，例如存储到数据库或转发给前端
    }

    private void sendResponse(WebSocketSession session, Object id, Map<String, Object> response) {
        try {
            if (id != null) {
                response.put("id", id);
            }
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractClientId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("client_id=")) {
            return query.split("client_id=")[1].split("&")[0];
        }
        
        String clientId = session.getHandshakeHeaders().getFirst("X-Client-ID");
        if (clientId != null && !clientId.isEmpty()) {
            return clientId;
        }
        
        return session.getId();
    }

    private Long extractUserId(WebSocketSession session) {
        String auth = session.getHandshakeHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                return jwtUtils.getUserIdFromToken(auth.substring(7));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
