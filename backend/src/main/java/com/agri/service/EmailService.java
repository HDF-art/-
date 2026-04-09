package com.agri.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String FROM_EMAIL = "noreply@admp.online";

    /**
     * 发送验证码邮件 - 美化版HTML
     */
    @Async
    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject("【安徽农业大学】农业大数据联合建模平台 - 验证码");
            
            String htmlContent = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background-color:#f5f7fa;font-family:Microsoft YaHei,Arial,sans-serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f5f7fa;padding:40px 0;'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:12px;box-shadow:0 4px 20px rgba(0,0,0,0.08);'>" +
                "<tr><td style='background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);padding:30px;border-radius:12px 12px 0 0;text-align:center;'>" +
                "<h1 style='color:#ffffff;margin:0;font-size:24px;font-weight:600;'>🌾 农业大数据联合建模平台</h1>" +
                "<p style='color:rgba(255,255,255,0.9);margin:10px 0 0 0;font-size:14px;'>Agricultural Big Data Joint Modeling Platform</p>" +
                "</td></tr>" +
                "<tr><td style='padding:40px 30px;'>" +
                "<h2 style='color:#333333;margin:0 0 20px 0;font-size:20px;font-weight:600;'>您好！</h2>" +
                "<p style='color:#666666;margin:0 0 20px 0;font-size:14px;line-height:1.8;'>您正在进行身份验证，您的验证码如下：</p>" +
                "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center' style='padding:25px;background:linear-gradient(135deg,#f5f7fa 0%,#e8ecf1 100%);border-radius:10px;border:2px dashed #667eea;'>" +
                "<span style='font-size:36px;font-weight:bold;color:#667eea;letter-spacing:8px;'>" + code + "</span>" +
                "</td></tr></table>" +
                "<p style='color:#999999;margin:25px 0 0 0;font-size:13px;line-height:1.8;'>⏱️ 验证码有效期为 <strong>5分钟</strong>，请尽快完成验证<br>🔒 为保护您的账户安全，请勿将验证码泄露给他人</p>" +
                "</td></tr>" +
                "<tr><td style='background-color:#f8f9fa;padding:25px 30px;border-radius:0 0 12px 12px;text-align:center;border-top:1px solid #eeeeee;'>" +
                "<p style='color:#999999;margin:0 0 10px 0;font-size:12px;line-height:1.6;'>© 安徽农业大学 | 地址：安徽省合肥市长江西路130号 | 邮编：230036</p>" +
                "<p style='color:#cccccc;margin:0;font-size:11px;'>本邮件由系统自动发送，请勿回复</p>" +
                "</td></tr>" +
                "</table></td></tr></table></body></html>";
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("验证码已发送到: " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.err.println("发送失败: " + e.getMessage());
            return sendSimpleEmail(toEmail, "【安徽农业大学】验证码", "您的验证码是：" + code + "，有效期5分钟。");
        } catch (Exception e) {
            System.err.println("发送验证码失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 降级发送纯文本邮件
     */
    private boolean sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 发送欢迎邮件
     */
    @Async
    public boolean sendWelcomeEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject("【安徽农业大学】欢迎注册农业大数据联合建模平台");
            
            String htmlContent = "<h2 style='color:#667eea;'>🌾 欢迎 " + username + "</h2><p>感谢注册农业大数据联合建模平台！</p><p>安徽农业大学</p>";
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("发送欢迎邮件失败: " + e.getMessage());
            return false;
        }
    }
}
