package com.agri.service;

import com.agri.model.TrainingTask;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 训练任务服务接口
 */
public interface TrainingTaskService extends IService<TrainingTask> {

    /**
     * 创建训练任务
     * @param task 训练任务信息
     * @return 创建结果
     */
    boolean createTask(TrainingTask task);

    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param status 状态
     * @return 更新结果
     */
    boolean updateTaskStatus(Long taskId, String status);

    /**
     * 开始训练任务
     * @param taskId 任务ID
     * @return 开始结果
     */
    boolean startTask(Long taskId);

    /**
     * 停止训练任务
     * @param taskId 任务ID
     * @return 停止结果
     */
    boolean stopTask(Long taskId);

    /**
     * 审核任务
     * @param taskId 任务ID
     * @param auditStatus 审核状态
     * @param auditComment 审核意见
     * @param auditedBy 审核人ID
     * @return 审核结果
     */
    boolean auditTask(Long taskId, String auditStatus, String auditComment, Long auditedBy);

    /**
     * 发布任务
     * @param taskId 任务ID
     * @param userIds 参与用户ID列表
     * @return 发布结果
     */
    boolean publishTask(Long taskId, List<Long> userIds);

    /**
     * 根据审核状态获取任务列表
     * @param auditStatus 审核状态
     * @return 任务列表
     */
    List<TrainingTask> getTasksByAuditStatus(String auditStatus);

    /**
     * 根据创建者ID获取任务列表
     * @param createdBy 创建者ID
     * @return 任务列表
     */
    List<TrainingTask> getTasksByCreatedBy(Long createdBy);

    /**
     * 获取用户参与的任务列表
     * @param userId 用户ID
     * @return 任务列表
     */
    List<TrainingTask> getParticipatedTasks(Long userId);

}
