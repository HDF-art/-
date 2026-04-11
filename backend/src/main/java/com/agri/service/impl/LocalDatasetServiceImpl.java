package com.agri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.agri.mapper.LocalDatasetMapper;
import com.agri.model.LocalDataset;
import com.agri.service.LocalDatasetService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LocalDatasetServiceImpl extends ServiceImpl<LocalDatasetMapper, LocalDataset> implements LocalDatasetService {

    @Override
    public List<LocalDataset> getByClientId(String clientId) {
        return list(new LambdaQueryWrapper<LocalDataset>()
                .eq(LocalDataset::getClientId, clientId)
                .orderByDesc(LocalDataset::getModifiedAt));
    }

    @Override
    public void syncDataset(LocalDataset dataset) {
        LocalDataset existing = getOne(new LambdaQueryWrapper<LocalDataset>()
                .eq(LocalDataset::getClientId, dataset.getClientId())
                .eq(LocalDataset::getPath, dataset.getPath()));
        
        if (existing != null) {
            dataset.setId(existing.getId());
        }
        
        dataset.setSyncedAt(LocalDateTime.now());
        saveOrUpdate(dataset);
    }

    @Override
    public void deleteByPath(String clientId, String path) {
        remove(new LambdaQueryWrapper<LocalDataset>()
                .eq(LocalDataset::getClientId, clientId)
                .eq(LocalDataset::getPath, path));
    }
}
