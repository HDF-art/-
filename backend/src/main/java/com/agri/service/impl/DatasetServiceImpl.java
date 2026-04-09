package com.agri.service.impl;

import com.agri.mapper.DatasetMapper;
import com.agri.model.Dataset;
import com.agri.service.DatasetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
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
    @CacheEvict(value = "datasets", allEntries = true)
    public boolean uploadDataset(Dataset dataset) {
        dataset.setStatus(0); // 未使用
        dataset.setCreatedAt(LocalDateTime.now());
        dataset.setUpdatedAt(LocalDateTime.now());
        return save(dataset);
    }

    @Override
    @Cacheable(value = "datasets", key = "'uploader:' + #uploaderId")
    public List<Dataset> getByUploaderId(Long uploaderId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Dataset>()
                .eq("uploader_id", uploaderId)
                .orderByDesc("created_at"));
    }

    @Override
    @Cacheable(value = "datasets", key = "'farm:' + #farmId")
    public List<Dataset> getByFarmId(Long farmId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Dataset>()
                .eq("farm_id", farmId)
                .orderByDesc("created_at"));
    }

    @Override
    @Cacheable(value = "datasets", key = "'type:' + #type")
    public List<Dataset> getByType(String type) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Dataset>()
                .eq("type", type)
                .orderByDesc("created_at"));
    }

    @Override
    @CacheEvict(value = "datasets", allEntries = true)
    public boolean deleteDataset(Long id) {
        return removeById(id);
    }

}
