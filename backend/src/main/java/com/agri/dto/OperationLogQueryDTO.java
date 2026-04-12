package com.agri.dto;

import lombok.Data;

@Data
public class OperationLogQueryDTO {
    private Integer page = 1;
    private Integer size = 20;
    private Long userId;
    private String username;
    private Integer userRole;
    private Long userFarmId;
    private String operationType;
    private String module;
    private String result;
    private String startDate;
    private String endDate;
    private String ipAddress;
}
