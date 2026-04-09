package com.agri.controller;

import com.agri.federation.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin
public class AuditLogController {
    
    @Autowired
    private AuditLogService auditLogService;
    
    /**
     * 记录操作日志
     */
    @PostMapping("/log")
    public Map<String, Object> log(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String username = (String) params.getOrDefault("username", "anonymous");
            String operation = (String) params.getOrDefault("operation", "");
            String resource = (String) params.getOrDefault("resource", "");
            String details = (String) params.getOrDefault("details", "");
            String ip = (String) params.getOrDefault("ip", "unknown");
            
            auditLogService.log(username, operation, resource, details, ip);
            
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 查询日志
     */
    @GetMapping("/logs")
    public Map<String, Object> queryLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> result = new HashMap<>();
        
        List<AuditLogService.AuditLog> logs = auditLogService.queryLogs(
            username, operation, startDate, endDate, page, size);
        
        result.put("success", true);
        result.put("logs", logs);
        
        return result;
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("statistics", auditLogService.getStatistics());
        return result;
    }
    
}
