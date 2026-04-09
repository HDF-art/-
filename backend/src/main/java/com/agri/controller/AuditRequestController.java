package com.agri.controller;

import com.agri.model.AuditRequest;
import com.agri.service.AuditRequestService;
import com.agri.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 审核请求控制器
 */
@RestController
@RequestMapping("/audit-requests")
public class AuditRequestController {

    @Autowired
    private AuditRequestService auditRequestService;

    /**
     * 创建审核请求
     * @param auditRequest 审核请求信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseUtils.ApiResponse<Boolean> createAuditRequest(@RequestBody AuditRequest auditRequest) {
        try {
            boolean result = auditRequestService.createAuditRequest(auditRequest);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "创建审核请求失败: " + e.getMessage());
        }
    }

    /**
     * 处理审核请求
     * @param id 审核请求ID
     * @param processInfo 处理信息
     * @return 处理结果
     */
    @PutMapping("/{id}/process")
    public ResponseUtils.ApiResponse<Boolean> processAuditRequest(@PathVariable("id") Long id, @RequestBody Map<String, Object> processInfo) {
        try {
            Integer status = (Integer) processInfo.get("status");
            Long auditorId = (Long) processInfo.get("auditorId");
            String auditComment = (String) processInfo.get("auditComment");
            boolean result = auditRequestService.processAuditRequest(id, status, auditorId, auditComment);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "处理审核请求失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取审核请求
     * @param id 审核请求ID
     * @return 审核请求信息
     */
    @GetMapping("/{id}")
    public ResponseUtils.ApiResponse<AuditRequest> getAuditRequestById(@PathVariable("id") Long id) {
        try {
            AuditRequest auditRequest = auditRequestService.getById(id);
            return ResponseUtils.success(auditRequest);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取审核请求失败: " + e.getMessage());
        }
    }

    /**
     * 根据申请人ID获取审核请求列表
     * @param applicantId 申请人ID
     * @return 审核请求列表
     */
    @GetMapping("/applicant/{applicantId}")
    public ResponseUtils.ApiResponse<List<AuditRequest>> getAuditRequestsByApplicantId(@PathVariable("applicantId") Long applicantId) {
        try {
            List<AuditRequest> auditRequests = auditRequestService.getByApplicantId(applicantId);
            return ResponseUtils.success(auditRequests);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取审核请求列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据审核状态获取审核请求列表
     * @param status 审核状态
     * @return 审核请求列表
     */
    @GetMapping("/status/{status}")
    public ResponseUtils.ApiResponse<List<AuditRequest>> getAuditRequestsByStatus(@PathVariable("status") Integer status) {
        try {
            List<AuditRequest> auditRequests = auditRequestService.getByStatus(status);
            return ResponseUtils.success(auditRequests);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取审核请求列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取审核请求列表
     * @param type 审核类型
     * @return 审核请求列表
     */
    @GetMapping("/type/{type}")
    public ResponseUtils.ApiResponse<List<AuditRequest>> getAuditRequestsByType(@PathVariable("type") Integer type) {
        try {
            List<AuditRequest> auditRequests = auditRequestService.getByType(type);
            return ResponseUtils.success(auditRequests);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取审核请求列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有审核请求列表
     * @return 审核请求列表
     */
    @GetMapping
    public ResponseUtils.ApiResponse<List<AuditRequest>> getAllAuditRequests() {
        try {
            List<AuditRequest> auditRequests = auditRequestService.list();
            return ResponseUtils.success(auditRequests);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取审核请求列表失败: " + e.getMessage());
        }
    }

}
