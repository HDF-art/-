package com.agri.service.impl;

import com.agri.service.SmsSenderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 短信发送服务实现
 * 支持多种短信平台
 */
@Service
public class SmsSenderServiceImpl implements SmsSenderService {

    // 短信平台类型: submail, twilio, aliyun
    @Value("${sms.provider:submail}")
    private String provider;

    @Value("${sms.submail.app_id:}")
    private String submailAppId;

    @Value("${sms.submail.app_key:}")
    private String submailAppKey;

    @Value("${sms.submail.signature:农业大数据平台}")
    private String submailSignature;

    @Value("${sms.twilio.account_sid:}")
    private String twilioAccountSid;

    @Value("${sms.twilio.auth_token:}")
    private String twilioAuthToken;

    @Value("${sms.twilio.phone_number:}")
    private String twilioPhoneNumber;

    @Value("${sms.aliyun.access_key_id:}")
    private String aliyunAccessKeyId;

    @Value("${sms.aliyun.access_key_secret:}")
    private String aliyunAccessKeySecret;

    @Value("${sms.aliyun.sign_name:}")
    private String aliyunSignName;

    @Value("${sms.aliyun.template_code:}")
    private String aliyunTemplateCode;

    // 存储验证码
    private static final Map<String, String> codeMap = new HashMap<>();
    private static final Map<String, Long> expireMap = new HashMap<>();
    private static final long EXPIRE_TIME = 5 * 60 * 1000;

    @Override
    public boolean sendVerifyCode(String phone) {
        try {
            String code = generateCode();
            codeMap.put(phone, code);
            expireMap.put(phone, System.currentTimeMillis() + EXPIRE_TIME);

            System.out.println("========== 短信发送 ==========");
            System.out.println("手机号: " + phone);
            System.out.println("验证码: " + code);
            System.out.println("平台: " + provider);
            System.out.println("==========================");

            // 根据平台发送
            boolean result = false;
            switch (provider.toLowerCase()) {
                case "submail":
                    result = sendViaSubmail(phone, code);
                    break;
                case "twilio":
                    result = sendViaTwilio(phone, code);
                    break;
                case "aliyun":
                    result = sendViaAliyun(phone, code);
                    break;
                default:
                    // 模拟模式
                    result = true;
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendViaSubmail(String phone, String code) {
        try {
            if (submailAppId == null || submailAppId.isEmpty()) {
                System.out.println("SUBMAIL未配置，使用模拟模式");
                return true;
            }
            
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.mysubmail.com/sms/send";

            Map<String, Object> params = new HashMap<>();
            params.put("appid", submailAppId);
            params.put("to", phone);
            params.put("signature", submailAppKey);
            params.put("content", submailSignature + "，您的验证码是：" + code + "，5分钟内有效。");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.out.println("SUBMAIL发送失败: " + e.getMessage());
            return false;
        }
    }

    private boolean sendViaTwilio(String phone, String code) {
        // Twilio实现需要额外SDK
        System.out.println("Twilio需要配置完整凭证");
        return false;
    }

    private boolean sendViaAliyun(String phone, String code) {
        // 阿里云实现
        System.out.println("阿里云短信需要配置完整凭证");
        return false;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        if (!codeMap.containsKey(phone)) {
            return false;
        }
        
        long expireTime = expireMap.get(phone);
        if (System.currentTimeMillis() > expireTime) {
            codeMap.remove(phone);
            expireMap.remove(phone);
            return false;
        }
        
        String storedCode = codeMap.get(phone);
        if (storedCode.equals(code)) {
            codeMap.remove(phone);
            expireMap.remove(phone);
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
