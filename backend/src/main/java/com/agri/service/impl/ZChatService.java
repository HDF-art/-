package com.agri.service.impl;

import org.springframework.stereotype.Service;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ZChatService {
    
    // 使用正确的Z.ai API配置
    private static final String API_KEY = "84b0c10a19954030994339f9101197f5.T8hZJQvRlisAOpmE";
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String MODEL = "glm-5.1";
    
    /**
     * 发送聊天请求到Z.ai
     */
    public String chat(String message) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);
            requestBody.put("messages", messages);
            
            // 发送请求
            OutputStream os = conn.getOutputStream();
            String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            os.close();
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            System.out.println("Z.ai API响应码: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                // 解析JSON响应
                return parseResponse(response.toString());
            } else {
                // 读取错误信息
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                br.close();
                return "API请求失败，错误码: " + responseCode + ", 详情: " + errorResponse.toString();
            }
            
        } catch (Exception e) {
            return "API调用错误: " + e.getMessage();
        }
    }
    
    /**
     * 解析Z.ai响应
     */
    private String parseResponse(String json) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            
            if (root.has("choices")) {
                var choices = root.get("choices");
                if (choices.isArray() && choices.size() > 0) {
                    var message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }
            
            if (root.has("error")) {
                return "API错误: " + root.get("error").asText();
            }
            
            return "响应: " + json.substring(0, Math.min(500, json.length()));
            
        } catch (Exception e) {
            return "解析错误: " + e.getMessage();
        }
    }
}
