package com.agri.service.impl;

import com.agri.dto.OperationLogQueryDTO;
import com.agri.mapper.FarmMapper;
import com.agri.mapper.OperationLogMapper;
import com.agri.mapper.UserMapper;
import com.agri.model.Farm;
import com.agri.model.OperationLog;
import com.agri.model.User;
import com.agri.service.OperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, ReentrantReadWriteLock> fileLocks = new HashMap<>();
    private final ReentrantReadWriteLock lockMapLock = new ReentrantReadWriteLock();

    @Value("${log.operation.path:/home/ubuntu/农业大数据联合建模平台/logs/operation}")
    private String logBasePath;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FarmMapper farmMapper;

    @Override
    public void saveLog(OperationLog log) {
        log.setCreatedAt(LocalDateTime.now());
        log.setLogDate(LocalDate.now().format(DATE_FORMATTER));
        operationLogMapper.insert(log);
        writeLogToFile(log);
    }

    @Override
    @Async
    public void saveLogAsync(OperationLog log) {
        try {
            saveLog(log);
        } catch (Exception e) {
            logger.error("保存日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public IPage<Map<String, Object>> queryLogs(OperationLogQueryDTO query, Long currentUserId, Integer currentUserRole, Long currentUserFarmId) {
        Page<Map<String, Object>> page = new Page<>(query.getPage(), query.getSize());

        applyPermissionFilters(query, currentUserId, currentUserRole, currentUserFarmId);

        return operationLogMapper.selectLogPage(page,
                query.getUserId(),
                query.getUsername(),
                query.getUserRole(),
                query.getUserFarmId(),
                query.getOperationType(),
                query.getModule(),
                query.getResult(),
                query.getStartDate(),
                query.getEndDate(),
                query.getIpAddress());
    }

    @Override
    public Map<String, Object> getLogDetail(Long logId, Long currentUserId, Integer currentUserRole, Long currentUserFarmId) {
        OperationLog log = operationLogMapper.selectById(logId);
        if (log == null) {
            return null;
        }

        if (!canViewLog(log, currentUserId, currentUserRole, currentUserFarmId)) {
            return null;
        }

        Map<String, Object> result = convertLogToMap(log);
        
        User user = userMapper.selectById(log.getUserId());
        if (user != null) {
            result.put("username", user.getUsername());
            result.put("userRole", user.getRole());
            result.put("userFarmId", user.getFarmId());
            if (user.getFarmId() != null) {
                Farm farm = farmMapper.selectById(user.getFarmId());
                if (farm != null) {
                    result.put("userFarmName", farm.getName());
                }
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> getStatistics(Long currentUserId, Integer currentUserRole, Long currentUserFarmId, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        Long queryUserId = null;
        if (currentUserRole == 3) {
            queryUserId = currentUserId;
        }

        List<Map<String, Object>> operationTypeStats = operationLogMapper.selectOperationTypeStats(queryUserId, startDate, endDate);
        List<Map<String, Object>> moduleStats = operationLogMapper.selectModuleStats(queryUserId, startDate, endDate);
        List<Map<String, Object>> dailyStats = operationLogMapper.selectDailyStats(queryUserId, startDate, endDate);

        result.put("operationTypeStats", operationTypeStats);
        result.put("moduleStats", moduleStats);
        result.put("dailyStats", dailyStats);

        return result;
    }

    @Override
    public List<String> getAllOperationTypes() {
        return operationLogMapper.selectAllOperationTypes();
    }

    @Override
    public List<String> getAllModules() {
        return operationLogMapper.selectAllModules();
    }

    @Override
    public void writeLogToFile(OperationLog log) {
        try {
            String date = log.getLogDate();
            Path logDir = Paths.get(logBasePath);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            Path logFile = logDir.resolve("operation_" + date + ".log");

            ReentrantReadWriteLock fileLock = getFileLock(date);
            fileLock.writeLock().lock();
            try {
                String logEntry = formatLogEntry(log);
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(logFile.toFile(), true), StandardCharsets.UTF_8))) {
                    writer.write(logEntry);
                    writer.newLine();
                }
            } finally {
                fileLock.writeLock().unlock();
            }
        } catch (Exception e) {
            logger.error("写入日志文件失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getLogsFromFile(String date, Long currentUserId, Integer currentUserRole, Long currentUserFarmId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Path logFile = Paths.get(logBasePath, "operation_" + date + ".log");
            if (!Files.exists(logFile)) {
                return result;
            }

            ReentrantReadWriteLock fileLock = getFileLock(date);
            fileLock.readLock().lock();
            try {
                List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
                for (String line : lines) {
                    try {
                        Map<String, Object> logMap = parseLogEntry(line);
                        if (logMap != null && canViewLogFromMap(logMap, currentUserId, currentUserRole, currentUserFarmId)) {
                            result.add(logMap);
                        }
                    } catch (Exception e) {
                        logger.warn("解析日志行失败: {}", line, e);
                    }
                }
            } finally {
                fileLock.readLock().unlock();
            }
        } catch (Exception e) {
            logger.error("读取日志文件失败: {}", e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Map<String, Object> exportLogs(OperationLogQueryDTO query, Long currentUserId, Integer currentUserRole, Long currentUserFarmId) {
        Map<String, Object> result = new HashMap<>();
        try {
            IPage<Map<String, Object>> pageData = queryLogs(query, currentUserId, currentUserRole, currentUserFarmId);
            result.put("logs", pageData.getRecords());
            result.put("total", pageData.getTotal());
        } catch (Exception e) {
            logger.error("导出日志失败: {}", e.getMessage(), e);
        }
        return result;
    }

    private void applyPermissionFilters(OperationLogQueryDTO query, Long currentUserId, Integer currentUserRole, Long currentUserFarmId) {
        if (currentUserRole == 1) {
            // 一级管理员：可以查看所有日志，不添加额外过滤
        } else if (currentUserRole == 2) {
            // 二级管理员：只能查看自己和本单位用户的日志
            query.setUserFarmId(currentUserFarmId);
        } else {
            // 普通用户：只能查看自己的日志
            query.setUserId(currentUserId);
        }
    }

    private boolean canViewLog(OperationLog log, Long currentUserId, Integer currentUserRole, Long currentUserFarmId) {
        if (currentUserRole == 1) {
            return true;
        }
        
        if (log.getUserId() == null) {
            return false;
        }

        if (currentUserRole == 2) {
            User logUser = userMapper.selectById(log.getUserId());
            if (logUser == null) {
                return false;
            }
            // 二级管理员可以查看自己的日志和同单位用户的日志
            if (log.getUserId().equals(currentUserId)) {
                return true;
            }
            return logUser.getFarmId() != null && logUser.getFarmId().equals(currentUserFarmId);
        }

        // 普通用户只能查看自己的日志
        return log.getUserId().equals(currentUserId);
    }

    private boolean canViewLogFromMap(Map<String, Object> logMap, Long currentUserId, Integer currentUserRole, Long currentUserFarmId) {
        if (currentUserRole == 1) {
            return true;
        }

        Long logUserId = logMap.get("userId") != null ? Long.valueOf(logMap.get("userId").toString()) : null;
        if (logUserId == null) {
            return false;
        }

        if (currentUserRole == 2) {
            Long logUserFarmId = logMap.get("userFarmId") != null ? Long.valueOf(logMap.get("userFarmId").toString()) : null;
            if (logUserId.equals(currentUserId)) {
                return true;
            }
            return logUserFarmId != null && logUserFarmId.equals(currentUserFarmId);
        }

        return logUserId.equals(currentUserId);
    }

    private Map<String, Object> convertLogToMap(OperationLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("userId", log.getUserId());
        map.put("username", log.getUsername());
        map.put("userRole", log.getUserRole());
        map.put("userFarmId", log.getUserFarmId());
        map.put("userFarmName", log.getUserFarmName());
        map.put("operationType", log.getOperationType());
        map.put("module", log.getModule());
        map.put("description", log.getDescription());
        map.put("requestMethod", log.getRequestMethod());
        map.put("requestUri", log.getRequestUri());
        map.put("requestParams", log.getRequestParams());
        map.put("responseStatus", log.getResponseStatus());
        map.put("responseTime", log.getResponseTime());
        map.put("ipAddress", log.getIpAddress());
        map.put("userAgent", log.getUserAgent());
        map.put("result", log.getResult());
        map.put("errorMessage", log.getErrorMessage());
        map.put("logDate", log.getLogDate());
        map.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().format(DATETIME_FORMATTER) : null);
        return map;
    }

    private String formatLogEntry(OperationLog log) {
        StringBuilder sb = new StringBuilder();
        sb.append(log.getCreatedAt() != null ? log.getCreatedAt().format(DATETIME_FORMATTER) : "").append("|");
        sb.append(log.getId()).append("|");
        sb.append(log.getUserId()).append("|");
        sb.append(escapeField(log.getUsername())).append("|");
        sb.append(log.getUserRole()).append("|");
        sb.append(log.getUserFarmId()).append("|");
        sb.append(escapeField(log.getUserFarmName())).append("|");
        sb.append(escapeField(log.getOperationType())).append("|");
        sb.append(escapeField(log.getModule())).append("|");
        sb.append(escapeField(log.getDescription())).append("|");
        sb.append(escapeField(log.getRequestMethod())).append("|");
        sb.append(escapeField(log.getRequestUri())).append("|");
        sb.append(escapeField(log.getRequestParams())).append("|");
        sb.append(log.getResponseStatus()).append("|");
        sb.append(log.getResponseTime()).append("|");
        sb.append(escapeField(log.getIpAddress())).append("|");
        sb.append(escapeField(log.getUserAgent())).append("|");
        sb.append(escapeField(log.getResult())).append("|");
        sb.append(escapeField(log.getErrorMessage()));
        return sb.toString();
    }

    private Map<String, Object> parseLogEntry(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] parts = line.split("\\|", -1);
        if (parts.length < 19) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("createdAt", parts[0]);
        map.put("id", parseLong(parts[1]));
        map.put("userId", parseLong(parts[2]));
        map.put("username", unescapeField(parts[3]));
        map.put("userRole", parseInteger(parts[4]));
        map.put("userFarmId", parseLong(parts[5]));
        map.put("userFarmName", unescapeField(parts[6]));
        map.put("operationType", unescapeField(parts[7]));
        map.put("module", unescapeField(parts[8]));
        map.put("description", unescapeField(parts[9]));
        map.put("requestMethod", unescapeField(parts[10]));
        map.put("requestUri", unescapeField(parts[11]));
        map.put("requestParams", unescapeField(parts[12]));
        map.put("responseStatus", parseInteger(parts[13]));
        map.put("responseTime", parseLong(parts[14]));
        map.put("ipAddress", unescapeField(parts[15]));
        map.put("userAgent", unescapeField(parts[16]));
        map.put("result", unescapeField(parts[17]));
        map.put("errorMessage", unescapeField(parts[18]));
        return map;
    }

    private String escapeField(String field) {
        if (field == null) {
            return "";
        }
        return field.replace("|", "\\|").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String unescapeField(String field) {
        if (field == null || field.isEmpty()) {
            return null;
        }
        return field.replace("\\|", "|").replace("\\n", "\n").replace("\\r", "\r");
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ReentrantReadWriteLock getFileLock(String date) {
        lockMapLock.readLock().lock();
        try {
            ReentrantReadWriteLock lock = fileLocks.get(date);
            if (lock != null) {
                return lock;
            }
        } finally {
            lockMapLock.readLock().unlock();
        }

        lockMapLock.writeLock().lock();
        try {
            return fileLocks.computeIfAbsent(date, k -> new ReentrantReadWriteLock());
        } finally {
            lockMapLock.writeLock().unlock();
        }
    }
}
