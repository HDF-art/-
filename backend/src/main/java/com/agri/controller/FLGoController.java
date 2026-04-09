package com.agri.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * FLGo联邦学习控制器
 * 支持多机分布式训练
 */
@RestController
@RequestMapping("/api/flgo")
@CrossOrigin
public class FLGoController {
    
    // 任务存储
    private static final Cache<String, Map<String, Object>> tasks = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    // 参与客户端记录
    private static final Cache<String, List<String>> taskParticipants = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    // 服务器IP配置
    private static String serverIp = null;
    
    /**
     * 配置服务器IP
     */
    @PostMapping("/config/server")
    public Map<String, Object> configServer(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String ip = params.get("serverIp");
        if (ip == null || ip.isEmpty()) {
            result.put("success", false);
            result.put("message", "服务器IP不能为空");
            return result;
        }
        serverIp = ip;
        result.put("success", true);
        result.put("message", "服务器IP已配置: " + ip);
        return result;
    }
    
    /**
     * 获取服务器IP
     */
    @GetMapping("/config/server")
    public Map<String, Object> getServerConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("serverIp", serverIp);
        return result;
    }
    
    /**
     * 创建联邦学习任务
     */
    @PostMapping("/task/create")
    public Map<String, Object> createTask(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        String taskId = UUID.randomUUID().toString();
        String taskName = (String) params.getOrDefault("taskName", "默认任务");
        String dataset = (String) params.getOrDefault("dataset", "mnist");
        String algorithm = (String) params.getOrDefault("algorithm", "fedavg");
        int numClients = (int) params.getOrDefault("numClients", 5);
        int numRounds = (int) params.getOrDefault("numRounds", 10);
        int numEpochs = (int) params.getOrDefault("numEpochs", 1);
        
        Map<String, Object> task = new HashMap<>();
        task.put("taskId", taskId);
        task.put("taskName", taskName);
        task.put("dataset", dataset);
        task.put("algorithm", algorithm);
        task.put("numClients", numClients);
        task.put("numRounds", numRounds);
        task.put("numEpochs", numEpochs);
        task.put("status", "CREATED");
        task.put("createTime", System.currentTimeMillis());
        
        tasks.get(taskId, k -> task);
        taskParticipants.get(taskId, k -> new ArrayList<>());
        
        result.put("success", true);
        result.put("taskId", taskId);
        result.put("message", "任务创建成功");
        result.put("task", task);
        
        return result;
    }
    
    /**
     * 客户端注册参与任务
     */
    @PostMapping("/participant/register")
    public Map<String, Object> registerParticipant(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        
        String taskId = params.get("taskId");
        String clientIp = params.get("clientIp");
        String clientName = params.get("clientName");
        
        if (taskId == null || clientIp == null) {
            result.put("success", false);
            result.put("message", "缺少必要参数");
            return result;
        }
        
        if (tasks.getIfPresent(taskId) == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        List<String> participants = taskParticipants.get(taskId, k -> new ArrayList<>());
        if (!participants.contains(clientIp)) {
            participants.add(clientIp);
        }
        
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("participantCount", participants.size());
        
        return result;
    }
    
    /**
     * 从参与者中选择服务器
     */
    @PostMapping("/task/select-server")
    public Map<String, Object> selectServer(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        
        String taskId = params.get("taskId");
        int serverIndex = params.get("serverIndex") != null ? 
            Integer.parseInt(params.get("serverIndex")) : 0;
        
        if (tasks.getIfPresent(taskId) == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        List<String> participants = taskParticipants.getIfPresent(taskId);
        if (participants == null || participants.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有参与者");
            return result;
        }
        
        if (serverIndex >= participants.size()) {
            serverIndex = 0;
        }
        
        String selectedServer = participants.get(serverIndex);
        serverIp = selectedServer;
        
        result.put("success", true);
        result.put("serverIp", selectedServer);
        result.put("message", "服务器已选择");
        
        return result;
    }
    
    /**
     * 获取任务信息
     */
    @GetMapping("/task/{taskId}")
    public Map<String, Object> getTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> task = tasks.getIfPresent(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        result.put("success", true);
        result.put("task", task);
        result.put("participants", taskParticipants.getIfPresent(taskId));
        
        return result;
    }
    
    /**
     * 获取所有任务
     */
    @GetMapping("/tasks")
    public Map<String, Object> listTasks() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tasks", tasks.asMap().values());
        return result;
    }
    
    /**
     * 启动任务
     */
    @PostMapping("/task/{taskId}/start")
    public Map<String, Object> startTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> task = tasks.getIfPresent(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        List<String> participants = taskParticipants.getIfPresent(taskId);
        if (participants == null || participants.size() < 2) {
            result.put("success", false);
            result.put("message", "参与者数量不足，至少需要2个");
            return result;
        }
        
        task.put("status", "RUNNING");
        task.put("startTime", System.currentTimeMillis());
        
        result.put("success", true);
        result.put("message", "任务已启动");
        result.put("serverIp", serverIp);
        result.put("participants", participants);
        
        return result;
    }
    
    /**
     * 停止任务
     */
    @PostMapping("/task/{taskId}/stop")
    public Map<String, Object> stopTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> task = tasks.getIfPresent(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        task.put("status", "STOPPED");
        task.put("endTime", System.currentTimeMillis());
        
        result.put("success", true);
        result.put("message", "任务已停止");
        
        return result;
    }
    
    /**
     * 获取任务状态
     */
    @GetMapping("/task/{taskId}/status")
    public Map<String, Object> getTaskStatus(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> task = tasks.getIfPresent(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("taskId", taskId);
        status.put("status", task.get("status"));
        status.put("currentRound", task.getOrDefault("currentRound", 0));
        status.put("totalRounds", task.get("numRounds"));
        status.put("accuracy", task.getOrDefault("accuracy", 0.0));
        status.put("loss", task.getOrDefault("loss", 0.0));
        status.put("participants", taskParticipants.getIfPresent(taskId));
        
        result.put("success", true);
        result.put("status", status);
        
        return result;
    }
    
    /**
     * 获取所有参与者IP列表
     */
    @GetMapping("/task/{taskId}/participants")
    public Map<String, Object> getParticipants(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        List<String> participants = taskParticipants.getIfPresent(taskId);
        if (participants == null) {
            result.put("success", false);
            result.put("message", "任务不存在或没有参与者");
            return result;
        }
        
        result.put("success", true);
        result.put("participants", participants);
        result.put("count", participants.size());
        
        return result;
    }
    
    /**
     * 下载客户端配置脚本
     */
    @GetMapping("/task/{taskId}/download-script")
    public Map<String, Object> downloadScript(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> task = tasks.getIfPresent(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        List<String> participants = taskParticipants.getIfPresent(taskId);
        if (participants == null || participants.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有参与者");
            return result;
        }
        
        String selectedServer = participants.get(0);
        
        Map<String, Object> scriptConfig = new HashMap<>();
        scriptConfig.put("taskId", taskId);
        scriptConfig.put("serverIp", selectedServer);
        scriptConfig.put("participants", participants);
        scriptConfig.put("algorithm", task.get("algorithm"));
        scriptConfig.put("numRounds", task.get("numRounds"));
        scriptConfig.put("numEpochs", task.get("numEpochs"));
        
        result.put("success", true);
        result.put("scriptConfig", scriptConfig);
        
        return result;
    }
}
