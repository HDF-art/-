package com.agri.controller;

import com.agri.model.TrainingLog;
import com.agri.service.TrainingLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 训练日志控制器
 */
@RestController
@RequestMapping("/training-logs")
public class TrainingLogController {

    @Autowired
    private TrainingLogService trainingLogService;

    /**
     * 根据任务ID获取日志列表
     * @param taskId 任务ID
     * @return 日志列表
     */
    @GetMapping("/task/{taskId}")
    public Map<String, Object> getLogsByTaskId(@PathVariable Long taskId) {
        List<TrainingLog> logs = trainingLogService.getLogsByTaskId(taskId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取训练日志成功");
        response.put("data", logs);
        
        return response;
    }

    /**
     * 根据任务ID和日志类型获取日志列表
     * @param taskId 任务ID
     * @param logType 日志类型
     * @return 日志列表
     */
    @GetMapping("/task/{taskId}/type/{logType}")
    public Map<String, Object> getLogsByTaskIdAndType(@PathVariable Long taskId, @PathVariable String logType) {
        List<TrainingLog> logs = trainingLogService.getLogsByTaskIdAndType(taskId, logType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取训练日志成功");
        response.put("data", logs);
        
        return response;
    }

    /**
     * 获取任务的最新日志
     * @param taskId 任务ID
     * @param limit 限制数量
     * @return 日志列表
     */
    @GetMapping("/task/{taskId}/latest")
    public Map<String, Object> getLatestLogsByTaskId(@PathVariable Long taskId, 
                                                   @RequestParam(defaultValue = "50") Integer limit) {
        List<TrainingLog> logs = trainingLogService.getLatestLogsByTaskId(taskId, limit);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取最新训练日志成功");
        response.put("data", logs);
        
        return response;
    }

    /**
     * 添加训练日志
     * @param taskId 任务ID
     * @param logType 日志类型
     * @param content 日志内容
     * @param nodeId 节点ID
     * @param round 轮次
     * @return 日志对象
     */
    @PostMapping("/add")
    public Map<String, Object> addLog(@RequestParam Long taskId, 
                                    @RequestParam String logType, 
                                    @RequestParam String content, 
                                    @RequestParam(required = false) String nodeId, 
                                    @RequestParam(required = false) Integer round) {
        TrainingLog log = trainingLogService.addLog(taskId, logType, content, nodeId, round);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "添加训练日志成功");
        response.put("data", log);
        
        return response;
    }

    /**
     * 清空任务的所有日志
     * @param taskId 任务ID
     * @return 操作结果
     */
    @DeleteMapping("/task/{taskId}")
    public Map<String, Object> clearLogsByTaskId(@PathVariable Long taskId) {
        boolean success = trainingLogService.clearLogsByTaskId(taskId);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("code", 200);
            response.put("message", "清空训练日志成功");
            response.put("data", true);
        } else {
            response.put("code", 500);
            response.put("message", "清空训练日志失败");
            response.put("data", false);
        }
        
        return response;
    }

}
