package com.agri.service;

import com.agri.model.Dataset;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 数据集服务接口
 */
public interface DatasetService extends IService<Dataset> {

    /**
     * 上传数据集
     * @param dataset 数据集信息
     * @return 上传结果
     */
    boolean uploadDataset(Dataset dataset);

    /**
     * 根据上传者ID获取数据集列表
     * @param uploaderId 上传者ID
     * @return 数据集列表
     */
    List<Dataset> getByUploaderId(Long uploaderId);

    /**
     * 根据农场ID获取数据集列表
     * @param farmId 农场ID
     * @return 数据集列表
     */
    List<Dataset> getByFarmId(Long farmId);

    /**
     * 根据类型获取数据集列表
     * @param type 数据集类型
     * @return 数据集列表
     */
    List<Dataset> getByType(String type);

    /**
     * 删除数据集
     * @param id 数据集ID
     * @return 删除结果
     */
    boolean deleteDataset(Long id);

}
