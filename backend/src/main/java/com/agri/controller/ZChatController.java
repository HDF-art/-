package com.agri.controller;

import com.agri.service.impl.ZChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class ZChatController {
    
    @Autowired
    private ZChatService zChatService;
    
    @PostMapping("/chat/zchat")
    public Map<String, Object> chat(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        
        String message = params.get("message");
        if (message == null || message.isEmpty()) {
            result.put("success", false);
            result.put("message", "消息不能为空");
            return result;
        }
        
        try {
            String response = zChatService.chat(message);
            result.put("success", true);
            result.put("message", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "错误: " + e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/chat/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "ZChat服务正常，模型: glm-5.1");
        return result;
    }
}
