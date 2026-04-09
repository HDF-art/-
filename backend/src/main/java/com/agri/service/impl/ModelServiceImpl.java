package com.agri.service.impl;

import com.agri.mapper.ModelMapper;
import com.agri.model.Model;
import com.agri.service.ModelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 模型服务实现类
 */
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {

    @Autowired
    private ModelMapper modelMapper;

    @Value("${model.save-path}")
    private String modelSavePath;

    @Override
    @Transactional
    public boolean uploadModel(MultipartFile file, Model model) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 确保目录存在
        File dir = new File(modelSavePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String fileExt = StringUtils.getFilenameExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + fileExt;
        String filePath = modelSavePath + fileName;

        // 保存文件
        try {
            file.transferTo(new File(filePath));
            model.setFilePath(filePath);
            model.setCreatedAt(LocalDateTime.now());
            return save(model);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }

    @Override
    @Transactional
    public boolean setDefaultModel(Long modelId) {
        // 先将所有模型设置为非默认
        update().set("is_default", 0).update();
        
        // 设置指定模型为默认
        return update().set("is_default", 1).eq("id", modelId).update();
    }

    @Override
    public Model getDefaultModel() {
        return modelMapper.findDefaultModel();
    }

    @Override
    public List<Model> getFarmModels(Long farmId) {
        return modelMapper.findByFarmId(farmId);
    }
}