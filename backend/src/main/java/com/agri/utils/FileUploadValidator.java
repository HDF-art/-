package com.agri.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class FileUploadValidator {
    
    // 允许的文件类型
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"
    ));
    
    private static final Set<String> ALLOWED_DATA_TYPES = new HashSet<>(Arrays.asList(
        "text/csv", "application/csv", "text/plain", "application/json",
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ));
    
    private static final Set<String> ALLOWED_MODEL_TYPES = new HashSet<>(Arrays.asList(
        "application/zip", "application/x-zip-compressed",
        "application/octet-stream", "model/onnx", "model/tensorflow"
    ));
    
    // 文件大小限制 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // 危险文件扩展名
    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
        "exe", "sh", "bat", "cmd", "ps1", "vbs", "js", "jsp", "asp", "php",
        "phtml", "shtml", "asa", "cer", "htaccess", "htpasswd", "cgi", "pl"
    ));
    
    /**
     * 验证图片上传
     */
    public String validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件不能为空";
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            return "文件大小不能超过10MB";
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return "只允许上传 JPG, PNG, GIF, BMP, WebP 格式的图片";
        }
        
        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (DANGEROUS_EXTENSIONS.contains(ext)) {
                return "不允许上传此类文件";
            }
        }
        
        // 验证文件内容 (检查文件头)
        if (!validateImageContent(file)) {
            return "文件内容无效或可能存在安全问题";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证数据文件上传
     */
    public String validateDataFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件不能为空";
        }
        
        if (file.getSize() > MAX_FILE_SIZE * 5) { // 数据文件允许50MB
            return "文件大小不能超过50MB";
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_DATA_TYPES.contains(contentType.toLowerCase())) {
            return "只允许上传 CSV, TXT, JSON, Excel 格式的数据文件";
        }
        
        // 检查扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (DANGEROUS_EXTENSIONS.contains(ext)) {
                return "不允许上传此类文件";
            }
        }
        
        return null;
    }
    
    /**
     * 验证模型文件上传
     */
    public String validateModelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件不能为空";
        }
        
        if (file.getSize() > MAX_FILE_SIZE * 100) { // 模型文件允许1GB
            return "文件大小不能超过1GB";
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MODEL_TYPES.contains(contentType.toLowerCase())) {
            return "只允许上传 ZIP, ONNX, TensorFlow 模型文件";
        }
        
        // 检查扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (DANGEROUS_EXTENSIONS.contains(ext)) {
                return "不允许上传此类文件";
            }
        }
        
        return null;
    }
    
    /**
     * 验证图片内容 (检查文件头魔数)
     */
    private boolean validateImageContent(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 4) return false;
            
            // JPEG: FF D8 FF
            if (bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8 && bytes[2] == (byte)0xFF) {
                return true;
            }
            // PNG: 89 50 4E 47
            if (bytes[0] == (byte)0x89 && bytes[1] == (byte)0x50 && bytes[2] == (byte)0x4E && bytes[3] == (byte)0x47) {
                return true;
            }
            // GIF: 47 49 46 38
            if (bytes[0] == (byte)0x47 && bytes[1] == (byte)0x49 && bytes[2] == (byte)0x46 && bytes[3] == (byte)0x38) {
                return true;
            }
            // BMP: 42 4D
            if (bytes[0] == (byte)0x42 && bytes[1] == (byte)0x4D) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 清理文件名 (防止路径遍历攻击)
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unknown";
        }
        
        // 移除路径分隔符
        filename = filename.replaceAll("[/\\\\]", "_");
        
        // 移除危险字符
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // 限制长度
        if (filename.length() > 200) {
            filename = filename.substring(0, 200);
        }
        
        return filename;
    }
}
