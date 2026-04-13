package com.agri.service.impl;

import com.agri.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class EmailCodeServiceImpl implements EmailCodeService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CacheService cacheService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String CODE_PREFIX = "email:code:";
    private static final String SEND_TIME_PREFIX = "email:send_time:";
    
    private static final long EXPIRE_TIME_MINUTES = 5; 
    private static final long SEND_INTERVAL_SECONDS = 60; 

    @Override
    public boolean sendCode(String email) {
        try {
            // 防刷检查
            String sendTimeKey = SEND_TIME_PREFIX + email;
            if (cacheService.exists(sendTimeKey)) {
                System.out.println("验证码发送过于频繁，请稍后再试");
                return false;
            }
            
            String code = generateCode();
            String codeKey = CODE_PREFIX + email;
            
            // 存入 Redis 并设置过期时间
            cacheService.set(codeKey, code, EXPIRE_TIME_MINUTES, TimeUnit.MINUTES);
            cacheService.set(sendTimeKey, String.valueOf(System.currentTimeMillis()), SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

            sendEmail(email, code);

            System.out.println("========== 注册验证码 ==========");
            System.out.println("邮箱: " + email);
            System.out.println("验证码: " + code);
            System.out.println("有效期: 5分钟");
            System.out.println("==============================");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendEmail(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("【农业大数据联合建模平台】注册验证码");
            message.setText("尊敬的用户您好！\n\n您的注册验证码是：" + code + "\n\n验证码有效期5分钟，请及时完成注册。\n\n如非本人操作，请忽略此邮件。\n\n-- 农业大数据联合建模平台");
            mailSender.send(message);
            System.out.println("邮件发送成功: " + toEmail);
        } catch (Exception e) {
            System.err.println("邮件发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String codeKey = CODE_PREFIX + email;
        Object storedCode = cacheService.get(codeKey);
        
        if (storedCode == null) {
            return false;
        }

        if (storedCode.toString().equals(code)) {
            cacheService.delete(codeKey);
            // 验证通过不强制清除发送时间限制，防止短时间内大量重复验证
            return true;
        }

        return false;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    public long getExpireSeconds(String email) {
        // 由于 CacheService 目前没有暴露取 TTL 的方法，这里简化返回一个标志位
        // 或者直接根据是否存在来判断
        return cacheService.exists(CODE_PREFIX + email) ? EXPIRE_TIME_MINUTES * 60 : 0;
    }
    
    public long getWaitSeconds(String email) {
        return cacheService.exists(SEND_TIME_PREFIX + email) ? SEND_INTERVAL_SECONDS : 0;
    }
    
    @Override
    public boolean sendAuditPassed(String email, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【农业大数据联合建模平台】二级管理员注册审核通过");
            message.setText("尊敬的 " + username + " 您好！\n\n" +
                "恭喜您！您申请的二级管理员账号已审核通过。\n\n" +
                "您现在可以使用注册的用户名和密码登录平台。\n\n" +
                "登录地址：https://admp.online\n\n" +
                "如有任何问题，请联系平台管理员。\n\n" +
                "-- 农业大数据联合建模平台");
            mailSender.send(message);
            System.out.println("审核通过邮件发送成功: " + email);
            return true;
        } catch (Exception e) {
            System.err.println("审核通过邮件发送失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean sendAuditRejected(String email, String username, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【农业大数据联合建模平台】二级管理员注册审核结果通知");
            message.setText("尊敬的 " + username + " 您好！\n\n" +
                "很遗憾地通知您，您申请的二级管理员账号未能通过审核。\n\n" +
                "拒绝原因：" + (reason != null ? reason : "无") + "\n\n" +
                "如有疑问，请联系平台管理员。\n\n" +
                "-- 农业大数据联合建模平台");
            mailSender.send(message);
            System.out.println("审核拒绝邮件发送成功: " + email);
            return true;
        } catch (Exception e) {
            System.err.println("审核拒绝邮件发送失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
