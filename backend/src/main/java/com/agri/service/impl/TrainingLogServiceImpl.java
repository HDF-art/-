package com.agri.service.impl;

import com.agri.model.TrainingLog;
import com.agri.mapper.TrainingLogMapper;
import com.agri.service.TrainingLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练日志服务实现
 */
@Service
public class TrainingLogServiceImpl extends ServiceImpl<TrainingLogMapper, TrainingLog> implements TrainingLogService {

    @Autowired
    private TrainingLogMapper trainingLogMapper;

    @Override
    public List<TrainingLog> getLogsByTaskId(Long taskId) {
        return trainingLogMapper.selectByTaskId(taskId);
    }

    @Override
    public List<TrainingLog> getLogsByTaskIdAndType(Long taskId, String logType) {
        return trainingLogMapper.selectByTaskIdAndType(taskId, logType);
    }

    @Override
    public List<TrainingLog> getLatestLogsByTaskId(Long taskId, Integer limit) {
        return trainingLogMapper.selectLatestByTaskId(taskId, limit);
    }

    @Override
    public TrainingLog addLog(Long taskId, String logType, String content, String nodeId, Integer round) {
        TrainingLog log = new TrainingLog();
        log.setTaskId(taskId);
        log.setLogType(logType);
        log.setContent(content);
        log.setTimestamp(LocalDateTime.now());
        log.setNodeId(nodeId);
        log.setRound(round);
        save(log);
        return log;
    }

    @Override
    public boolean addLogs(List<TrainingLog> logs) {
        return saveBatch(logs);
    }

    @Override
    public boolean clearLogsByTaskId(Long taskId) {
        // 使用MyBatis-Plus的删除方法
        return remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TrainingLog>()
                .eq("task_id", taskId));
    }

}
