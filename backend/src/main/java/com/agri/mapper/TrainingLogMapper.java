package com.agri.mapper;

import com.agri.model.TrainingLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 训练日志Mapper
 */
@Mapper
public interface TrainingLogMapper extends BaseMapper<TrainingLog> {

    /**
     * 根据任务ID获取日志列表
     * @param taskId 任务ID
     * @return 日志列表
     */
    List<TrainingLog> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据任务ID和日志类型获取日志列表
     * @param taskId 任务ID
     * @param logType 日志类型
     * @return 日志列表
     */
    List<TrainingLog> selectByTaskIdAndType(@Param("taskId") Long taskId, @Param("logType") String logType);

    /**
     * 获取任务的最新日志
     * @param taskId 任务ID
     * @param limit 限制数量
     * @return 日志列表
     */
    List<TrainingLog> selectLatestByTaskId(@Param("taskId") Long taskId, @Param("limit") Integer limit);

}
