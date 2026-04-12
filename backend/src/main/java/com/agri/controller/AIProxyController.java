package com.agri.controller;

import com.agri.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIProxyController {
    
    @Value("${deepseek.api-key:}")
    private String deepseekApiKey;
    
    @Value("${deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @PostMapping("/analyze")
    public ResponseUtils.ApiResponse<Map<String, Object>> analyzeDisease(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
                return ResponseUtils.error(503, "AI服务暂未配置");
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("messages", request.get("messages"));
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
