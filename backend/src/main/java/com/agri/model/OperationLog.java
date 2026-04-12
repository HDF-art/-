package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 用户角色（1:一级管理员, 2:二级管理员, 3:普通用户）
     */
    private Integer userRole;

    /**
     * 用户所属单位ID（农场ID）
     */
    private Long userFarmId;

    /**
     * 用户所属单位名称
     */
    private String userFarmName;

    /**
     * 操作类型（LOGIN, LOGOUT, CREATE, UPDATE, DELETE, VIEW, etc.）
     */
    private String operationType;

    /**
     * 操作模块（USER, DATASET, MODEL, TASK, etc.）
     */
    private String module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方法（GET, POST, PUT, DELETE）
     */
    private String requestMethod;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 客户端IP地址
     */
    private String ipAddress;

    /**
     * 浏览器信息
     */
    private String userAgent;

    /**
     * 操作结果（SUCCESS, FAILURE）
     */
    private String result;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 日志日期（用于文件存储，格式：yyyy-MM-dd）
     */
    private String logDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
