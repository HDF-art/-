package com.agri.service.impl;

import com.agri.service.SmsService;
import com.agri.service.SmsSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private SmsSenderService smsSenderService;

    // 存储验证码
    private static final Map<String, String> codeMap = new HashMap<>();
    private static final Map<String, Long> expireMap = new HashMap<>();
    private static final long EXPIRE_TIME = TimeUnit.MINUTES.toMillis(5);

    @Override
    public boolean sendCode(String phone, String email) {
        try {
            // 使用短信服务发送验证码
            return smsSenderService.sendVerifyCode(phone);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        // 先检查内存中的验证码
        if (codeMap.containsKey(phone)) {
            long expireTime = expireMap.get(phone);
            if (System.currentTimeMillis() > expireTime) {
                codeMap.remove(phone);
                expireMap.remove(phone);
            } else {
                String storedCode = codeMap.get(phone);
                if (storedCode.equals(code)) {
                    codeMap.remove(phone);
                    expireMap.remove(phone);
                    return true;
                }
            }
        }
        
        // 调用短信服务验证
        return smsSenderService.verifyCode(phone, code);
    }

    /**
     * 生成6位随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
