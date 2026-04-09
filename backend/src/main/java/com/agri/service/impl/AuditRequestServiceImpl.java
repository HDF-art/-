package com.agri.service.impl;

import com.agri.mapper.AuditRequestMapper;
import com.agri.model.AuditRequest;
import com.agri.model.User;
import com.agri.service.AuditRequestService;
import com.agri.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审核请求服务实现类
 */
@Service
public class AuditRequestServiceImpl extends ServiceImpl<AuditRequestMapper, AuditRequest> implements AuditRequestService {

    @Autowired
    private AuditRequestMapper auditRequestMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean createAuditRequest(AuditRequest auditRequest) {
        auditRequest.setStatus(0); // 待审核
        auditRequest.setCreatedAt(LocalDateTime.now());
        return save(auditRequest);
    }

    @Override
    public boolean processAuditRequest(Long id, Integer status, Long auditorId, String auditComment) {
        AuditRequest auditRequest = getById(id);
        if (auditRequest == null) {
            return false;
        }

        auditRequest.setStatus(status);
        auditRequest.setAuditorId(auditorId);
        auditRequest.setAuditComment(auditComment);
        auditRequest.setAuditedAt(LocalDateTime.now());

        boolean result = updateById(auditRequest);

        // 如果审核通过，执行相应的操作
        if (result && status == 1) {
            processApprovedRequest(auditRequest);
        }

        return result;
    }

    private void processApprovedRequest(AuditRequest auditRequest) {
        try {
            switch (auditRequest.getType()) {
                case 1: // 二级管理员个人信息修改
                    processAdmin2InfoUpdate(auditRequest);
                    break;
                case 2: // 二级管理员账号注销
                    processAdmin2AccountDelete(auditRequest);
                    break;
                case 3: // 普通用户加入农场
                    processUserJoinFarm(auditRequest);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processAdmin2InfoUpdate(AuditRequest auditRequest) throws Exception {
        Map<String, Object> content = objectMapper.readValue(auditRequest.getContent(), Map.class);
        Long userId = auditRequest.getApplicantId();
        User user = userService.getById(userId);
        if (user != null) {
            if (content.containsKey("name")) {
                user.setName((String) content.get("name"));
            }
            if (content.containsKey("email")) {
                user.setEmail((String) content.get("email"));
            }
            if (content.containsKey("phone")) {
                user.setPhone((String) content.get("phone"));
            }
            user.setUpdatedAt(LocalDateTime.now());
            userService.updateUser(user);
        }
    }

    private void processAdmin2AccountDelete(AuditRequest auditRequest) {
        Long userId = auditRequest.getApplicantId();
        userService.deleteUser(userId);
    }

    private void processUserJoinFarm(AuditRequest auditRequest) {
        Long userId = auditRequest.getApplicantId();
        Long farmId = auditRequest.getFarmId();
        User user = userService.getById(userId);
        if (user != null) {
            user.setFarmId(farmId);
            user.setUpdatedAt(LocalDateTime.now());
            userService.updateUser(user);
        }
    }

    @Override
    public List<AuditRequest> getByApplicantId(Long applicantId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AuditRequest>()
                .eq("applicant_id", applicantId));
    }

    @Override
    public List<AuditRequest> getByStatus(Integer status) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AuditRequest>()
                .eq("status", status));
    }

    @Override
    public List<AuditRequest> getByType(Integer type) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AuditRequest>()
                .eq("type", type));
    }

}
