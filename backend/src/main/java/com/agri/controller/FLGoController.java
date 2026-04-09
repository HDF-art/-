package com.agri.controller;

import com.agri.mapper.TrainingTaskMapper;
import com.agri.model.TrainingTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.LocalDateTime;

/**
 * FLGo联邦学习控制器
 * 支持多机分布式训练
 */
@RestController
@RequestMapping("/api/flgo")
@CrossOrigin
public class FLGoController {
    
    @Autowired
    private TrainingTaskMapper trainingTaskMapper;
    
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
    
    private Map<String, Object> parseParameters(String parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return new HashMap<>();
        }
        try {
            // Very simple JSON string parsing bypass since we control creation
            // In a real app, use ObjectMapper
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(parameters, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    private void saveParameters(TrainingTask task, Map<String, Object> params) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            task.setParameters(mapper.writeValueAsString(params));
        } catch (Exception e) {
            task.setParameters("{}");
        }
    }
    
    /**
     * 创建联邦学习任务
     */
    @PostMapping("/task/create")
    public Map<String, Object> createTask(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        String taskName = (String) params.getOrDefault("taskName", "默认任务");
        String dataset = (String) params.getOrDefault("dataset", "mnist");
        String algorithm = (String) params.getOrDefault("algorithm", "fedavg");
        int numClients = (int) params.getOrDefault("numClients", 5);
        int numRounds = (int) params.getOrDefault("numRounds", 10);
        int numEpochs = (int) params.getOrDefault("numEpochs", 1);
        
        TrainingTask task = new TrainingTask();
        task.setName(taskName);
        task.setDatasetPath(dataset);
        task.setModelType(algorithm);
        task.setStatus("CREATED");
        task.setCreatedAt(LocalDateTime.now());
        
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("numClients", numClients);
        taskParams.put("numRounds", numRounds);
        taskParams.put("numEpochs", numEpochs);
        taskParams.put("participants", new ArrayList<String>());
        
        saveParameters(task, taskParams);
        trainingTaskMapper.insert(task);
        
        result.put("success", true);
        result.put("taskId", String.valueOf(task.getId()));
        result.put("message", "任务创建成功");
        
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("taskId", String.valueOf(task.getId()));
        taskMap.put("taskName", taskName);
        taskMap.put("dataset", dataset);
        taskMap.put("algorithm", algorithm);
        taskMap.put("numClients", numClients);
        taskMap.put("numRounds", numRounds);
        taskMap.put("numEpochs", numEpochs);
        taskMap.put("status", "CREATED");
        taskMap.put("createTime", System.currentTimeMillis());
        result.put("task", taskMap);
        
        return result;
    }
    
