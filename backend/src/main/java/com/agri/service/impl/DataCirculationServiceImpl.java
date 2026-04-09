package com.agri.service.impl;

import com.agri.mapper.DataCirculationMapper;
import com.agri.model.DataCirculation;
import com.agri.service.DataCirculationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据流通服务实现类
 */
@Service
public class DataCirculationServiceImpl extends ServiceImpl<DataCirculationMapper, DataCirculation> implements DataCirculationService {

    @Autowired
    private DataCirculationMapper dataCirculationMapper;

    @Override
    public boolean publishData(DataCirculation dataCirculation) {
        dataCirculation.setStatus(0); // 待交易
        dataCirculation.setCreatedAt(LocalDateTime.now());
        dataCirculation.setUpdatedAt(LocalDateTime.now());
        return save(dataCirculation);
    }

    @Override
    public List<DataCirculation> getAvailableData() {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataCirculation>()
                .eq("status", 0)
                .orderByDesc("created_at"));
    }

    @Override
    public List<DataCirculation> getByPublisherId(Long publisherId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataCirculation>()
                .eq("publisher_id", publisherId)
                .orderByDesc("created_at"));
    }

    @Override
    public DataCirculation getById(Long id) {
        return super.getById(id);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        DataCirculation dataCirculation = new DataCirculation();
        dataCirculation.setId(id);
        dataCirculation.setStatus(status);
        dataCirculation.setUpdatedAt(LocalDateTime.now());
        return updateById(dataCirculation);
    }

}
