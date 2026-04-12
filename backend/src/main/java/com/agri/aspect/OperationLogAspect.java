package com.agri.aspect;

import com.agri.annotation.OperationLog;
import com.agri.mapper.FarmMapper;
import com.agri.mapper.UserMapper;
import com.agri.model.Farm;
import com.agri.model.User;
import com.agri.model.UserPrincipal;
import com.agri.service.OperationLogService;
import com.agri.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FarmMapper farmMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(com.agri.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);
        
        com.agri.model.OperationLog log = new com.agri.model.OperationLog();
        log.setLogDate(LocalDate.now().format(DATE_FORMATTER));
        
        HttpServletRequest request = getRequest();
        if (request != null) {
            log.setRequestMethod(request.getMethod());
            log.setRequestUri(request.getRequestURI());
            log.setIpAddress(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            
            setUserInfo(log, request);
        }
        
        log.setOperationType(operationLogAnnotation.operationType());
        log.setModule(operationLogAnnotation.module());
        log.setDescription(operationLogAnnotation.description());
        
        if (operationLogAnnotation.saveParams()) {
            String params = getRequestParams(point, signature);
            log.setRequestParams(params);
        }
        
        Object result = null;
        Exception exception = null;
        
        try {
            result = point.proceed();
            log.setResult("SUCCESS");
            log.setResponseStatus(200);
        } catch (Exception e) {
            exception = e;
            log.setResult("FAILURE");
            log.setErrorMessage(e.getMessage());
            
            if (e.getClass().getName().contains("AccessDenied")) {
                log.setResponseStatus(403);
            } else {
                log.setResponseStatus(500);
            }
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            log.setResponseTime(endTime - startTime);
            
            try {
                operationLogService.saveLogAsync(log);
            } catch (Exception e) {
                logger.error("保存操作日志失败: {}", e.getMessage());
            }
        }
        
        return result;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private void setUserInfo(com.agri.model.OperationLog log, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            log.setUserId(principal.getId());
            log.setUsername(principal.getUsername());
            
            if (principal.getRole() != null) {
                try {
                    log.setUserRole(Integer.parseInt(principal.getRole()));
                } catch (NumberFormatException e) {
                    log.setUserRole(3);
                }
            }
            
            if (principal.getFarmId() != null) {
                log.setUserFarmId(principal.getFarmId());
                Farm farm = farmMapper.selectById(principal.getFarmId());
                if (farm != null) {
                    log.setUserFarmName(farm.getName());
                }
            }
        } else {
            String token = extractToken(request);
            if (token != null) {
                try {
                    Map<String, Object> tokenInfo = jwtUtils.validateToken(token);
                    if (Boolean.TRUE.equals(tokenInfo.get("valid"))) {
                        Object userIdObj = tokenInfo.get("userId");
                        Long userId = null;
                        if (userIdObj instanceof Integer) {
                            userId = ((Integer) userIdObj).longValue();
                        } else if (userIdObj instanceof Long) {
                            userId = (Long) userIdObj;
                        } else if (userIdObj != null) {
                            userId = Long.parseLong(userIdObj.toString());
                        }
                        
                        if (userId != null) {
                            log.setUserId(userId);
                            User user = userMapper.selectById(userId);
                            if (user != null) {
                                log.setUsername(user.getUsername());
                                log.setUserRole(user.getRole());
                                log.setUserFarmId(user.getFarmId());
                                if (user.getFarmId() != null) {
                                    Farm farm = farmMapper.selectById(user.getFarmId());
                                    if (farm != null) {
                                        log.setUserFarmName(farm.getName());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.debug("解析token失败: {}", e.getMessage());
                }
            }
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getRequestParams(ProceedingJoinPoint point, MethodSignature signature) {
        try {
            String[] paramNames = signature.getParameterNames();
            Object[] paramValues = point.getArgs();
            
            if (paramNames == null || paramNames.length == 0) {
                return null;
            }
            
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                Object value = paramValues[i];
                if (value instanceof HttpServletRequest 
                    || value instanceof HttpServletResponse 
                    || value instanceof MultipartFile) {
                    continue;
                }
                
                if (value != null) {
                    String valueStr = value.toString();
                    if (valueStr.length() > 500) {
                        valueStr = valueStr.substring(0, 500) + "...";
                    }
                    params.put(paramNames[i], valueStr);
                }
            }
            
            if (params.isEmpty()) {
                return null;
            }
            
            String json = objectMapper.writeValueAsString(params);
            if (json.length() > 2000) {
                json = json.substring(0, 2000) + "...";
            }
            return json;
        } catch (Exception e) {
            logger.debug("获取请求参数失败: {}", e.getMessage());
            return null;
        }
    }
}
