package com.agri.service;

import com.agri.model.Notification;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 发送通知
     * @param notification 通知信息
     * @return 发送结果
     */
    boolean sendNotification(Notification notification);

    /**
     * 批量发送通知
     * @param notifications 通知列表
     * @return 发送结果
     */
    boolean sendBatchNotifications(List<Notification> notifications);

    /**
     * 标记通知为已读
     * @param id 通知ID
     * @return 标记结果
     */
    boolean markAsRead(Long id);

    /**
     * 获取用户的通知列表
     * @param userId 用户ID
     * @param status 状态（0:未读, 1:已读, null:所有）
     * @return 通知列表
     */
    List<Notification> getUserNotifications(Long userId, Integer status);

    /**
     * 获取用户的未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 获取农场用户的通知列表（二级管理员使用）
     * @param farmId 农场ID
     * @param status 状态（0:未读, 1:已读, null:所有）
     * @return 通知列表
     */
    List<Notification> getFarmNotifications(Long farmId, Integer status);

}
