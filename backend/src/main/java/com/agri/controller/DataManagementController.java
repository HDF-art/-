package com.agri.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.agri.utils.FileUploadValidator;
import com.agri.federation.AuditLogService;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/data")
@CrossOrigin
public class DataManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(DataManagementController.class);
    private static final Path UPLOAD_DIR = Paths.get("/home/ubuntu/agri_data").toAbsolutePath().normalize();
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private FileUploadValidator fileUploadValidator;
    
    private Path validateAndResolvePath(String userId, String filename) {
        if (userId == null || !userId.matches("^[a-zA-Z0-9_-]+$")) {
            throw new SecurityException("无效的用户ID");
        }
        
        if (filename == null || filename.isEmpty()) {
            throw new SecurityException("文件名不能为空");
        }
        
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new SecurityException("文件名包含非法字符");
        }
        
        Path targetPath = UPLOAD_DIR.resolve(userId).resolve(filename).normalize();
        
        if (!targetPath.startsWith(UPLOAD_DIR)) {
            throw new SecurityException("检测到目录遍历攻击");
        }
        
        return targetPath;
    }
    
    private Path validateUserDirectory(String userId) {
        if (userId == null || !userId.matches("^[a-zA-Z0-9_-]+$")) {
            throw new SecurityException("无效的用户ID");
        }
        
        Path userDir = UPLOAD_DIR.resolve(userId).normalize();
        
        if (!userDir.startsWith(UPLOAD_DIR)) {
            throw new SecurityException("检测到目录遍历攻击");
        }
        
        return userDir;
    }
    
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "description", required = false) String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            FileUploadValidator.ValidationResult validationResult = fileUploadValidator.validateDataFile(file);
            if (!validationResult.isValid()) {
                result.put("success", false);
                result.put("message", validationResult.getErrorMessage());
                return result;
            }
            
            String safeFilename = fileUploadValidator.generateSafeFilename(file.getOriginalFilename());
            
            Path userDir = validateUserDirectory(userId);
            Files.createDirectories(userDir);
            
            Path targetPath = userDir.resolve(safeFilename).normalize();
            
            if (!targetPath.startsWith(userDir)) {
                result.put("success", false);
                result.put("message", "文件路径验证失败");
                return result;
            }
            
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            auditLogService.log(userId, "UPLOAD", "DATA", 
                "上传文件: " + safeFilename + (description != null ? ", 描述: " + description : ""), 
                "local");
            
            result.put("success", true);
            result.put("filename", safeFilename);
            result.put("message", "文件上传成功");
        } catch (SecurityException e) {
            logger.warn("安全违规: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "操作被拒绝");
        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "文件上传失败");
        }
        
        return result;
    }
    
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path targetDir;
            if (userId != null && !userId.isEmpty()) {
                targetDir = validateUserDirectory(userId);
            } else {
                targetDir = UPLOAD_DIR;
            }
            
            if (!targetDir.startsWith(UPLOAD_DIR)) {
                result.put("success", false);
                result.put("message", "访问被拒绝");
                return result;
            }
            
            List<Map<String, Object>> files = new ArrayList<>();
            
            if (Files.exists(targetDir) && Files.isDirectory(targetDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
                    for (Path filePath : stream) {
                        if (Files.isRegularFile(filePath)) {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("filename", filePath.getFileName().toString());
                            fileInfo.put("size", Files.size(filePath));
                            fileInfo.put("createdAt", new Date(Files.getLastModifiedTime(filePath).toMillis()));
                            files.add(fileInfo);
                        }
                    }
                }
            }
            
            result.put("success", true);
            result.put("files", files);
        } catch (SecurityException e) {
            logger.warn("安全违规: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "访问被拒绝");
        } catch (Exception e) {
            logger.error("获取文件列表失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取文件列表失败");
        }
        
        return result;
    }
    
    @GetMapping("/download")
    public void download(@RequestParam String filename, @RequestParam String userId, 
            javax.servlet.http.HttpServletResponse response) throws IOException {
        
        try {
            Path targetPath = validateAndResolvePath(userId, filename);
            
            if (!Files.exists(targetPath)) {
                response.setStatus(404);
                return;
            }
            
            String safeFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + safeFilename + "\"");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setContentLengthLong(Files.size(targetPath));
            
            Files.copy(targetPath, response.getOutputStream());
            response.getOutputStream().flush();
            
        } catch (SecurityException e) {
            logger.warn("安全违规: {}", e.getMessage());
            response.setStatus(403);
        } catch (Exception e) {
            logger.error("文件下载失败: {}", e.getMessage());
            response.setStatus(500);
        }
    }
    
    @DeleteMapping("/delete")
    public Map<String, Object> delete(@RequestParam String filename, @RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path targetPath = validateAndResolvePath(userId, filename);
            
            if (!Files.exists(targetPath)) {
                result.put("success", false);
                result.put("message", "文件不存在");
                return result;
            }
            
            Files.delete(targetPath);
            
            auditLogService.log(userId, "DELETE", "DATA", "删除文件: " + filename, "local");
            
            result.put("success", true);
            result.put("message", "文件删除成功");
        } catch (SecurityException e) {
            logger.warn("安全违规: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "操作被拒绝");
        } catch (Exception e) {
            logger.error("文件删除失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "文件删除失败");
        }
        
        return result;
    }
}
