package com.agri.service.impl;

import com.agri.dto.NotificationDTO;
import com.agri.mapper.NotificationMapper;
import com.agri.model.Notification;
import com.agri.model.User;
import com.agri.service.NotificationService;
import com.agri.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知服务实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserService userService;

    @Override
    public boolean sendNotification(Notification notification) {
        notification.setStatus(0); // 未读
        notification.setCreatedAt(LocalDateTime.now());
        return save(notification);
    }

    @Override
    public boolean sendBatchNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            notification.setStatus(0); // 未读
            notification.setCreatedAt(LocalDateTime.now());
        }
        return saveBatch(notifications);
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        UpdateWrapper<Notification> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("receiver_id", userId).eq("status", 0);
        updateWrapper.set("status", 1).set("read_at", LocalDateTime.now());
        return update(updateWrapper);
    }

    @Override
    public IPage<NotificationDTO> getUserNotifications(Page<NotificationDTO> page, Long userId, Integer status) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("n.receiver_id", userId);
        if (status != null) {
            queryWrapper.eq("n.status", status);
        }
        queryWrapper.orderByDesc("n.created_at");
        return notificationMapper.selectUserNotificationsPage(page, queryWrapper);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification>();
        queryWrapper.eq("receiver_id", userId);
        queryWrapper.eq("status", 0);
        return (int) count(queryWrapper);
    }

    @Override
    public List<Notification> getFarmNotifications(Long farmId, Integer status) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification>();
        queryWrapper.eq("farm_id", farmId);
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_at");
        return list(queryWrapper);
    }

    /**
     * 发送通知给指定类型的用户
     * @param notification 通知信息
     * @param receiverType 接收者类型
     * @param farmId 农场ID（二级管理员使用）
     * @param specificUserIds 特定用户ID列表
     * @return 发送结果
     */
    public boolean sendNotificationToUsers(Notification notification, Integer receiverType, Long farmId, List<Long> specificUserIds) {
        List<Notification> notifications = new ArrayList<>();
        
        switch (receiverType) {
            case 1: // 所有用户
                List<User> allUsers = userService.list();
                for (User user : allUsers) {
                    Notification n = new Notification();
                    n.setTitle(notification.getTitle());
                    n.setContent(notification.getContent());
                    n.setSenderId(notification.getSenderId());
                    n.setReceiverId(user.getId());
                    n.setReceiverType(receiverType);
                    n.setFarmId(farmId);
                    n.setStatus(0);
                    n.setCreatedAt(LocalDateTime.now());
                    notifications.add(n);
                }
                break;
            case 2: // 一级管理员
                List<User> admin1Users = userService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("role", 1));
                for (User user : admin1Users) {
                    Notification n = new Notification();
                    n.setTitle(notification.getTitle());
                    n.setContent(notification.getContent());
                    n.setSenderId(notification.getSenderId());
                    n.setReceiverId(user.getId());
                    n.setReceiverType(receiverType);
                    n.setFarmId(farmId);
                    n.setStatus(0);
                    n.setCreatedAt(LocalDateTime.now());
                    notifications.add(n);
                }
                break;
            case 3: // 二级管理员
                List<User> admin2Users = userService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("role", 2));
                for (User user : admin2Users) {
                    Notification n = new Notification();
                    n.setTitle(notification.getTitle());
                    n.setContent(notification.getContent());
                    n.setSenderId(notification.getSenderId());
                    n.setReceiverId(user.getId());
                    n.setReceiverType(receiverType);
                    n.setFarmId(farmId);
                    n.setStatus(0);
                    n.setCreatedAt(LocalDateTime.now());
                    notifications.add(n);
                }
                break;
            case 4: // 普通用户
                List<User> normalUsers;
                if (farmId != null) {
                    // 二级管理员只能发送给本农场的普通用户
                    normalUsers = userService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                            .eq("role", 3)
                            .eq("farm_id", farmId));
                } else {
                    // 一级管理员可以发送给所有普通用户
                    normalUsers = userService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("role", 3));
                }
                for (User user : normalUsers) {
                    Notification n = new Notification();
                    n.setTitle(notification.getTitle());
                    n.setContent(notification.getContent());
                    n.setSenderId(notification.getSenderId());
                    n.setReceiverId(user.getId());
                    n.setReceiverType(receiverType);
                    n.setFarmId(farmId);
                    n.setStatus(0);
                    n.setCreatedAt(LocalDateTime.now());
                    notifications.add(n);
                }
                break;
            case 5: // 特定用户
                if (specificUserIds != null && !specificUserIds.isEmpty()) {
                    for (Long userId : specificUserIds) {
                        Notification n = new Notification();
                        n.setTitle(notification.getTitle());
                        n.setContent(notification.getContent());
                        n.setSenderId(notification.getSenderId());
                        n.setReceiverId(userId);
                        n.setReceiverType(receiverType);
                        n.setFarmId(farmId);
                        n.setStatus(0);
                        n.setCreatedAt(LocalDateTime.now());
                        notifications.add(n);
                    }
                }
                break;
        }
        
        if (!notifications.isEmpty()) {
            // 每 500 条进行一次批量保存，防止一次性保存过大数据导致数据库或内存问题
            return saveBatch(notifications, 500);
        }
        return false;
    }

}
