package com.agri.service;

import com.agri.model.DataCirculation;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 数据流通服务接口
 */
public interface DataCirculationService extends IService<DataCirculation> {

    /**
     * 发布数据集交易信息
     * @param dataCirculation 数据流通信息
     * @return 发布结果
     */
    boolean publishData(DataCirculation dataCirculation);

    /**
     * 获取所有待交易的数据集
     * @return 数据集列表
     */
    List<DataCirculation> getAvailableData();

    /**
     * 根据发布者ID获取数据集列表
     * @param publisherId 发布者ID
     * @return 数据集列表
     */
    List<DataCirculation> getByPublisherId(Long publisherId);

    /**
     * 根据ID获取数据流通信息
     * @param id 数据流通ID
     * @return 数据流通信息
     */
    DataCirculation getById(Long id);

    /**
     * 更新数据流通状态
     * @param id 数据流通ID
     * @param status 状态
     * @return 更新结果
     */
    boolean updateStatus(Long id, Integer status);

}
