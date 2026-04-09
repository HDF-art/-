package com.agri.controller;

import com.agri.model.RecognitionRecord;
import com.agri.utils.FileUploadValidator;
import com.agri.service.RecognitionRecordService;
import com.agri.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

}