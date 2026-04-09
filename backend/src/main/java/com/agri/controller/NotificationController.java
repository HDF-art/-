package com.agri.controller;

import com.agri.dto.SendNotificationDTO;
import com.agri.model.Notification;
import com.agri.service.NotificationService;
import com.agri.service.impl.NotificationServiceImpl;
import com.agri.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationServiceImpl notificationServiceImpl;

    /**
     * 发送通知
     * @param notification 通知信息
     * @return 发送结果
     */
    @PostMapping
    public ResponseUtils.ApiResponse<Boolean> sendNotification(@RequestBody Notification notification) {
        try {
            boolean result = notificationService.sendNotification(notification);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 批量发送通知
     * @param notifications 通知列表
     * @return 发送结果
     */
    @PostMapping("/batch")
    public ResponseUtils.ApiResponse<Boolean> sendBatchNotifications(@RequestBody List<Notification> notifications) {
        try {
            boolean result = notificationService.sendBatchNotifications(notifications);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "批量发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送通知给指定类型的用户
     * @param dto 请求数据
     * @return 发送结果
     */
    @PostMapping("/send-to-users")
    public ResponseUtils.ApiResponse<Boolean> sendNotificationToUsers(@RequestBody SendNotificationDTO dto) {
        try {
            Notification notification = new Notification();
            notification.setTitle(dto.getTitle());
            notification.setContent(dto.getContent());
            notification.setSenderId(dto.getSenderId());

            boolean result = notificationServiceImpl.sendNotificationToUsers(notification, dto.getReceiverType(), dto.getFarmId(), dto.getSpecificUserIds());
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     * @param id 通知ID
     * @return 标记结果
     */
    @PutMapping("/{id}/read")
    public ResponseUtils.ApiResponse<Boolean> markAsRead(@PathVariable("id") Long id) {
        try {
            boolean result = notificationService.markAsRead(id);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "标记通知为已读失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的通知列表
     * @param userId 用户ID
     * @param status 状态（0:未读, 1:已读, null:所有）
     * @return 通知列表
     */
    @GetMapping("/user/{userId}")
    public ResponseUtils.ApiResponse<List<Notification>> getUserNotifications(@PathVariable("userId") Long userId, @RequestParam(required = false) Integer status) {
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId, status);
            return ResponseUtils.success(notifications);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseUtils.ApiResponse<Integer> getUnreadCount(@PathVariable("userId") Long userId) {
        try {
            Integer count = notificationService.getUnreadCount(userId);
            return ResponseUtils.success(count);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取未读通知数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取农场用户的通知列表（二级管理员使用）
     * @param farmId 农场ID
     * @param status 状态（0:未读, 1:已读, null:所有）
     * @return 通知列表
     */
    @GetMapping("/farm/{farmId}")
    public ResponseUtils.ApiResponse<List<Notification>> getFarmNotifications(@PathVariable("farmId") Long farmId, @RequestParam(required = false) Integer status) {
        try {
            List<Notification> notifications = notificationService.getFarmNotifications(farmId, status);
            return ResponseUtils.success(notifications);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取农场通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取通知
     * @param id 通知ID
     * @return 通知信息
     */
    @GetMapping("/{id}")
    public ResponseUtils.ApiResponse<Notification> getNotificationById(@PathVariable("id") Long id) {
        try {
            Notification notification = notificationService.getById(id);
            return ResponseUtils.success(notification);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取通知失败: " + e.getMessage());
        }
    }

    /**
     * 删除通知
     * @param id 通知ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseUtils.ApiResponse<Boolean> deleteNotification(@PathVariable("id") Long id) {
        try {
            boolean result = notificationService.removeById(id);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "删除通知失败: " + e.getMessage());
        }
    }

}
