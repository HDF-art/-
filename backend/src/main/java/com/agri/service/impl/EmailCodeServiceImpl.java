package com.agri.service.impl;

import com.agri.service.EmailCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailCodeServiceImpl implements EmailCodeService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final Map<String, String> codeMap = new HashMap<>();
    private static final Map<String, Long> expireMap = new HashMap<>();
    private static final Map<String, Long> sendTimeMap = new HashMap<>();
    
    private static final long EXPIRE_TIME = 5 * 60 * 1000; 
    private static final long SEND_INTERVAL = 60 * 1000; 

    @Override
    public boolean sendCode(String email) {
        try {
            if (sendTimeMap.containsKey(email)) {
                long lastSendTime = sendTimeMap.get(email);
                long now = System.currentTimeMillis();
                if (now - lastSendTime < SEND_INTERVAL) {
                    System.out.println("验证码发送过于频繁，请" + ((SEND_INTERVAL - (now - lastSendTime)) / 1000) + "秒后重试");
                    return false;
                }
            }
            
            String code = generateCode();
            codeMap.put(email, code);
            expireMap.put(email, System.currentTimeMillis() + EXPIRE_TIME);
            sendTimeMap.put(email, System.currentTimeMillis());

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
        if (!codeMap.containsKey(email)) {
            return false;
        }

        long expireTime = expireMap.get(email);
        if (System.currentTimeMillis() > expireTime) {
            codeMap.remove(email);
            expireMap.remove(email);
            sendTimeMap.remove(email);
            return false;
        }

        String storedCode = codeMap.get(email);
        if (storedCode.equals(code)) {
            codeMap.remove(email);
            expireMap.remove(email);
            sendTimeMap.remove(email);
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
        if (!expireMap.containsKey(email)) {
            return 0;
        }
        long remaining = (expireMap.get(email) - System.currentTimeMillis()) / 1000;
        return remaining > 0 ? remaining : 0;
    }
    
    public long getWaitSeconds(String email) {
        if (!sendTimeMap.containsKey(email)) {
            return 0;
        }
        long wait = (sendTimeMap.get(email) + SEND_INTERVAL - System.currentTimeMillis()) / 1000;
        return wait > 0 ? wait : 0;
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
