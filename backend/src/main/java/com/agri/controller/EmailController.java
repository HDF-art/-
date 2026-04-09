package com.agri.controller;

import com.agri.service.UserService;
import com.agri.utils.JwtUtils;
import com.agri.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 邮箱控制器 - 邮箱验证码登录
 */
@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final Map<String, String> codeMap = new HashMap<>();
    private static final Map<String, Long> expireMap = new HashMap<>();
    private static final long EXPIRE_TIME = 5 * 60 * 1000;

    @PostMapping("/send-code")
    public Map<String, Object> sendCode(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String email = params.get("email");

        try {
            String code = generateCode();
            codeMap.put(email, code);
            expireMap.put(email, System.currentTimeMillis() + EXPIRE_TIME);

            // 发送真实邮件
            sendEmail(email, code);

            result.put("code", 200);
            result.put("message", "验证码已发送到邮箱");

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "发送失败: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/email-login")
    public Map<String, Object> emailLogin(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String email = params.get("email");
        String code = params.get("code");

        if (!verifyCode(email, code)) {
            result.put("code", 400);
            result.put("message", "验证码错误或已过期");
            return result;
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            result.put("code", 400);
            result.put("message", "用户不存在");
            return result;
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole().toString());

        result.put("code", 200);
        result.put("message", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", user);
        result.put("data", data);

        return result;
    }

    private void sendEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("【农业大数据联合建模平台】验证码");
        message.setText("尊敬的用户您好！\n\n您的验证码是：" + code + "\n\n有效期5分钟，请及时完成验证。\n\n-- 农业大数据联合建模平台");
        javaMailSender.send(message);
    }

    private boolean verifyCode(String email, String code) {
        if (!codeMap.containsKey(email)) return false;
        
        long expireTime = expireMap.get(email);
        if (System.currentTimeMillis() > expireTime) {
            codeMap.remove(email);
            expireMap.remove(email);
            return false;
        }

        String storedCode = codeMap.get(email);
        if (storedCode.equals(code)) {
            codeMap.remove(email);
            expireMap.remove(email);
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
}