    /**
     * 客户端注册参与任务
     */
    @PostMapping("/participant/register")
    public Map<String, Object> registerParticipant(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        
        String taskIdStr = params.get("taskId");
        String clientIp = params.get("clientIp");
        
        if (taskIdStr == null || clientIp == null) {
            result.put("success", false);
            result.put("message", "缺少必要参数");
            return result;
        }
        
        TrainingTask task = trainingTaskMapper.selectById(Long.valueOf(taskIdStr));
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        Map<String, Object> taskParams = parseParameters(task.getParameters());
        List<String> participants = (List<String>) taskParams.getOrDefault("participants", new ArrayList<String>());
        
        if (!participants.contains(clientIp)) {
            participants.add(clientIp);
            taskParams.put("participants", participants);
            saveParameters(task, taskParams);
            trainingTaskMapper.updateById(task);
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
        
        String taskIdStr = params.get("taskId");
        int serverIndex = params.get("serverIndex") != null ? 
            Integer.parseInt(params.get("serverIndex")) : 0;
        
        TrainingTask task = trainingTaskMapper.selectById(Long.valueOf(taskIdStr));
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        Map<String, Object> taskParams = parseParameters(task.getParameters());
        List<String> participants = (List<String>) taskParams.get("participants");
        
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
        
        TrainingTask task = trainingTaskMapper.selectById(Long.valueOf(taskId));
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        Map<String, Object> taskInfo = new HashMap<>();
        taskInfo.put("taskId", String.valueOf(task.getId()));
        taskInfo.put("taskName", task.getName());
        taskInfo.put("dataset", task.getDatasetPath());
        taskInfo.put("algorithm", task.getModelType());
        taskInfo.put("status", task.getStatus());
        
        Map<String, Object> taskParams = parseParameters(task.getParameters());
        taskInfo.putAll(taskParams);
        
        result.put("success", true);
        result.put("task", taskInfo);
        result.put("participants", taskParams.get("participants"));
        
        return result;
    }
    
    /**
     * 获取所有任务
     */
    @GetMapping("/tasks")
    public Map<String, Object> listTasks() {
        Map<String, Object> result = new HashMap<>();
        List<TrainingTask> allTasks = trainingTaskMapper.selectList(null);
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        for (TrainingTask task : allTasks) {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("taskId", String.valueOf(task.getId()));
            taskInfo.put("taskName", task.getName());
            taskInfo.put("status", task.getStatus());
            tasks.add(taskInfo);
        }
        
        result.put("success", true);
        result.put("tasks", tasks);
        return result;
    }
    
    /**
     * 启动任务
     */
    @PostMapping("/task/{taskId}/start")
    public Map<String, Object> startTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        TrainingTask task = trainingTaskMapper.selectById(Long.valueOf(taskId));
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        Map<String, Object> taskParams = parseParameters(task.getParameters());
        List<String> participants = (List<String>) taskParams.get("participants");
        
        if (participants == null || participants.size() < 2) {
            result.put("success", false);
            result.put("message", "参与者数量不足，至少需要2个");
            return result;
        }
        
        task.setStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        trainingTaskMapper.updateById(task);
        
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
        
        TrainingTask task = trainingTaskMapper.selectById(Long.valueOf(taskId));
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        task.setStatus("STOPPED");
        task.setEndTime(LocalDateTime.now());
        trainingTaskMapper.updateById(task);
        
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
        
        TrainingTask task = trainingTaskMapper.selectById(Long.valueOf(taskId));
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        Map<String, Object> taskParams = parseParameters(task.getParameters());
        
        Map<String, Object> status = new HashMap<>();
        status.put("taskId", String.valueOf(task.getId()));
        status.put("status", task.getStatus());
        status.put("currentRound", taskParams.getOrDefault("currentRound", 0));
        status.put("totalRounds", taskParams.get("numRounds"));
        status.put("accuracy", task.getAccuracy());
        status.put("participants", taskParams.get("participants"));
        
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
        
        TrainingTask task = trainingTaskMapper.selectById(Long.valueOf(taskId));
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        Map<String, Object> taskParams = parseParameters(task.getParameters());
        List<String> participants = (List<String>) taskParams.get("participants");
        
        if (participants == null) {
            participants = new ArrayList<>();
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
        
        TrainingTask task = trainingTaskMapper.selectById(Long.valueOf(taskId));
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        
        Map<String, Object> taskParams = parseParameters(task.getParameters());
        List<String> participants = (List<String>) taskParams.get("participants");
        
        if (participants == null || participants.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有参与者");
            return result;
        }
        
        String selectedServer = participants.get(0);
        
        Map<String, Object> scriptConfig = new HashMap<>();
        scriptConfig.put("taskId", String.valueOf(task.getId()));
        scriptConfig.put("serverIp", selectedServer);
        scriptConfig.put("participants", participants);
        scriptConfig.put("algorithm", task.getModelType());
        scriptConfig.put("numRounds", taskParams.get("numRounds"));
        scriptConfig.put("numEpochs", taskParams.get("numEpochs"));
        
        result.put("success", true);
        result.put("scriptConfig", scriptConfig);
        
        return result;
    }
}
