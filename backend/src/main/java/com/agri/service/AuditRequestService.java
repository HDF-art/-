package com.agri.service;

import com.agri.model.AuditRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 审核请求服务接口
 */
public interface AuditRequestService extends IService<AuditRequest> {

    /**
     * 创建审核请求
     * @param auditRequest 审核请求信息
     * @return 创建结果
     */
    boolean createAuditRequest(AuditRequest auditRequest);

    /**
     * 处理审核请求
     * @param id 审核请求ID
     * @param status 审核状态
     * @param auditorId 审核人ID
     * @param auditComment 审核意见
     * @return 处理结果
     */
    boolean processAuditRequest(Long id, Integer status, Long auditorId, String auditComment);

    /**
     * 根据申请人ID获取审核请求列表
     * @param applicantId 申请人ID
     * @return 审核请求列表
     */
    List<AuditRequest> getByApplicantId(Long applicantId);

    /**
     * 根据审核状态获取审核请求列表
     * @param status 审核状态
     * @return 审核请求列表
     */
    List<AuditRequest> getByStatus(Integer status);

    /**
     * 根据类型获取审核请求列表
     * @param type 审核类型
     * @return 审核请求列表
     */
    List<AuditRequest> getByType(Integer type);

}
