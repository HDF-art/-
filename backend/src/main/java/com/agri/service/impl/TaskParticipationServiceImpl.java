package com.agri.service.impl;

import com.agri.mapper.TaskParticipationMapper;
import com.agri.model.TaskParticipation;
import com.agri.service.TaskParticipationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务参与服务实现类
 */
@Service
public class TaskParticipationServiceImpl extends ServiceImpl<TaskParticipationMapper, TaskParticipation> implements TaskParticipationService {

    @Autowired
    private TaskParticipationMapper taskParticipationMapper;

    @Override
    public boolean createParticipation(TaskParticipation participation) {
        participation.setStatus("待确认");
        participation.setParticipateTime(LocalDateTime.now());
        return save(participation);
    }

    @Override
    public boolean updateParticipationStatus(Long id, String status) {
        TaskParticipation participation = getById(id);
        if (participation == null) {
            return false;
        }
        participation.setStatus(status);
        participation.setConfirmTime(LocalDateTime.now());
        return updateById(participation);
    }

    @Override
    public List<TaskParticipation> getByTaskId(Long taskId) {
        return taskParticipationMapper.getByTaskId(taskId);
    }

    @Override
    public TaskParticipation getByUserIdAndTaskId(Long userId, Long taskId) {
        return taskParticipationMapper.getByUserIdAndTaskId(userId, taskId);
    }

    @Override
    public Integer countByTaskIdAndStatus(Long taskId, String status) {
        return taskParticipationMapper.countByTaskIdAndStatus(taskId, status);
    }

    @Override
    public boolean batchCreateParticipations(List<TaskParticipation> participations) {
        for (TaskParticipation participation : participations) {
            participation.setStatus("待确认");
            participation.setParticipateTime(LocalDateTime.now());
        }
        return saveBatch(participations);
    }

}
