package com.agri.mapper;

import com.agri.model.TaskParticipation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 任务参与Mapper接口
 */
@Mapper
public interface TaskParticipationMapper extends BaseMapper<TaskParticipation> {

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

}
