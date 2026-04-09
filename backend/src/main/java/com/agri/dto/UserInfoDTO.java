package com.agri.dto;

import lombok.Data;

/**
 * 用户信息DTO
 */
@Data
public class UserInfoDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色（1:一级管理员, 2:二级管理员, 3:普通用户）
     */
    private Integer role;

    /**
     * 所属农场ID
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

    private String roleName;

    /**
     * 农场名称（如果有）
     */
    private String farmName;

}