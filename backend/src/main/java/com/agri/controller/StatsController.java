package com.agri.controller;

import com.agri.model.RecognitionRecord;
import com.agri.model.TrainingTask;
import com.agri.model.User;
import com.agri.model.TaskParticipation;
import com.agri.service.RecognitionRecordService;
import com.agri.service.TrainingTaskService;
import com.agri.service.UserService;
import com.agri.service.TaskParticipationService;
import com.agri.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private UserService userService;

    @Autowired
    private TrainingTaskService trainingTaskService;

    @Autowired
    private RecognitionRecordService recognitionRecordService;

    @Autowired
    private TaskParticipationService taskParticipationService;

    @GetMapping("/dashboard")
    public ResponseUtils.ApiResponse<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            long userCount = userService.count();
            long taskCount = trainingTaskService.count();
            long successTaskCount = trainingTaskService.count(new QueryWrapper<TrainingTask>().eq("status", "COMPLETED"));
            long totalRecognition = recognitionRecordService.count();
            long todayRecognition = recognitionRecordService.count(new QueryWrapper<RecognitionRecord>().ge("created_at", LocalDate.now()));

            List<RecognitionRecord> allRecords = recognitionRecordService.list();
            double avgAccuracy = 0;
            if (!allRecords.isEmpty()) {
                double sum = allRecords.stream()
                    .filter(r -> r.getConfidence() != null)
                    .mapToDouble(r -> r.getConfidence().doubleValue())
                    .sum();
                long count = allRecords.stream().filter(r -> r.getConfidence() != null).count();
                if (count > 0) avgAccuracy = sum / count;
            }

            stats.put("userCount", userCount);
            stats.put("taskCount", taskCount);
            stats.put("successTaskCount", successTaskCount);
            stats.put("totalRecognition", totalRecognition);
            stats.put("todayRecognition", todayRecognition);
            stats.put("averageAccuracy", avgAccuracy);

            return ResponseUtils.success(stats);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取统计数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/task-success-count")
    public ResponseUtils.ApiResponse<Long> getTaskSuccessCount() {
        try {
            long count = trainingTaskService.count(new QueryWrapper<TrainingTask>().eq("status", "已完成"));
            return ResponseUtils.success(count);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取成功任务数失败: " + e.getMessage());
        }
    }

    @GetMapping("/average-accuracy")
    public ResponseUtils.ApiResponse<Map<String, Object>> getAverageAccuracy() {
        try {
            List<RecognitionRecord> allRecords = recognitionRecordService.list();
            double avgAccuracy = 0;
            if (!allRecords.isEmpty()) {
                double sum = allRecords.stream()
                    .filter(r -> r.getConfidence() != null)
                    .mapToDouble(r -> r.getConfidence().doubleValue())
                    .sum();
                long count = allRecords.stream().filter(r -> r.getConfidence() != null).count();
                if (count > 0) avgAccuracy = sum / count;
            }
            Map<String, Object> result = new HashMap<>();
            result.put("averageAccuracy", avgAccuracy);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取平均准确率失败: " + e.getMessage());
        }
    }

    @GetMapping("/recognition")
    public ResponseUtils.ApiResponse<Map<String, Object>> getRecognitionStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            long total = recognitionRecordService.count();
            long today = recognitionRecordService.count(new QueryWrapper<RecognitionRecord>().ge("created_at", LocalDate.now()));

            List<RecognitionRecord> allRecords = recognitionRecordService.list();
            double avgConfidence = 0;
            if (!allRecords.isEmpty()) {
                double sum = allRecords.stream()
                    .filter(r -> r.getConfidence() != null)
                    .mapToDouble(r -> r.getConfidence().doubleValue())
                    .sum();
                long count = allRecords.stream().filter(r -> r.getConfidence() != null).count();
                if (count > 0) avgConfidence = sum / count;
            }

            Map<String, Long> diseaseDistribution = allRecords.stream()
                .filter(r -> r.getResult() != null && !r.getResult().isEmpty())
                .collect(Collectors.groupingBy(RecognitionRecord::getResult, Collectors.counting()));

            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<RecognitionRecord> recentRecords = recognitionRecordService.list(
                new QueryWrapper<RecognitionRecord>().ge("created_at", sevenDaysAgo).orderByAsc("created_at")
            );

            Map<String, Long> dailyCount = new LinkedHashMap<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.getMonthValue() + "/" + date.getDayOfMonth();
                long count = date.getMonthValue() + "/" + date.getDayOfMonth() != null ?
                    recentRecords.stream()
                        .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().toLocalDate().equals(date))
                        .count() : 0;
                dailyCount.put(dateStr, count);
            }

            stats.put("totalCount", total);
            stats.put("todayCount", today);
            stats.put("averageConfidence", avgConfidence);
            stats.put("diseaseDistribution", diseaseDistribution);
            stats.put("dailyCount", dailyCount);

            return ResponseUtils.success(stats);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取识别统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/user-roles")
    public ResponseUtils.ApiResponse<Map<String, Object>> getUserRoleStats() {
        try {
            List<User> allUsers = userService.list();
            long admin1Count = allUsers.stream().filter(u -> u.getRole() != null && u.getRole() == 1).count();
            long admin2Count = allUsers.stream().filter(u -> u.getRole() != null && u.getRole() == 2).count();
            long userCount = allUsers.stream().filter(u -> u.getRole() == null || u.getRole() == 0).count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("admin1Count", admin1Count);
            stats.put("admin2Count", admin2Count);
            stats.put("userCount", userCount);
            stats.put("total", allUsers.size());

            return ResponseUtils.success(stats);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取用户角色统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/task-status")
    public ResponseUtils.ApiResponse<Map<String, Object>> getTaskStatusStats() {
        try {
            List<TrainingTask> allTasks = trainingTaskService.list();

            Map<String, Long> statusDistribution = allTasks.stream()
                .filter(t -> t.getStatus() != null)
                .collect(Collectors.groupingBy(TrainingTask::getStatus, Collectors.counting()));

            Map<String, Object> stats = new HashMap<>();
            stats.put("statusDistribution", statusDistribution);
            stats.put("total", allTasks.size());

            return ResponseUtils.success(stats);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取任务状态统计失败: " + e.getMessage());
        }
    }
}
