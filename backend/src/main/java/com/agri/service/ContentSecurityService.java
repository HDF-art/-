package com.agri.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 内容安全检查服务
 */
@Service
public class ContentSecurityService {

    // 允许的图片类型
    private static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    // 允许的最大文件大小 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // 危险文件特征（文件头魔术字节）
    private static final Map<String, String> DANGEROUS_SIGNATURES = Map.of(
        "FFD8FF", "jpg",
        "89504E47", "png",
        "47494638", "gif",
        "424D", "bmp"
    );

    /**
     * 检查图片是否安全
     */
    public Map<String, Object> checkImage(MultipartFile file) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            result.put("safe", false);
            result.put("message", "文件不能为空");
            return result;
        }
        
        // 2. 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            result.put("safe", false);
            result.put("message", "文件大小不能超过10MB");
            return result;
        }
        
        // 3. 检查文件扩展名
        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);
        if (!isAllowedExtension(extension)) {
            result.put("safe", false);
            result.put("message", "不允许的文件类型: " + extension);
            return result;
        }
        
        // 4. 检查文件内容（魔术字节）
        try {
            byte[] bytes = file.getBytes();
            String magicBytes = bytesToHex(bytes);
            if (!isValidImageSignature(magicBytes)) {
                result.put("safe", false);
                result.put("message", "文件内容与扩展名不匹配，可能存在安全隐患");
                return result;
            }
        } catch (Exception e) {
            result.put("safe", false);
            result.put("message", "文件读取失败");
            return result;
        }
        
        // 5. 检查文件名是否包含危险字符
        if (containsDangerousChars(filename)) {
            result.put("safe", false);
            result.put("message", "文件名包含危险字符");
            return result;
        }
        
        result.put("safe", true);
        result.put("message", "检查通过");
        return result;
    }

    /**
     * 检查文本内容是否包含敏感信息
     */
    public Map<String, Object> checkTextContent(String content) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        if (content == null || content.trim().isEmpty()) {
            result.put("safe", true);
            result.put("message", "内容为空");
            return result;
        }
        
        // 检查长度
        if (content.length() > 10000) {
            result.put("safe", false);
            result.put("message", "内容过长");
            return result;
        }
        
        // 检查SQL注入特征
        if (containsSQLInjection(content)) {
            result.put("safe", false);
            result.put("message", "内容包含非法SQL语句");
            return result;
        }
        
        // 检查XSS特征
        if (containsXSS(content)) {
            result.put("safe", false);
            result.put("message", "内容包含潜在恶意脚本");
            return result;
        }
        
        result.put("safe", true);
        result.put("message", "检查通过");
        return result;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_IMAGE_TYPES) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidImageSignature(String hex) {
        for (String sig : DANGEROUS_SIGNATURES.keySet()) {
            if (hex.startsWith(sig)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsDangerousChars(String filename) {
        String dangerous = "<>:\"|?*\\/\0";
        for (char c : filename.toCharArray()) {
            if (dangerous.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSQLInjection(String content) {
        String lower = content.toLowerCase();
        String[] sqlKeywords = {"select ", "insert ", "update ", "delete ", "drop ", "union ", "create ", "alter ", "exec ", "execute "};
        for (String keyword : sqlKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsXSS(String content) {
        String lower = content.toLowerCase();
        String[] xssPatterns = {"<script", "javascript:", "onerror=", "onload=", "eval(", "expression("};
        for (String pattern : xssPatterns) {
            if (lower.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(bytes.length, 10); i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }
}
