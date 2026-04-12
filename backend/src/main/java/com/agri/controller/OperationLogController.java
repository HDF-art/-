package com.agri.controller;

import com.agri.dto.OperationLogQueryDTO;
import com.agri.model.UserPrincipal;
import com.agri.service.OperationLogService;
import com.agri.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
@CrossOrigin
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public ResponseUtils.ApiResponse<Map<String, Object>> getLogList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer userRole,
            @RequestParam(required = false) Long userFarmId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String ipAddress) {

        OperationLogQueryDTO query = new OperationLogQueryDTO();
        query.setPage(page);
        query.setSize(size);
        query.setUserId(userId);
        query.setUsername(username);
        query.setUserRole(userRole);
        query.setUserFarmId(userFarmId);
        query.setOperationType(operationType);
        query.setModule(module);
        query.setResult(result);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setIpAddress(ipAddress);

        Long[] userInfo = getCurrentUserInfo();
        IPage<Map<String, Object>> pageData = operationLogService.queryLogs(query, userInfo[0], userInfo[1].intValue(), userInfo[2]);

        Map<String, Object> result2 = Map.of(
                "list", pageData.getRecords(),
                "total", pageData.getTotal(),
                "page", pageData.getCurrent(),
                "size", pageData.getSize()
        );

        return ResponseUtils.success(result2);
    }

    @GetMapping("/detail/{id}")
    public ResponseUtils.ApiResponse<Map<String, Object>> getLogDetail(@PathVariable Long id) {
        Long[] userInfo = getCurrentUserInfo();
        Map<String, Object> log = operationLogService.getLogDetail(id, userInfo[0], userInfo[1].intValue(), userInfo[2]);
        
        if (log == null) {
            return ResponseUtils.error(404, "日志不存在或无权限查看");
        }
        
        return ResponseUtils.success(log);
    }

    @GetMapping("/statistics")
    public ResponseUtils.ApiResponse<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Long[] userInfo = getCurrentUserInfo();
        Map<String, Object> stats = operationLogService.getStatistics(
                userInfo[0], userInfo[1].intValue(), userInfo[2], startDate, endDate);
        
        return ResponseUtils.success(stats);
    }

    @GetMapping("/operation-types")
    public ResponseUtils.ApiResponse<List<String>> getAllOperationTypes() {
        return ResponseUtils.success(operationLogService.getAllOperationTypes());
    }

    @GetMapping("/modules")
    public ResponseUtils.ApiResponse<List<String>> getAllModules() {
        return ResponseUtils.success(operationLogService.getAllModules());
    }

    @GetMapping("/file/{date}")
    public ResponseUtils.ApiResponse<List<Map<String, Object>>> getLogsFromFile(@PathVariable String date) {
        Long[] userInfo = getCurrentUserInfo();
        List<Map<String, Object>> logs = operationLogService.getLogsFromFile(
                date, userInfo[0], userInfo[1].intValue(), userInfo[2]);
        return ResponseUtils.success(logs);
    }

    @GetMapping("/export")
    public ResponseUtils.ApiResponse<Map<String, Object>> exportLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "1000") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer userRole,
            @RequestParam(required = false) Long userFarmId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String ipAddress) {

        OperationLogQueryDTO query = new OperationLogQueryDTO();
        query.setPage(page);
        query.setSize(size);
        query.setUserId(userId);
        query.setUsername(username);
        query.setUserRole(userRole);
        query.setUserFarmId(userFarmId);
        query.setOperationType(operationType);
        query.setModule(module);
        query.setResult(result);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setIpAddress(ipAddress);

        Long[] userInfo = getCurrentUserInfo();
        Map<String, Object> result2 = operationLogService.exportLogs(
                query, userInfo[0], userInfo[1].intValue(), userInfo[2]);

        return ResponseUtils.success(result2);
    }

    private Long[] getCurrentUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            Long roleValue = 3L;
            if (principal.getRole() != null) {
                try {
                    roleValue = Long.parseLong(principal.getRole());
                } catch (NumberFormatException e) {
                    roleValue = 3L;
                }
            }
            return new Long[]{principal.getId(), roleValue, principal.getFarmId()};
        }
        return new Long[]{null, 3L, null};
    }
}
