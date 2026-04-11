package com.agri.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Component
public class FileUploadValidator {
    
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final long MAX_DATA_SIZE = 50 * 1024 * 1024;
    private static final long MAX_MODEL_SIZE = 1024 * 1024 * 1024;
    
    private static final Map<String, String> ALLOWED_IMAGE_EXTENSIONS = Map.of(
        "jpg", "image/jpeg",
        "jpeg", "image/jpeg",
        "png", "image/png",
        "gif", "image/gif",
        "webp", "image/webp"
    );
    
    private static final Map<String, String> ALLOWED_DATA_EXTENSIONS = Map.of(
        "csv", "text/csv",
        "json", "application/json",
        "txt", "text/plain",
        "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "xls", "application/vnd.ms-excel"
    );
    
    private static final Map<String, String> ALLOWED_MODEL_EXTENSIONS = Map.of(
        "pt", "application/octet-stream",
        "pth", "application/octet-stream",
        "onnx", "application/octet-stream",
        "h5", "application/octet-stream",
        "zip", "application/zip"
    );
    
    private static final Map<String, byte[]> FILE_SIGNATURES = Map.of(
        "jpeg", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF},
        "png", new byte[]{(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47},
        "gif", new byte[]{(byte)0x47, (byte)0x49, (byte)0x46, (byte)0x38},
        "zip", new byte[]{(byte)0x50, (byte)0x4B, (byte)0x03, (byte)0x04}
    );
    
    public ValidationResult validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.failure("文件不能为空");
        }
        
        if (file.getSize() > MAX_IMAGE_SIZE) {
            return ValidationResult.failure("文件大小不能超过10MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ValidationResult.failure("文件名无效");
        }
        
        String extension = getExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.containsKey(extension)) {
            return ValidationResult.failure("只允许上传 JPG, PNG, GIF, WebP 格式的图片");
        }
        
        String detectedType = detectFileType(file);
        if (detectedType == null || !ALLOWED_IMAGE_EXTENSIONS.containsValue(detectedType)) {
            return ValidationResult.failure("文件内容验证失败，可能不是有效的图片文件");
        }
        
        if (!verifyFileSignature(file, extension)) {
            return ValidationResult.failure("文件签名验证失败，文件可能被篡改");
        }
        
        return ValidationResult.success(extension);
    }
    
    public ValidationResult validateDataFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.failure("文件不能为空");
        }
        
        if (file.getSize() > MAX_DATA_SIZE) {
            return ValidationResult.failure("文件大小不能超过50MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ValidationResult.failure("文件名无效");
        }
        
        String extension = getExtension(originalFilename);
        if (!ALLOWED_DATA_EXTENSIONS.containsKey(extension)) {
            return ValidationResult.failure("只允许上传 CSV, JSON, TXT, Excel 格式的数据文件");
        }
        
        return ValidationResult.success(extension);
    }
    
    public ValidationResult validateModelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.failure("文件不能为空");
        }
        
        if (file.getSize() > MAX_MODEL_SIZE) {
            return ValidationResult.failure("文件大小不能超过1GB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ValidationResult.failure("文件名无效");
        }
        
        String extension = getExtension(originalFilename);
        if (!ALLOWED_MODEL_EXTENSIONS.containsKey(extension)) {
            return ValidationResult.failure("只允许上传 .pt, .pth, .onnx, .h5, .zip 格式的模型文件");
        }
        
        return ValidationResult.success(extension);
    }
    
    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) return "";
        return filename.substring(lastDot + 1).toLowerCase();
    }
    
    private String detectFileType(MultipartFile file) {
        try {
            byte[] header = new byte[8];
            int bytesRead = file.getInputStream().read(header);
            if (bytesRead < 4) return null;
            
            if (header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF) {
                return "image/jpeg";
            }
            if (header[0] == (byte)0x89 && header[1] == (byte)0x50 && 
                header[2] == (byte)0x4E && header[3] == (byte)0x47) {
                return "image/png";
            }
            if (header[0] == (byte)0x47 && header[1] == (byte)0x49 && 
                header[2] == (byte)0x46 && header[3] == (byte)0x38) {
                return "image/gif";
            }
            
            return null;
        } catch (IOException e) {
            return null;
        }
    }
    
    private boolean verifyFileSignature(MultipartFile file, String expectedExtension) {
        try {
            byte[] header = new byte[8];
            int bytesRead = file.getInputStream().read(header);
            if (bytesRead < 4) return false;
            
            byte[] expectedSignature = FILE_SIGNATURES.get(expectedExtension);
            if (expectedSignature == null) return true;
            
            for (int i = 0; i < expectedSignature.length; i++) {
                if (header[i] != expectedSignature[i]) {
                    return false;
                }
            }
            
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "file_" + System.currentTimeMillis();
        }
        
        filename = filename.replaceAll("[/\\\\]", "_");
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        filename = filename.replaceAll("_{2,}", "_");
        
        if (filename.startsWith(".")) {
            filename = "file" + filename;
        }
        
        if (filename.length() > 200) {
            int lastDot = filename.lastIndexOf('.');
            if (lastDot > 0) {
                String name = filename.substring(0, lastDot);
                String ext = filename.substring(lastDot);
                name = name.substring(0, 200 - ext.length());
                filename = name + ext;
            } else {
                filename = filename.substring(0, 200);
            }
        }
        
        return filename;
    }
    
    public String generateSafeFilename(String originalFilename) {
        String extension = getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        if (extension.isEmpty()) {
            return uuid;
        }
        
        return uuid + "." + extension;
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final String extension;
        
        private ValidationResult(boolean valid, String errorMessage, String extension) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.extension = extension;
        }
        
        public static ValidationResult success(String extension) {
            return new ValidationResult(true, null, extension);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage, null);
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public String getExtension() { return extension; }
    }
}
