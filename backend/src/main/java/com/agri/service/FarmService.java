package com.agri.service;

import com.agri.model.Farm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 农场服务接口
 */
public interface FarmService extends IService<Farm> {

    /**
     * 根据管理员ID查询农场
     * @param adminId 管理员ID
     * @return 农场信息
     */
    Farm findByAdminId(Long adminId);

    /**
     * 创建农场
     * @param farm 农场信息
     * @return 创建结果
     */
    boolean createFarm(Farm farm);

    /**
     * 更新农场
     * @param farm 农场信息
     * @return 更新结果
     */
    boolean updateFarm(Farm farm);

}