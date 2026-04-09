package com.agri.service;

import java.util.Map;

/**
 * 短信发送服务接口
 */
public interface SmsSenderService {
    
    /**
     * 发送短信验证码
     * @param phone 手机号
     * @return 发送结果
     */
    boolean sendVerifyCode(String phone);
    
    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifyCode(String phone, String code);
}
