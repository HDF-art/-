package com.agri.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponseDTO {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 过期时间
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfoDTO userInfo;

}