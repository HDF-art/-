package com.agri.service;

public interface EmailCodeService {
    boolean sendCode(String email);
    boolean verifyCode(String email, String code);
    
    boolean sendAuditPassed(String email, String username);
    boolean sendAuditRejected(String email, String username, String reason);
}
