package com.agri.dto;

import lombok.Data;

/**
 * 发送验证码请求DTO
 */
@Data
public class SendCodeDTO {

    /**
     * 手机号
     */
    private String phone;

}
