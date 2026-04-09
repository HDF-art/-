package com.agri.dto;

import lombok.Data;

/**
 * 手机验证码登录请求DTO
 */
@Data
public class PhoneLoginDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;

}
