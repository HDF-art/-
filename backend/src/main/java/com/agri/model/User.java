package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 角色（1:一级管理员, 2:二级管理员, 3:普通用户）
     */
    private Integer role;

    /**
     * 所属农场ID（普通用户）
     */
    private Long farmId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    private String avatar;

    private Integer status;

    /**
     * 审核状态（0:待审核，1:审核通过，2:审核拒绝）- 仅二级管理员需要
     */
    private Integer auditStatus;

    /**
     * 单位名称（二级管理员）
     */
    private String organization;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}