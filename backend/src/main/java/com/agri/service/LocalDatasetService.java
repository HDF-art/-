package com.agri.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.agri.model.LocalDataset;
import java.util.List;

public interface LocalDatasetService extends IService<LocalDataset> {
    
    List<LocalDataset> getByClientId(String clientId);
    
    void syncDataset(LocalDataset dataset);
    
    void deleteByPath(String clientId, String path);
}
