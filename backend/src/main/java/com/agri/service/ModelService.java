package com.agri.service;

import com.agri.model.Model;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 模型服务接口
 */
public interface ModelService extends IService<Model> {

    /**
     * 上传模型文件
     * @param file 模型文件
     * @param model 模型信息
     * @return 上传结果
     */
    boolean uploadModel(MultipartFile file, Model model);

    /**
     * 设置默认模型
     * @param modelId 模型ID
     * @return 设置结果
     */
    boolean setDefaultModel(Long modelId);

    /**
     * 获取默认模型
     * @return 默认模型
     */
    Model getDefaultModel();

    /**
     * 获取农场的个性化模型
     * @param farmId 农场ID
     * @return 模型列表
     */
    java.util.List<Model> getFarmModels(Long farmId);

}