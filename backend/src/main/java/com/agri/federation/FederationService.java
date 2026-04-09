package com.agri.federation;

import com.agri.mapper.TrainingTaskMapper;
import com.agri.model.TrainingTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FederationService {
    
    @Autowired
    private TrainingTaskMapper trainingTaskMapper;
    
    private ObjectMapper mapper = new ObjectMapper();
    
    public String createTask(String taskName, String algorithm, int rounds, int clients) {
        TrainingTask task = new TrainingTask();
        task.setName(taskName);
        task.setModelType(algorithm);
        task.setStatus("PENDING");
        task.setCreatedAt(LocalDateTime.now());
        
        Map<String, Object> params = new HashMap<>();
        params.put("totalRounds", rounds);
        params.put("expectedClients", clients);
        params.put("currentRound", 0);
        
        try {
            task.setParameters(mapper.writeValueAsString(params));
        } catch (JsonProcessingException e) {
            task.setParameters("{}");
        }
        
        trainingTaskMapper.insert(task);
        return String.valueOf(task.getId());
    }
    
    // FedAvg removed per user request
    public TrainingTask getTask(String taskId) {
        try {
            return trainingTaskMapper.selectById(Long.valueOf(taskId));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public List<TrainingTask> getAllTasks() {
        return trainingTaskMapper.selectList(null);
    }
    
    public Map<String, Object> getGlobalModel(String taskId) {
        TrainingTask task = getTask(taskId);
        if (task == null) return null;
        Map<String, Object> model = new HashMap<>();
        model.put("taskId", taskId);
        model.put("taskName", task.getName());
        model.put("algorithm", task.getModelType());
        model.put("status", task.getStatus());
        
        try {
            if (task.getParameters() != null) {
                Map<String, Object> params = mapper.readValue(task.getParameters(), Map.class);
                model.put("round", params.getOrDefault("currentRound", 0));
            } else {
                model.put("round", 0);
            }
        } catch (Exception e) {
            model.put("round", 0);
        }
        
        return model;
    }
}
