package com.agri.controller;

import com.agri.model.TaskParticipation;
import com.agri.service.TaskParticipationService;
import com.agri.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 任务参与控制器
 */
@RestController
@RequestMapping("/task-participations")
public class TaskParticipationController {

    @Autowired
    private TaskParticipationService taskParticipationService;

    @GetMapping("/user/{userId}")
    public ResponseUtils.ApiResponse<List<TaskParticipation>> getByUserId(@PathVariable("userId") Long userId) {
        try {
            List<TaskParticipation> list = taskParticipationService.list(
                new QueryWrapper<TaskParticipation>().eq("user_id", userId).orderByDesc("participate_time")
            );
            return ResponseUtils.success(list);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取参与记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据任务ID获取参与列表
     * @param taskId 任务ID
     * @return 参与列表
     */
    @GetMapping("/task/{taskId}")
    public List<TaskParticipation> getByTaskId(@PathVariable("taskId") Long taskId) {
        return taskParticipationService.getByTaskId(taskId);
    }

    /**
     * 根据用户ID和任务ID获取参与信息
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 参与信息
     */
    @GetMapping("/user/{userId}/task/{taskId}")
    public TaskParticipation getByUserIdAndTaskId(@PathVariable("userId") Long userId, @PathVariable("taskId") Long taskId) {
        return taskParticipationService.getByUserIdAndTaskId(userId, taskId);
    }

    /**
     * 创建任务参与记录
     * @param participation 参与信息
     * @return 创建结果
     */
    @PostMapping
    public boolean createParticipation(@RequestBody TaskParticipation participation) {
        return taskParticipationService.createParticipation(participation);
    }

    /**
     * 更新参与状态
     * @param id 参与ID
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public boolean updateParticipationStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        return taskParticipationService.updateParticipationStatus(id, status);
    }

    /**
     * 根据任务ID和状态获取参与数量
     * @param taskId 任务ID
     * @param status 状态
     * @return 参与数量
     */
    @GetMapping("/count/task/{taskId}/status/{status}")
    public Integer countByTaskIdAndStatus(@PathVariable("taskId") Long taskId, @PathVariable("status") String status) {
        return taskParticipationService.countByTaskIdAndStatus(taskId, status);
    }

}
