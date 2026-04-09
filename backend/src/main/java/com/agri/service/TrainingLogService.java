package com.agri.service;

import com.agri.model.TrainingLog;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 训练日志服务接口
 */
public interface TrainingLogService extends IService<TrainingLog> {

    /**
     * 根据任务ID获取日志列表
     * @param taskId 任务ID
     * @return 日志列表
     */
    List<TrainingLog> getLogsByTaskId(Long taskId);

    /**
     * 根据任务ID和日志类型获取日志列表
     * @param taskId 任务ID
     * @param logType 日志类型
     * @return 日志列表
     */
    List<TrainingLog> getLogsByTaskIdAndType(Long taskId, String logType);

    /**
     * 获取任务的最新日志
     * @param taskId 任务ID
     * @param limit 限制数量
     * @return 日志列表
     */
    List<TrainingLog> getLatestLogsByTaskId(Long taskId, Integer limit);

    /**
     * 添加训练日志
     * @param taskId 任务ID
     * @param logType 日志类型
     * @param content 日志内容
     * @param nodeId 节点ID
     * @param round 轮次
     * @return 日志对象
     */
    TrainingLog addLog(Long taskId, String logType, String content, String nodeId, Integer round);

    /**
     * 批量添加训练日志
     * @param logs 日志列表
     * @return 是否添加成功
     */
    boolean addLogs(List<TrainingLog> logs);

    /**
     * 清空任务的所有日志
     * @param taskId 任务ID
     * @return 是否清空成功
     */
    boolean clearLogsByTaskId(Long taskId);

}
