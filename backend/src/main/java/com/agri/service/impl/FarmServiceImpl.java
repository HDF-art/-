package com.agri.service.impl;

import com.agri.mapper.FarmMapper;
import com.agri.model.Farm;
import com.agri.service.FarmService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

/**
 * 农场服务实现类
 */
@Service
public class FarmServiceImpl extends ServiceImpl<FarmMapper, Farm> implements FarmService {

    @Autowired
    private FarmMapper farmMapper;

    @Override
    @Cacheable(value = "farms", key = "'adminId:' + #adminId")
    public Farm findByAdminId(Long adminId) {
        return farmMapper.findByAdminId(adminId);
    }

    @Override
    @CacheEvict(value = "farms", allEntries = true)
    public boolean createFarm(Farm farm) {
        farm.setCreatedAt(java.time.LocalDateTime.now());
        farm.setUpdatedAt(java.time.LocalDateTime.now());
        return save(farm);
    }

    @Override
    @CacheEvict(value = "farms", allEntries = true)
    public boolean updateFarm(Farm farm) {
        farm.setUpdatedAt(java.time.LocalDateTime.now());
        return updateById(farm);
    }
}