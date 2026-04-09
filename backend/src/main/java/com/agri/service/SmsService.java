package com.agri.service;

/**
 * 验证码服务
 */
public interface SmsService {

    /**
     * 发送验证码到邮箱
     * @param phone 手机号
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    boolean sendCode(String phone, String email);

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String phone, String code);

}
