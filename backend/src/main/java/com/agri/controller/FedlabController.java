package com.agri.controller;

import com.agri.dto.FedLabTaskConfigDTO;
import com.agri.mapper.TrainingTaskMapper;
import com.agri.model.TrainingTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fedlab")
@CrossOrigin
public class FedlabController {

    @Autowired
    private TrainingTaskMapper trainingTaskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/task/create")
    public Map<String, Object> createTask(@RequestBody FedLabTaskConfigDTO config) {
        Map<String, Object> result = new HashMap<>();

        TrainingTask task = new TrainingTask();
        task.setName(config.getTaskName() != null ? config.getTaskName() : "FedLab任务");
        task.setDatasetPath(config.getDataset() != null ? config.getDataset() : "default");
        task.setModelType(config.getAlgorithm() != null ? config.getAlgorithm() : "FedAvg");
        task.setStatus("CREATED");
        task.setCreatedAt(LocalDateTime.now());

        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("serverIp", config.getServerIp());
        taskParams.put("serverPort", config.getServerPort() != null ? config.getServerPort() : 3002);
        taskParams.put("worldSize", config.getWorldSize());
        taskParams.put("communicationRounds", config.getCommunicationRounds() != null ? config.getCommunicationRounds() : 10);
        taskParams.put("localEpochs", config.getLocalEpochs() != null ? config.getLocalEpochs() : 1);
        taskParams.put("participants", new ArrayList<String>());

        saveParameters(task, taskParams);
        trainingTaskMapper.insert(task);

        result.put("success", true);
        result.put("taskId", String.valueOf(task.getId()));
        result.put("message", "任务创建成功");

        return result;
    }

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

        return result;
    }

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

        if (participants == null || participants.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有参与者");
            return result;
        }

        task.setStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        trainingTaskMapper.updateById(task);

        result.put("success", true);
        result.put("message", "任务已启动");
        result.put("serverIp", taskParams.get("serverIp"));
        result.put("serverPort", taskParams.get("serverPort"));
        result.put("worldSize", participants.size() + 1);

        return result;
    }

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
        status.put("accuracy", task.getAccuracy());
        status.put("participants", taskParams.get("participants"));

        result.put("success", true);
        result.put("status", status);

        return result;
    }

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

    @GetMapping("/task/{taskId}/download-script")
    public Map<String, Object> downloadScript(@PathVariable String taskId, 
                                               @RequestParam(required = false) String clientIp) {
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

        int rank = 0;
        if (clientIp != null && participants.contains(clientIp)) {
            rank = participants.indexOf(clientIp) + 1;
        }

        Map<String, Object> scriptConfig = new HashMap<>();
        scriptConfig.put("taskId", String.valueOf(task.getId()));
        scriptConfig.put("serverIp", taskParams.get("serverIp"));
        scriptConfig.put("serverPort", taskParams.get("serverPort"));
        scriptConfig.put("worldSize", participants.size() + 1);
        scriptConfig.put("rank", rank);
        scriptConfig.put("communicationRounds", taskParams.get("communicationRounds"));
        scriptConfig.put("localEpochs", taskParams.get("localEpochs"));
        scriptConfig.put("algorithm", task.getModelType());

        result.put("success", true);
        result.put("scriptConfig", scriptConfig);

        return result;
    }

    private Map<String, Object> parseParameters(String parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(parameters, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void saveParameters(TrainingTask task, Map<String, Object> params) {
        try {
            task.setParameters(objectMapper.writeValueAsString(params));
        } catch (Exception e) {
            task.setParameters("{}");
        }
    }
}
