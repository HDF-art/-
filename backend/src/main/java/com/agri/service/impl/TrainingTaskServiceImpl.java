package com.agri.service.impl;

import com.agri.mapper.TrainingTaskMapper;
import com.agri.model.TaskParticipation;
import com.agri.model.TrainingTask;
import com.agri.service.TaskParticipationService;
import com.agri.service.TrainingTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainingTaskServiceImpl extends ServiceImpl<TrainingTaskMapper, TrainingTask> implements TrainingTaskService {

    @Autowired
    private TrainingTaskMapper trainingTaskMapper;

    @Autowired
    private TaskParticipationService taskParticipationService;

    @Override
    public boolean createTask(TrainingTask task) {
        task.setStatus("待开始");
        task.setCreatedAt(LocalDateTime.now());
        return save(task);
    }

    @Override
    public boolean updateTaskStatus(Long taskId, String status) {
        TrainingTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        task.setStatus(status);
        if ("进行中".equals(status)) {
            task.setStartTime(LocalDateTime.now());
        } else if ("已完成".equals(status) || "已失败".equals(status)) {
            task.setEndTime(LocalDateTime.now());
        }
        return updateById(task);
    }

    @Override
    public boolean startTask(Long taskId) {
        TrainingTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        task.setStatus("进行中");
        task.setStartTime(LocalDateTime.now());
        return updateById(task);
    }

    @Override
    public boolean stopTask(Long taskId) {
        TrainingTask task = getById(taskId);
        if (task != null && "进行中".equals(task.getStatus())) {
            return updateTaskStatus(taskId, "已失败");
        }
        return false;
    }

    @Override
    public boolean auditTask(Long taskId, String auditStatus, String auditComment, Long auditedBy) {
        TrainingTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        return updateById(task);
    }

    @Override
    public boolean publishTask(Long taskId, List<Long> userIds) {
        TrainingTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        List<TaskParticipation> participations = new ArrayList<>();
        for (Long userId : userIds) {
            TaskParticipation participation = new TaskParticipation();
            participation.setTaskId(taskId);
            participation.setUserId(userId);
            participations.add(participation);
        }
        return taskParticipationService.batchCreateParticipations(participations);
    }

    @Override
    public List<TrainingTask> getTasksByAuditStatus(String auditStatus) {
        return new ArrayList<>();
    }

    @Override
    public List<TrainingTask> getTasksByCreatedBy(Long createdBy) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TrainingTask>().eq("created_by", createdBy));
    }

    @Override
    public List<TrainingTask> getParticipatedTasks(Long userId) {
        return new ArrayList<>();
    }
}
