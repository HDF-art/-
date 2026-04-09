package com.agri.service;

import com.agri.model.SecureMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 安全消息服务接口
 */
public interface SecureMessageService extends IService<SecureMessage> {

    /**
     * 发送加密消息
     * @param secureMessage 安全消息
     * @return 发送结果
     */
    boolean sendMessage(SecureMessage secureMessage);

    /**
     * 根据交易ID获取消息列表
     * @param transactionId 交易ID
     * @return 消息列表
     */
    List<SecureMessage> getByTransactionId(Long transactionId);

    /**
     * 根据接收者ID获取未读消息列表
     * @param receiverId 接收者ID
     * @return 消息列表
     */
    List<SecureMessage> getUnreadMessages(Long receiverId);

    /**
     * 标记消息为已读
     * @param id 消息ID
     * @return 标记结果
     */
    boolean markAsRead(Long id);

}
