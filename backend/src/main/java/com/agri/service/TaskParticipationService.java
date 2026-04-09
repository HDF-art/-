package com.agri.service;

import com.agri.model.TaskParticipation;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 任务参与服务接口
 */
public interface TaskParticipationService extends IService<TaskParticipation> {

    /**
     * 创建任务参与记录
     * @param participation 参与信息
     * @return 创建结果
     */
    boolean createParticipation(TaskParticipation participation);

    /**
     * 更新参与状态
     * @param id 参与ID
     * @param status 状态
     * @return 更新结果
     */
    boolean updateParticipationStatus(Long id, String status);

    /**
     * 根据任务ID获取参与列表
     * @param taskId 任务ID
     * @return 参与列表
     */
    List<TaskParticipation> getByTaskId(Long taskId);

    /**
     * 根据用户ID和任务ID获取参与信息
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 参与信息
     */
    TaskParticipation getByUserIdAndTaskId(Long userId, Long taskId);

    /**
     * 根据任务ID和状态获取参与数量
     * @param taskId 任务ID
     * @param status 状态
     * @return 参与数量
     */
    Integer countByTaskIdAndStatus(Long taskId, String status);

    /**
     * 批量创建任务参与记录
     * @param participations 参与信息列表
     * @return 创建结果
     */
    boolean batchCreateParticipations(List<TaskParticipation> participations);

}
