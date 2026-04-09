package com.agri.service.impl;

import com.agri.mapper.ModelMapper;
import com.agri.mapper.RecognitionRecordMapper;
import com.agri.model.Model;
import com.agri.model.RecognitionRecord;
import com.agri.service.RecognitionRecordService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RecognitionRecordServiceImpl extends ServiceImpl<RecognitionRecordMapper, RecognitionRecord> implements RecognitionRecordService {

    @Autowired
    private RecognitionRecordMapper recognitionRecordMapper;
    
    @Autowired
    private ModelMapper modelMapper;

    @Value("${upload.image-path}")
    private String imageSavePath;
    
    @Value("${model.service.url}")
    private String modelServiceUrl;

    @Override
    @Transactional
    public RecognitionRecord recognize(byte[] fileBytes, String fileName, Long userId, Long modelId, String cropType, String taskType) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new RuntimeException("图片不能为空");
        }

        File dir = new File(imageSavePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileExt = StringUtils.getFilenameExtension(fileName);
        String newFileName = UUID.randomUUID().toString() + "." + fileExt;
        String filePath = imageSavePath + newFileName;
        String relativePath = "/images/" + newFileName;

        try {
            Files.write(Paths.get(filePath), fileBytes);
            System.out.println("图片已保存: " + filePath);
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败", e);
        }

        System.out.println("调用ML服务: " + modelServiceUrl);
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                modelServiceUrl, requestEntity, String.class);
            
            System.out.println("ML服务返回: " + response.getBody());
            
            String result = response.getBody();
            String disease = "未知";
            String prevention = "请稍后再试";
            double confidence = 0.5;
            
            if (result != null && result.contains("disease")) {
                try {
                    com.fasterxml.jackson.databind.JsonNode json = 
                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(result);
                    if (json.has("disease")) {
                        disease = json.get("disease").asText();
                    }
                    if (json.has("treatment")) {
                        prevention = json.get("treatment").asText();
                    }
                    if (json.has("confidence")) {
                        confidence = json.get("confidence").asDouble();
                    }
                } catch (Exception e) {
                    System.out.println("解析ML服务结果失败: " + e.getMessage());
                }
            }
            
            RecognitionRecord record = new RecognitionRecord();
            record.setUserId(userId);
            record.setModelId(modelId);
            record.setImagePath(relativePath);
            record.setResult(disease);
            record.setConfidence(BigDecimal.valueOf(confidence));
            record.setCropType(cropType);
            record.setPreventionAdvice(prevention);
            record.setCreatedAt(LocalDateTime.now());
            
            recognitionRecordMapper.insert(record);
            
            return record;
            
        } catch (Exception e) {
            throw new RuntimeException("模型服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public Page<RecognitionRecord> getUserRecords(Long userId, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<RecognitionRecord> pg = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        
        return recognitionRecordMapper.selectPage(pg, 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RecognitionRecord>()
                .eq("user_id", userId)
                .orderByDesc("created_at"));
    }

    @Override
    public String generatePreventionAdvice(RecognitionRecord record) {
        if (record == null || record.getResult() == null) {
            return "无法生成防治建议";
        }
        
        String disease = record.getResult();
        
        if (disease.contains("稻瘟病")) {
            return "1. 选用抗病品种 2. 合理施肥，避免偏施氮肥 3. 及时喷施三环唑或富士一号 4. 收获后清除病残体";
        } else if (disease.contains("纹枯病")) {
            return "1. 及时捞除菌核 2. 避免过量施用氮肥 3. 发病初期喷施井冈霉素 4. 保持田间通风透光";
        } else if (disease.contains("锈病")) {
            return "1. 选用抗病品种 2. 加强栽培管理 3. 发病初期喷施粉锈宁 4. 避免连作";
        } else {
            return "1. 加强田间管理 2. 合理施肥浇水 3. 及时清除病残体 4. 必要时咨询当地农技部门";
        }
    }
}
