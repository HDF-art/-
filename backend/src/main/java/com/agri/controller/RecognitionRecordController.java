package com.agri.controller;

import com.agri.model.RecognitionRecord;
import com.agri.utils.FileUploadValidator;
import com.agri.service.RecognitionRecordService;
import com.agri.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 识别记录控制器
 */
@RestController
@RequestMapping("/recognition-records")
public class RecognitionRecordController {

    @Autowired
    private RecognitionRecordService recognitionRecordService;

    @Autowired
    private FileUploadValidator fileUploadValidator;

    @Value("${model.service.base-url:http://localhost:5000}")
    private String modelServiceBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 根据ID获取识别记录
     * @param recordId 记录ID
     * @return 识别记录
     */
    @GetMapping("/{id}")
    public RecognitionRecord getRecordById(@PathVariable("id") Long recordId) {
        return recognitionRecordService.getById(recordId);
    }

    /**
     * 获取用户的识别记录
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserRecords(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<RecognitionRecord> pageResult = recognitionRecordService.getUserRecords(userId, page, pageSize);
        
        // 构造统一的响应格式
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取识别记录成功");
        response.put("data", pageResult);
        
        return response;
    }

    /**
     * 进行农作物识别
     * @param file 图片文件
     * @param userId 用户ID
     * @param modelId 模型ID
     * @param cropType 作物类型
     * @param taskType 任务类型
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public java.util.Map<String, Object> recognize(@RequestPart("file") MultipartFile file,
                                     @RequestParam Long userId,
                                     @RequestParam(required = false) Long modelId,
                                     @RequestParam String cropType,
                                     @RequestParam(required = false) String taskType) {
        try {
            // 立即读取文件字节,避免Tomcat清理临时文件
            byte[] fileBytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            
            System.out.println("接收到文件: " + fileName + ", 大小: " + fileBytes.length + " bytes");
            System.out.println("任务类型: " + taskType + ", 模型ID: " + modelId);
            
            RecognitionRecord record = recognitionRecordService.recognize(fileBytes, fileName, userId, modelId, cropType, taskType);
            
            // 返回统一格式
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("code", 200);
            response.put("message", "识别成功");
            response.put("data", record);
            return response;
        } catch (Exception e) {
            System.err.println("读取文件失败: " + e.getMessage());
            e.printStackTrace();
            
            // 返回错误信息
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("code", 500);
            response.put("message", "识别失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/count-today")
    public ResponseUtils.ApiResponse<Long> getTodayCount() {
        try {
            long count = recognitionRecordService.count(new QueryWrapper<RecognitionRecord>().ge("created_at", LocalDate.now()));
            return ResponseUtils.success(count);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取今日识别次数失败: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public ResponseUtils.ApiResponse<Long> getTotalCount() {
        try {
            long count = recognitionRecordService.count();
            return ResponseUtils.success(count);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取识别总次数失败: " + e.getMessage());
        }
    }

    /**
     * 删除识别记录
     * @param recordId 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteRecord(@PathVariable("id") Long recordId) {
        boolean success = recognitionRecordService.removeById(recordId);

        // 构造统一的响应格式
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("code", 200);
            response.put("message", "删除成功");
            response.put("data", true);
        } else {
            response.put("code", 500);
            response.put("message", "删除失败");
            response.put("data", false);
        }

        return response;
    }

    /**
     * 环境预测 - 代理调用模型服务的 /predict/env 端点
     * @param body 包含 history 字段的请求体
     * @return 预测结果
     */
    @PostMapping("/predict-env")
    public Map<String, Object> predictEnv(@RequestBody Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> resp = restTemplate.exchange(
                    modelServiceBaseUrl + "/predict/env",
                    HttpMethod.POST, entity, Map.class);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "环境预测成功");
            response.put("data", resp.getBody());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "环境预测失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取环境预测模型信息 - 代理调用模型服务的 /env/info 端点
     * @return 模型信息
     */
    @GetMapping("/env-info")
    public Map<String, Object> getEnvInfo() {
        try {
            ResponseEntity<Map> resp = restTemplate.getForEntity(
                    modelServiceBaseUrl + "/env/info", Map.class);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取环境预测模型信息成功");
            response.put("data", resp.getBody());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取环境预测模型信息失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 解析Excel文件 - 代理调用模型服务的 /env/parse-excel 端点
     * @param file Excel文件
     * @return 解析后的时间序列数据
     */
    @PostMapping("/parse-env-excel")
    public Map<String, Object> parseEnvExcel(@RequestParam("file") MultipartFile file) {
        try {
            org.springframework.util.LinkedMultiValueMap<String, Object> body =
                    new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> resp = restTemplate.exchange(
                    modelServiceBaseUrl + "/env/parse-excel",
                    HttpMethod.POST, entity, Map.class);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Excel解析成功");
            response.put("data", resp.getBody());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Excel解析失败: " + e.getMessage());
            return response;
        }
    }

}