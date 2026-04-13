package com.agri.dto;

import com.agri.model.Notification;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知数据传输对象，包含发件人名称
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationDTO extends Notification {
    /**
     * 发件人名称
     */
    private String senderName;
}
