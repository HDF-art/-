package com.agri.controller;

import com.agri.service.CacheService;
import com.agri.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIProxyController {
    
    @Value("${deepseek.api-key:}")
    private String deepseekApiKey;
    
    @Value("${deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @PostMapping("/analyze")
    public ResponseUtils.ApiResponse<Map<String, Object>> analyzeDisease(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
                return ResponseUtils.error(503, "AI服务暂未配置");
            }

            // 1. 频率限制 (Rate Limiting)
            Long userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                userId = jwtUtils.getUserIdFromToken(token);
            }

            if (userId != null) {
                String limitKey = "rate_limit:ai:analyze:" + userId;
                Object countObj = cacheService.get(limitKey);
                int count = countObj != null ? (int) countObj : 0;
                
                if (count >= 5) { // 每分钟5次
                    return ResponseUtils.error(429, "请求过于频繁，请稍后再试（每分钟限5次）");
                }
                cacheService.set(limitKey, count + 1, 1, TimeUnit.MINUTES);
            }

            // 2. 内容安全过滤和长度限制
            Object messagesObj = request.get("messages");
            if (messagesObj instanceof List) {
                List<Map<String, String>> messages = (List<Map<String, String>>) messagesObj;
                long totalLength = 0;
                for (Map<String, String> msg : messages) {
                    String content = msg.get("content");
                    if (content != null) {
                        totalLength += content.length();
                    }
                }
                if (totalLength > 8000) { // 限制 8000 字符
                    return ResponseUtils.error(400, "输入内容过长，请精简后再试");
                }
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("messages", messagesObj);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                deepseekApiUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ResponseUtils.success(response.getBody());
            } else {
                return ResponseUtils.error(500, "AI服务调用失败");
            }
            
        } catch (Exception e) {
            return ResponseUtils.error(500, "AI分析服务暂时不可用");
        }
    }
}
