package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核请求实体类
 */
@Data
@TableName("audit_request")
public class AuditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请类型（1:二级管理员个人信息修改, 2:二级管理员账号注销, 3:普通用户加入农场）
     */
    private Integer type;

    /**
     * 审核状态（0:待审核, 1:已通过, 2:已拒绝）
     */
    private Integer status;

    /**
     * 相关农场ID（普通用户加入农场时使用）
     */
    private Long farmId;

    /**
     * 申请内容（JSON格式）
     */
    private String content;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 审核时间
     */
    private LocalDateTime auditedAt;

}
