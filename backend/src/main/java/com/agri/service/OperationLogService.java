package com.agri.service;

import com.agri.dto.OperationLogQueryDTO;
import com.agri.model.OperationLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import java.util.Map;

public interface OperationLogService {

    void saveLog(OperationLog log);

    void saveLogAsync(OperationLog log);

    IPage<Map<String, Object>> queryLogs(OperationLogQueryDTO query, Long currentUserId, Integer currentUserRole, Long currentUserFarmId);

    Map<String, Object> getLogDetail(Long logId, Long currentUserId, Integer currentUserRole, Long currentUserFarmId);

    Map<String, Object> getStatistics(Long currentUserId, Integer currentUserRole, Long currentUserFarmId, String startDate, String endDate);

    List<String> getAllOperationTypes();

    List<String> getAllModules();

    void writeLogToFile(OperationLog log);

    List<Map<String, Object>> getLogsFromFile(String date, Long currentUserId, Integer currentUserRole, Long currentUserFarmId);

    Map<String, Object> exportLogs(OperationLogQueryDTO query, Long currentUserId, Integer currentUserRole, Long currentUserFarmId);
}
