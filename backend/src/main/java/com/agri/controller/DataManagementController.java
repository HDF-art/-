package com.agri.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.agri.utils.FileUploadValidator;
import com.agri.federation.AuditLogService;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/data")
@CrossOrigin
public class DataManagementController {
    
    private static final String UPLOAD_DIR = "/home/ubuntu/agri_data/";
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private FileUploadValidator fileUploadValidator;
    
    /**
     * 上传数据文件
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "description", required = false) String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证文件
            String error = fileUploadValidator.validateDataFile(file);
            if (error != null) {
                result.put("success", false);
                result.put("message", error);
                return result;
            }
            
            // 清理文件名
            String safeFilename = fileUploadValidator.sanitizeFilename(file.getOriginalFilename());
            
            // 创建用户目录
            String userDir = UPLOAD_DIR + userId + "/";
            new File(userDir).mkdirs();
            
            // 保存文件
            String filename = System.currentTimeMillis() + "_" + safeFilename;
            String filepath = userDir + filename;
            
            try (InputStream in = file.getInputStream();
                 OutputStream out = new FileOutputStream(filepath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            // 记录审计日志
            auditLogService.log(userId, "UPLOAD", "DATA", 
                "上传文件: " + filename + (description != null ? ", 描述: " + description : ""), 
                "local");
            
            result.put("success", true);
            result.put("filename", filename);
            result.put("filepath", filepath);
            result.put("size", file.getSize());
            result.put("message", "文件上传成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取数据文件列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String dir = userId != null ? UPLOAD_DIR + userId : UPLOAD_DIR;
            File folder = new File(dir);
            List<Map<String, Object>> files = new ArrayList<>();
            
            if (folder.exists() && folder.isDirectory()) {
                for (File file : folder.listFiles()) {
                    if (file.isFile()) {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("filename", file.getName());
                        fileInfo.put("size", file.length());
                        fileInfo.put("createdAt", new Date(file.lastModified()));
                        files.add(fileInfo);
                    }
                }
            }
            
            result.put("success", true);
            result.put("files", files);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 下载数据文件
     */
    @GetMapping("/download")
    public void download(@RequestParam String filename, @RequestParam String userId, javax.servlet.http.HttpServletResponse response) throws IOException {
        String filepath = UPLOAD_DIR + userId + "/" + filename;
        File file = new File(filepath);
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }
        
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentLengthLong(file.length());
        
        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }
}
