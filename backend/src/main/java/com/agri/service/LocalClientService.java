package com.agri.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.agri.model.LocalClient;
import java.util.List;
import java.util.Map;

public interface LocalClientService extends IService<LocalClient> {
    
    LocalClient registerClient(Long userId, String clientId, Map<String, Object> info);
    
    void updateHeartbeat(String clientId);
    
    void updateClientStatus(String clientId, String status);
    
    LocalClient getByClientId(String clientId);
    
    List<LocalClient> getByUserId(Long userId);
    
    List<LocalClient> getConnectedClients();
}
