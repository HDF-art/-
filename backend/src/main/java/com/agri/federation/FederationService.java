package com.agri.federation;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FederationService {
    private Map<String, FederatedTask> tasks = new HashMap<>();
    
    public FederatedTask createTask(String taskName, String algorithm, int rounds, int clients) {
        FederatedTask task = new FederatedTask();
        task.setId(UUID.randomUUID().toString());
        task.setTaskName(taskName);
        task.setAlgorithm(algorithm);
        task.setTotalRounds(rounds);
        task.setExpectedClients(clients);
        task.setCurrentRound(0);
        task.setStatus("PENDING");
        task.setCreateTime(new Date());
        tasks.put(task.getId(), task);
        return task;
    }
    
    public double[] fedAvg(List<double[]> clientModels, int[] clientWeights) {
        if (clientModels.isEmpty()) return null;
        int modelSize = clientModels.get(0).length;
        double[] aggregated = new double[modelSize];
        int totalWeight = 0;
        for (int w : clientWeights) totalWeight += w;
        for (int i = 0; i < modelSize; i++) {
            double sum = 0;
            for (int j = 0; j < clientModels.size(); j++) {
                sum += clientModels.get(j)[i] * clientWeights[j];
            }
            aggregated[i] = sum / totalWeight;
        }
        return aggregated;
    }
    
    public FederatedTask getTask(String taskId) {
        return tasks.get(taskId);
    }
    
    public List<FederatedTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }
    
    public Map<String, Object> getGlobalModel(String taskId) {
        FederatedTask task = tasks.get(taskId);
        if (task == null) return null;
        Map<String, Object> model = new HashMap<>();
        model.put("taskId", taskId);
        model.put("taskName", task.getTaskName());
        model.put("algorithm", task.getAlgorithm());
        model.put("round", task.getCurrentRound());
        model.put("status", task.getStatus());
        return model;
    }
}
