package com.agri.service.impl;

import com.agri.mapper.SecureMessageMapper;
import com.agri.model.SecureMessage;
import com.agri.service.SecureMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全消息服务实现类
 */
@Service
public class SecureMessageServiceImpl extends ServiceImpl<SecureMessageMapper, SecureMessage> implements SecureMessageService {

    @Autowired
    private SecureMessageMapper secureMessageMapper;

    @Override
    public boolean sendMessage(SecureMessage secureMessage) {
        secureMessage.setStatus(0); // 未读
        secureMessage.setCreatedAt(LocalDateTime.now());
        return save(secureMessage);
    }

    @Override
    public List<SecureMessage> getByTransactionId(Long transactionId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SecureMessage>()
                .eq("transaction_id", transactionId)
                .orderByAsc("created_at"));
    }

    @Override
    public List<SecureMessage> getUnreadMessages(Long receiverId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SecureMessage>()
                .eq("receiver_id", receiverId)
                .eq("status", 0)
                .orderByDesc("created_at"));
    }

    @Override
    public boolean markAsRead(Long id) {
        SecureMessage secureMessage = new SecureMessage();
        secureMessage.setId(id);
        secureMessage.setStatus(1); // 已读
        secureMessage.setReadAt(LocalDateTime.now());
        return updateById(secureMessage);
    }

}
