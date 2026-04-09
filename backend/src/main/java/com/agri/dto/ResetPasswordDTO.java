package com.agri.dto;

import lombok.Data;

/**
 * 找回密码请求DTO
 */
@Data
public class ResetPasswordDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;

    /**
     * 新密码
     */
    private String newPassword;

}
