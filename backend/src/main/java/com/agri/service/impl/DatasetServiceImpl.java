package com.agri.service.impl;

import com.agri.mapper.DatasetMapper;
import com.agri.model.Dataset;
import com.agri.service.DatasetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据集服务实现类
 */
@Service
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset> implements DatasetService {

    @Autowired
    private DatasetMapper datasetMapper;

    @Override
    public boolean uploadDataset(Dataset dataset) {
        dataset.setStatus(0); // 未使用
        dataset.setCreatedAt(LocalDateTime.now());
        dataset.setUpdatedAt(LocalDateTime.now());
        return save(dataset);
    }

    @Override
    public List<Dataset> getByUploaderId(Long uploaderId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Dataset>()
                .eq("uploader_id", uploaderId)
                .orderByDesc("created_at"));
    }

    @Override
    public List<Dataset> getByFarmId(Long farmId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Dataset>()
                .eq("farm_id", farmId)
                .orderByDesc("created_at"));
    }

    @Override
    public List<Dataset> getByType(String type) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Dataset>()
                .eq("type", type)
                .orderByDesc("created_at"));
    }

    @Override
    public boolean deleteDataset(Long id) {
        return removeById(id);
    }

}
