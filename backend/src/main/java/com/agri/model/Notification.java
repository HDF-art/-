package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID（如果为null则表示发送给所有用户）
     */
    private Long receiverId;

    /**
     * 接收者类型（1:所有用户, 2:一级管理员, 3:二级管理员, 4:普通用户, 5:特定用户）
     */
    private Integer receiverType;

    /**
     * 关联农场ID（二级管理员发送通知时使用）
     */
    private Long farmId;

    /**
     * 状态（0:未读, 1:已读）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 读取时间
     */
    private LocalDateTime readAt;

}
