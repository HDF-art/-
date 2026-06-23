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
    
    @Value("${model.service.base-url:http://localhost:5000}")
    private String modelServiceBaseUrl;

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
            
            // 根据模型任务类型选择推理端点
            String endpointUrl = modelServiceUrl; // 默认病虫害识别端点
            String modelTaskType = taskType;
            if (modelId != null) {
                Model model = modelMapper.selectById(modelId);
                if (model != null && model.getTaskType() != null) {
                    modelTaskType = model.getTaskType();
                    if ("strawberry_ripeness".equals(modelTaskType)) {
                        endpointUrl = modelServiceBaseUrl + "/predict/strawberry";
                    } else if ("env_prediction".equals(modelTaskType)) {
                        // 环境预测是时间序列输入，不走图片识别流程
                        throw new RuntimeException("环境预测模型不支持图片识别，请使用环境预测接口");
                    }
                }
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            System.out.println("调用推理端点: " + endpointUrl + " (taskType=" + modelTaskType + ")");
            ResponseEntity<String> response = restTemplate.postForEntity(
                endpointUrl, requestEntity, String.class);
            
            System.out.println("ML服务返回: " + response.getBody());
            
            String result = response.getBody();
            String disease = "未知";
            String prevention = "请稍后再试";
            double confidence = 0.5;
            String detailsJson = null;
            
            if (result != null && result.contains("diseaseName")) {
                try {
                    com.fasterxml.jackson.databind.JsonNode json = 
                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(result);
                    if (json.has("diseaseName")) {
                        disease = json.get("diseaseName").asText();
                    }
                    if (json.has("confidence")) {
                        confidence = json.get("confidence").asDouble();
                    }
                    if (json.has("prevention") && !json.get("prevention").asText().isEmpty()) {
                        prevention = json.get("prevention").asText();
                    }
                    if (json.has("details")) {
                        detailsJson = json.get("details").toString();
                    }
                } catch (Exception e) {
                    System.out.println("解析ML服务结果失败: " + e.getMessage());
                }
            }
            
            // 根据病虫害名称生成防治建议
            prevention = getPreventionAdvice(disease);
            
            RecognitionRecord record = new RecognitionRecord();
            record.setUserId(userId);
            record.setModelId(modelId);
            record.setImagePath(relativePath);
            record.setResult(disease);
            record.setConfidence(BigDecimal.valueOf(confidence));
            record.setCropType(cropType);
            record.setPreventionAdvice(prevention);
            record.setResultJson(detailsJson);
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
    
    private String getPreventionAdvice(String disease) {
        if (disease == null || disease.isEmpty()) {
            return "请咨询当地农技部门获取防治建议";
        }
        // 昆虫类害虫
        if (disease.contains("稻象甲")) return "1. 田间深水灌溉抑制幼虫 2. 使用氯虫苯甲酰胺喷雾防治 3. 清除田间杂草减少越冬场所 4. 灯光诱杀成虫";
        if (disease.contains("飞虱")) return "1. 选用抗虫品种 2. 合理施肥避免偏施氮肥 3. 使用吡蚜酮或烯啶虫胺防治 4. 保护利用天敌如蜘蛛和黑肩绿盲蝽";
        if (disease.contains("叶蝉")) return "1. 选用抗虫品种 2. 清除田边杂草 3. 使用吡虫啉或噻虫嗪喷雾 4. 利用灯光诱杀";
        if (disease.contains("管蓟马")) return "1. 清除田间残株和杂草 2. 蓝板诱杀成虫 3. 使用多杀霉素或乙基多杀菌素防治 4. 适时播种避开高峰期";
        if (disease.contains("瘿蚊")) return "1. 选用抗虫品种 2. 适时晒田抑制幼虫 3. 使用氯虫苯甲酰胺防治 4. 保护利用天敌";
        if (disease.contains("弄蝶")) return "1. 人工摘除虫苞 2. 使用甲维盐或氯虫苯甲酰胺喷雾 3. 保护利用赤眼蜂等天敌 4. 灯光诱杀成虫";
        if (disease.contains("草螟")) return "1. 清除田边杂草减少产卵场所 2. 使用氯虫苯甲酰胺或甲维盐防治 3. 灯光诱杀成虫 4. 适时晒田";
        if (disease.contains("秆蝇")) return "1. 适时早播避开产卵高峰 2. 使用吡虫啉拌种 3. 清除田间残株 4. 保护利用天敌";
        if (disease.contains("水蝇")) return "1. 保持田间适当水层 2. 使用敌百虫或毒死蜱防治 3. 清除受害茎秆 4. 合理施肥促进植株健壮";
        if (disease.contains("夜蛾")) return "1. 灯光诱杀成虫 2. 使用氯虫苯甲酰胺或甲维盐喷雾 3. 性诱剂诱杀雄虫 4. 保护利用天敌";
        if (disease.contains("蓟马")) return "1. 蓝板诱杀成虫 2. 使用多杀霉素或乙基多杀菌素防治 3. 清除田间杂草 4. 适时播种避开高峰期";
        // 病害类
        if (disease.contains("白叶枯")) return "1. 选用抗病品种 2. 及时排水晒田 3. 发病初期喷施噻唑锌或叶枯唑 4. 避免偏施氮肥";
        if (disease.contains("稻瘟病")) return "1. 选用抗病品种 2. 合理施肥避免偏施氮肥 3. 发病初期喷施三环唑或稻瘟灵 4. 收获后清除病残体";
        if (disease.contains("褐斑病")) return "1. 选用抗病品种 2. 平衡施肥避免偏施氮肥 3. 发病初期喷施稻瘟灵或咪鲜胺 4. 及时排水晒田";
        if (disease.contains("东格鲁")) return "1. 选用抗病品种 2. 防治传毒介体叶蝉和飞虱 3. 使用吡虫啉或噻虫嗪防治传毒昆虫 4. 及时拔除病株";
        // 草莓成熟度检测相关
        if (disease.contains("未成熟")) return "1. 草莓尚未成熟，建议继续观察 2. 保持适宜温度（15-25℃）和湿度 3. 待果实颜色转红后再采摘 4. 避免过早采摘影响口感";
        if (disease.contains("成熟")) return "1. 草莓已达最佳采摘期 2. 建议尽快采摘以保持最佳风味 3. 采摘后冷藏保存（0-4℃） 4. 运输过程避免挤压";
        if (disease.contains("过熟")) return "1. 草莓已过熟，建议尽快处理 2. 可用于制作果酱或加工产品 3. 避免长期储存 4. 及时采摘以免影响其他果实";
        return "1. 加强田间管理 2. 合理施肥浇水 3. 及时清除病残体 4. 必要时咨询当地农技部门";
    }
}
