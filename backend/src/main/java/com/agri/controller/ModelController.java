package com.agri.controller;

import com.agri.model.Model;
import com.agri.service.ModelService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.agri.utils.FileUploadValidator;

import java.util.List;

/**
 * 模型控制器
 */
@RestController
@RequestMapping("/models")
public class ModelController {

    @Autowired
    private ModelService modelService;
    
    @Autowired
    private FileUploadValidator fileUploadValidator;

    /**
     * 根据ID获取模型
     * @param modelId 模型ID
     * @return 模型信息
     */
    @GetMapping("/{id}")
    public Model getModelById(@PathVariable("id") Long modelId) {
        return modelService.getById(modelId);
    }

    /**
     * 分页获取模型列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping
    public Page<Model> getModelList(@RequestParam(defaultValue = "1") Integer page, 
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return modelService.page(new Page<>(page, pageSize));
    }

    /**
     * 获取农场的个性化模型
     * @param farmId 农场ID
     * @return 模型列表
     */
    @GetMapping("/farm/{farmId}")
    public List<Model> getFarmModels(@PathVariable Long farmId) {
        return modelService.getFarmModels(farmId);
    }

    /**
     * 获取默认模型
     * @return 默认模型
     */
    @GetMapping("/default")
    public Model getDefaultModel() {
        return modelService.getDefaultModel();
    }

    /**
     * 上传模型文件
     * @param file 模型文件
     * @param model 模型信息
     * @return 上传结果
     */
    @PostMapping("/upload")
    public boolean uploadModel(@RequestPart("file") MultipartFile file, 
                             @RequestPart("model") Model model) {
        // 验证模型文件
        String error = fileUploadValidator.validateModelFile(file);
        if (error != null) {
            throw new RuntimeException(error);
        }
        return modelService.uploadModel(file, model);
    }

    /**
     * 设置默认模型
     * @param modelId 模型ID
     * @return 设置结果
     */
    @PostMapping("/{id}/set-default")
    public boolean setDefaultModel(@PathVariable("id") Long modelId) {
        return modelService.setDefaultModel(modelId);
    }

    /**
     * 删除模型
     * @param modelId 模型ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteModel(@PathVariable("id") Long modelId) {
        return modelService.removeById(modelId);
    }

}