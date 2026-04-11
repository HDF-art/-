package com.agri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.agri.mapper.LocalClientMapper;
import com.agri.model.LocalClient;
import com.agri.service.LocalClientService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class LocalClientServiceImpl extends ServiceImpl<LocalClientMapper, LocalClient> implements LocalClientService {

    @Override
    public LocalClient registerClient(Long userId, String clientId, Map<String, Object> info) {
        LocalClient client = getByClientId(clientId);
        if (client == null) {
            client = new LocalClient();
            client.setClientId(clientId);
        }
        
        client.setUserId(userId);
        client.setStatus("connected");
        client.setConnectedAt(LocalDateTime.now());
        client.setLastHeartbeat(LocalDateTime.now());
        
        if (info != null) {
            client.setPlatform((String) info.get("platform"));
            client.setPythonVersion((String) info.get("python_version"));
            client.setCpuCount((Integer) info.get("cpu_count"));
            client.setMemoryTotal(((Number) info.get("memory_total")).longValue());
            client.setDiskTotal(((Number) info.get("disk_total")).longValue());
            client.setDataDir((String) info.get("data_dir"));
            client.setDatasetsCount((Integer) info.get("datasets_count"));
            client.setRunningTasks((Integer) info.get("running_tasks"));
        }
        
        saveOrUpdate(client);
        return client;
    }

    @Override
    public void updateHeartbeat(String clientId) {
        LocalClient client = getByClientId(clientId);
        if (client != null) {
            client.setLastHeartbeat(LocalDateTime.now());
            updateById(client);
        }
    }

    @Override
    public void updateClientStatus(String clientId, String status) {
        LocalClient client = getByClientId(clientId);
        if (client != null) {
            client.setStatus(status);
            if ("disconnected".equals(status)) {
                client.setDisconnectedAt(LocalDateTime.now());
            }
            updateById(client);
        }
    }

    @Override
    public LocalClient getByClientId(String clientId) {
        return getOne(new LambdaQueryWrapper<LocalClient>()
                .eq(LocalClient::getClientId, clientId));
    }

    @Override
    public List<LocalClient> getByUserId(Long userId) {
        return list(new LambdaQueryWrapper<LocalClient>()
                .eq(LocalClient::getUserId, userId)
                .orderByDesc(LocalClient::getLastHeartbeat));
    }

    @Override
    public List<LocalClient> getConnectedClients() {
        return list(new LambdaQueryWrapper<LocalClient>()
                .eq(LocalClient::getStatus, "connected")
                .orderByDesc(LocalClient::getLastHeartbeat));
    }
}
