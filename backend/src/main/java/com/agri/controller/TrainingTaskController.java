package com.agri.controller;

import com.agri.model.TrainingTask;
import com.agri.service.TrainingTaskService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 训练任务控制器
 */
@RestController
@RequestMapping("/training-tasks")
public class TrainingTaskController {

    @Autowired
    private TrainingTaskService trainingTaskService;

    /**
     * 根据ID获取训练任务
     * @param taskId 任务ID
     * @return 训练任务
     */
    @GetMapping("/{id}")
    public TrainingTask getTaskById(@PathVariable("id") Long taskId) {
        return trainingTaskService.getById(taskId);
    }

    /**
     * 分页获取训练任务列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping
    public Page<TrainingTask> getTaskList(@RequestParam(defaultValue = "1") Integer page, 
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return trainingTaskService.page(new Page<>(page, pageSize));
    }

    /**
     * 根据审核状态获取任务列表
     * @param auditStatus 审核状态
     * @return 任务列表
     */
    @GetMapping("/audit-status/{status}")
    public List<TrainingTask> getTasksByAuditStatus(@PathVariable("status") String auditStatus) {
        return trainingTaskService.getTasksByAuditStatus(auditStatus);
    }

    /**
     * 根据创建者ID获取任务列表
     * @param createdBy 创建者ID
     * @return 任务列表
     */
    @GetMapping("/created-by/{id}")
    public List<TrainingTask> getTasksByCreatedBy(@PathVariable("id") Long createdBy) {
        return trainingTaskService.getTasksByCreatedBy(createdBy);
    }

    /**
     * 获取用户参与的任务列表
     * @param userId 用户ID
     * @return 任务列表
     */
    @GetMapping("/participated/{userId}")
    public List<TrainingTask> getParticipatedTasks(@PathVariable("userId") Long userId) {
        return trainingTaskService.getParticipatedTasks(userId);
    }

    /**
     * 创建训练任务
     * @param task 训练任务信息
     * @return 创建结果
     */
    @PostMapping
    public boolean createTask(@RequestBody TrainingTask task) {
        return trainingTaskService.createTask(task);
    }

    /**
     * 更新训练任务
     * @param task 训练任务信息
     * @return 更新结果
     */
    @PutMapping
    public boolean updateTask(@RequestBody TrainingTask task) {
        return trainingTaskService.updateById(task);
    }

    /**
     * 审核任务
     * @param taskId 任务ID
     * @param auditInfo 审核信息
     * @return 审核结果
     */
    @PostMapping("/{id}/audit")
    public boolean auditTask(@PathVariable("id") Long taskId, @RequestBody Map<String, Object> auditInfo) {
        String auditStatus = (String) auditInfo.get("auditStatus");
        String auditComment = (String) auditInfo.get("auditComment");
        Long auditedBy = (Long) auditInfo.get("auditedBy");
        return trainingTaskService.auditTask(taskId, auditStatus, auditComment, auditedBy);
    }

    /**
     * 发布任务
     * @param taskId 任务ID
     * @param publishInfo 发布信息
     * @return 发布结果
     */
    @PostMapping("/{id}/publish")
    public boolean publishTask(@PathVariable("id") Long taskId, @RequestBody Map<String, Object> publishInfo) {
        List<Long> userIds = (List<Long>) publishInfo.get("userIds");
        return trainingTaskService.publishTask(taskId, userIds);
    }

    /**
     * 开始训练任务
     * @param taskId 任务ID
     * @return 开始结果
     */
    @PostMapping("/{id}/start")
    public boolean startTask(@PathVariable("id") Long taskId) {
        return trainingTaskService.startTask(taskId);
    }

    /**
     * 停止训练任务
     * @param taskId 任务ID
     * @return 停止结果
     */
    @PostMapping("/{id}/stop")
    public boolean stopTask(@PathVariable("id") Long taskId) {
        return trainingTaskService.stopTask(taskId);
    }

    /**
     * 删除训练任务
     * @param taskId 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteTask(@PathVariable("id") Long taskId) {
        return trainingTaskService.removeById(taskId);
    }

}