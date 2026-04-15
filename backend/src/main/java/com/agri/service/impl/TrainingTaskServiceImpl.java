package com.agri.service.impl;

import com.agri.mapper.TrainingTaskMapper;
import com.agri.model.TaskParticipation;
import com.agri.model.TrainingTask;
import com.agri.service.TaskParticipationService;
import com.agri.service.TrainingTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TrainingTaskServiceImpl extends ServiceImpl<TrainingTaskMapper, TrainingTask> implements TrainingTaskService {

    @Autowired
    private TrainingTaskMapper trainingTaskMapper;

    @Autowired
    private TaskParticipationService taskParticipationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${fedlab.script.path:../models/fedlab_server_runner.py}")
    private String fedlabScriptPath;

    @Override
    public boolean createTask(TrainingTask task) {
        task.setStatus("待开始");
        task.setCreatedAt(LocalDateTime.now());
        return save(task);
    }

    @Override
    public boolean updateTaskStatus(Long taskId, String status) {
        TrainingTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        task.setStatus(status);
        if ("进行中".equals(status)) {
            task.setStartTime(LocalDateTime.now());
        } else if ("已完成".equals(status) || "已失败".equals(status)) {
            task.setEndTime(LocalDateTime.now());
        }
        return updateById(task);
    }

    @Override
    public boolean startTask(Long taskId) {
        TrainingTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        task.setStatus("进行中");
        task.setStartTime(LocalDateTime.now());
        return updateById(task);
    }

    @Override
    public boolean stopTask(Long taskId) {
        TrainingTask task = getById(taskId);
        if (task != null && "进行中".equals(task.getStatus())) {
            return updateTaskStatus(taskId, "已失败");
        }
        return false;
    }

    @Override
    public boolean auditTask(Long taskId, String auditStatus, String auditComment, Long auditedBy) {
        TrainingTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        return updateById(task);
    }

    @Override
    public boolean publishTask(Long taskId, List<Long> userIds) {
        TrainingTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        List<TaskParticipation> participations = new ArrayList<>();
        for (Long userId : userIds) {
            TaskParticipation participation = new TaskParticipation();
            participation.setTaskId(taskId);
            participation.setUserId(userId);
            participations.add(participation);
        }
        return taskParticipationService.batchCreateParticipations(participations);
    }

    @Override
    public List<TrainingTask> getTasksByAuditStatus(String auditStatus) {
        return new ArrayList<>();
    }

    @Override
    public List<TrainingTask> getTasksByCreatedBy(Long createdBy) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TrainingTask>().eq("created_by", createdBy));
    }

    @Override
    public List<TrainingTask> getParticipatedTasks(Long userId) {
        return new ArrayList<>();
    }

    public boolean startFedLabServer(Long taskId) {
        try {
            TrainingTask task = getById(taskId);
            if (task == null) {
                return false;
            }

            Map<String, Object> taskParams = parseParameters(task.getParameters());
            String serverIp = (String) taskParams.get("serverIp");
            Integer serverPort = taskParams.get("serverPort") != null ? 
                (Integer) taskParams.get("serverPort") : 3002;
            Integer worldSize = taskParams.get("worldSize") != null ? 
                (Integer) taskParams.get("worldSize") : 2;
            Integer communicationRounds = taskParams.get("communicationRounds") != null ? 
                (Integer) taskParams.get("communicationRounds") : 10;
            Integer localEpochs = taskParams.get("localEpochs") != null ? 
                (Integer) taskParams.get("localEpochs") : 1;

            List<String> command = new ArrayList<>();
            command.add("python3");
            command.add(fedlabScriptPath);
            command.add("--server_ip");
            command.add(serverIp != null ? serverIp : "0.0.0.0");
            command.add("--server_port");
            command.add(String.valueOf(serverPort));
            command.add("--world_size");
            command.add(String.valueOf(worldSize));
            command.add("--rank");
            command.add("0");
            command.add("--communication_rounds");
            command.add(String.valueOf(communicationRounds));
            command.add("--local_epochs");
            command.add(String.valueOf(localEpochs));
            command.add("--task_id");
            command.add(String.valueOf(taskId));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FedLab] " + line);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<String, Object> parseParameters(String parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            return objectMapper.readValue(parameters, Map.class);
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
}
