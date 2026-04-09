package com.agri.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * FLGo训练状态控制器
 * 接收客户端上报的训练状态
 */
@RestController
@RequestMapping("/api/flgo/status")
@CrossOrigin
public class FLGoStatusController {
    
    // 存储各客户端的训练状态
    // taskId -> (clientIp -> status)
    private static final Cache<String, Map<String, TrainingStatus>> clientStatuses = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    
    // 全局训练历史
    private static final Cache<String, List<Map<String, Object>>> trainingHistory = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    
    public static class TrainingStatus {
        public String clientIp;
        public String clientName;
        public String status;  // IDLE, TRAINING, COMPLETED, FAILED
        public int currentRound;
        public int totalRounds;
        public double accuracy;
        public double loss;
        public long lastUpdate;
        
        public TrainingStatus() {}
        
        public TrainingStatus(String clientIp, String status) {
            this.clientIp = clientIp;
            this.status = status;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
    
    /**
     * 客户端上报训练状态
     */
    @PostMapping("/report")
    public Map<String, Object> reportStatus(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        String taskId = (String) params.get("taskId");
        String clientIp = (String) params.get("clientIp");
        String clientName = (String) params.get("clientName");
        String status = (String) params.get("status");
        int currentRound = params.get("currentRound") != null ? 
            (int) params.get("currentRound") : 0;
        int totalRounds = params.get("totalRounds") != null ? 
            (int) params.get("totalRounds") : 0;
        double accuracy = params.get("accuracy") != null ? 
            (double) params.get("accuracy") : 0.0;
        double loss = params.get("loss") != null ? 
            (double) params.get("loss") : 0.0;
        
        if (taskId == null || clientIp == null) {
            result.put("success", false);
            result.put("message", "缺少必要参数");
            return result;
        }
        
        // 创建或更新状态
        TrainingStatus ts = new TrainingStatus();
        ts.clientIp = clientIp;
        ts.clientName = clientName;
        ts.status = status;
        ts.currentRound = currentRound;
        ts.totalRounds = totalRounds;
        ts.accuracy = accuracy;
        ts.loss = loss;
        ts.lastUpdate = System.currentTimeMillis();
        
        // 存储
        Map<String, TrainingStatus> statuses = clientStatuses.get(taskId, k -> new ConcurrentHashMap<>());
        statuses.put(clientIp, ts);
        
        // 记录历史
        Map<String, Object> history = new HashMap<>();
        history.put("taskId", taskId);
        history.put("clientIp", clientIp);
        history.put("status", status);
        history.put("currentRound", currentRound);
        history.put("accuracy", accuracy);
        history.put("timestamp", System.currentTimeMillis());
        List<Map<String, Object>> historyList = trainingHistory.get("history", k -> new ArrayList<>());
        historyList.add(history);
        
        result.put("success", true);
        result.put("message", "状态已更新");
        
        return result;
    }
    
    /**
     * 获取任务的所有客户端状态（一级管理员）
     */
    @GetMapping("/task/{taskId}/all")
    public Map<String, Object> getAllClientStatus(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, TrainingStatus> statuses = clientStatuses.getIfPresent(taskId);
        
        // 统计信息
        int totalClients = statuses != null ? statuses.size() : 0;
        int trainingCount = 0;
        int completedCount = 0;
        double avgAccuracy = 0;
        
        if (statuses != null) {
            for (TrainingStatus ts : statuses.values()) {
                if ("TRAINING".equals(ts.status)) trainingCount++;
                if ("COMPLETED".equals(ts.status)) completedCount++;
                avgAccuracy += ts.accuracy;
            }
            if (totalClients > 0) avgAccuracy /= totalClients;
        }
        
        result.put("success", true);
        result.put("taskId", taskId);
        result.put("clients", statuses != null ? statuses.values() : new ArrayList<>());
        result.put("statistics", Map.of(
            "totalClients", totalClients,
            "trainingCount", trainingCount,
            "completedCount", completedCount,
            "averageAccuracy", avgAccuracy
        ));
        
        return result;
    }
    
    /**
     * 获取指定客户端状态（二级管理员）
     */
    @GetMapping("/task/{taskId}/client/{clientIp}")
    public Map<String, Object> getClientStatus(
            @PathVariable String taskId, 
            @PathVariable String clientIp) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, TrainingStatus> statuses = clientStatuses.getIfPresent(taskId);
        TrainingStatus ts = statuses != null ? statuses.get(clientIp) : null;
        
        if (ts == null) {
            result.put("success", false);
            result.put("message", "没有该客户端的状态");
            return result;
        }
        
        result.put("success", true);
        result.put("status", ts);
        
        return result;
    }
    
    /**
     * 获取用户参与的任务列表
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserTasks(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 简单实现：返回所有任务（实际应根据用户角色过滤）
        List<Map<String, Object>> userTasks = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, TrainingStatus>> entry : clientStatuses.asMap().entrySet()) {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("taskId", entry.getKey());
            taskInfo.put("clientCount", entry.getValue().size());
            
            // 查找该用户的任务
            for (TrainingStatus ts : entry.getValue().values()) {
                taskInfo.put("myStatus", ts);
                break;
            }
            
            userTasks.add(taskInfo);
        }
        
        result.put("success", true);
        result.put("tasks", userTasks);
        
        return result;
    }
    
    /**
     * 获取训练历史
     */
    @GetMapping("/history")
    public Map<String, Object> getHistory() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("history", trainingHistory.getIfPresent("history"));
        return result;
    }
}
