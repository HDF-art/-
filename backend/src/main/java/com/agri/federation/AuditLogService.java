package com.agri.federation;

import org.springframework.stereotype.Service;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * 审计日志服务
 */
@Service
public class AuditLogService {
    
    private List<AuditLog> logs = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_LOGS = 10000;
    
    /**
     * 记录操作日志
     */
    public void log(String username, String operation, String resource, String details, String ip) {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(UUID.randomUUID().toString());
        auditLog.setUsername(username);
        auditLog.setOperation(operation);
        auditLog.setResource(resource);
        auditLog.setDetails(details);
        auditLog.setIp(ip);
        auditLog.setTimestamp(new Date());
        
        // 超过最大数量时删除最早的
        if (logs.size() >= MAX_LOGS) {
            logs.remove(0);
        }
        
        logs.add(auditLog);
    }
    
    /**
     * 查询日志
     */
    public List<AuditLog> queryLogs(String username, String operation, Date startDate, Date endDate, int page, int size) {
        List<AuditLog> result = new ArrayList<>();
        
        for (AuditLog log : logs) {
            boolean match = true;
            
            if (username != null && !username.equals(log.getUsername())) {
                match = false;
            }
            if (operation != null && !operation.equals(log.getOperation())) {
                match = false;
            }
            if (startDate != null && log.getTimestamp().before(startDate)) {
                match = false;
            }
            if (endDate != null && log.getTimestamp().after(endDate)) {
                match = false;
            }
            
            if (match) {
                result.add(log);
            }
        }
        
        // 分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, result.size());
        
        return result.subList(Math.min(start, result.size()), end);
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总数
        stats.put("total", logs.size());
        
        // 按操作类型统计
        Map<String, Integer> byOperation = new HashMap<>();
        for (AuditLog log : logs) {
            byOperation.put(log.getOperation(), byOperation.getOrDefault(log.getOperation(), 0) + 1);
        }
        stats.put("byOperation", byOperation);
        
        // 按用户统计
        Map<String, Integer> byUser = new HashMap<>();
        for (AuditLog log : logs) {
            byUser.put(log.getUsername(), byUser.getOrDefault(log.getUsername(), 0) + 1);
        }
        stats.put("byUser", byUser);
        
        return stats;
    }
    
    /**
     * 清空日志
     */
    public void clearLogs() {
        logs.clear();
    }
    
    // 审计日志内部类
    public static class AuditLog {
        private String id;
        private String username;
        private String operation;
        private String resource;
        private String details;
        private String ip;
        private Date timestamp;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    }
}
