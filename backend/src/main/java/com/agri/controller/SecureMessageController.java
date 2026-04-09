package com.agri.controller;

import com.agri.model.SecureMessage;
import com.agri.service.SecureMessageService;
import com.agri.utils.EncryptionUtils;
import com.agri.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.crypto.SecretKey;
import java.util.List;

/**
 * 安全消息控制器
 */
@RestController
@RequestMapping("/secure-message")
public class SecureMessageController {

    @Autowired
    private SecureMessageService secureMessageService;

    /**
     * 发送加密消息
     * @param secureMessage 安全消息
     * @param key 加密密钥（Base64格式）
     * @return 发送结果
     */
    @PostMapping
    public ResponseUtils.ApiResponse<SecureMessage> sendMessage(@RequestBody SecureMessage secureMessage, @RequestHeader(value = "X-Encryption-Key", required = false) String key) {
        try {
            // 加密消息内容
            if (key != null) {
                SecretKey secretKey = EncryptionUtils.getKeyFromString(key);
                String encryptedContent = EncryptionUtils.encrypt(secureMessage.getEncryptedContent(), secretKey);
                secureMessage.setEncryptedContent(encryptedContent);
            }
            
            boolean result = secureMessageService.sendMessage(secureMessage);
            if (result) {
                return ResponseUtils.success(secureMessage);
            } else {
                return ResponseUtils.error(500, "发送消息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 根据交易ID获取消息列表
     * @param transactionId 交易ID
     * @param key 解密密钥（Base64格式）
     * @return 消息列表
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseUtils.ApiResponse<List<SecureMessage>> getByTransactionId(@PathVariable("transactionId") Long transactionId, @RequestHeader(value = "X-Encryption-Key", required = false) String key) {
        try {
            List<SecureMessage> messageList = secureMessageService.getByTransactionId(transactionId);
            
            // 解密消息内容
            if (key != null) {
                SecretKey secretKey = EncryptionUtils.getKeyFromString(key);
                for (SecureMessage message : messageList) {
                    try {
                        String decryptedContent = EncryptionUtils.decrypt(message.getEncryptedContent(), secretKey);
                        message.setEncryptedContent(decryptedContent);
                    } catch (Exception e) {
                        // 解密失败，保持加密内容
                        e.printStackTrace();
                    }
                }
            }
            
            return ResponseUtils.success(messageList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据接收者ID获取未读消息列表
     * @param receiverId 接收者ID
     * @param key 解密密钥（Base64格式）
     * @return 消息列表
     */
    @GetMapping("/unread/{receiverId}")
    public ResponseUtils.ApiResponse<List<SecureMessage>> getUnreadMessages(@PathVariable("receiverId") Long receiverId, @RequestHeader(value = "X-Encryption-Key", required = false) String key) {
        try {
            List<SecureMessage> messageList = secureMessageService.getUnreadMessages(receiverId);
            
            // 解密消息内容
            if (key != null) {
                SecretKey secretKey = EncryptionUtils.getKeyFromString(key);
                for (SecureMessage message : messageList) {
                    try {
                        String decryptedContent = EncryptionUtils.decrypt(message.getEncryptedContent(), secretKey);
                        message.setEncryptedContent(decryptedContent);
                    } catch (Exception e) {
                        // 解密失败，保持加密内容
                        e.printStackTrace();
                    }
                }
            }
            
            return ResponseUtils.success(messageList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "获取未读消息失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     * @param id 消息ID
     * @return 标记结果
     */
    @PutMapping("/{id}/read")
    public ResponseUtils.ApiResponse<Boolean> markAsRead(@PathVariable("id") Long id) {
        try {
            boolean result = secureMessageService.markAsRead(id);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "标记消息为已读失败: " + e.getMessage());
        }
    }

    /**
     * 生成加密密钥
     * @return 密钥（Base64格式）
     */
    @GetMapping("/generate-key")
    public ResponseUtils.ApiResponse<String> generateKey() {
        try {
            SecretKey key = EncryptionUtils.generateKey();
            String keyString = EncryptionUtils.keyToString(key);
            return ResponseUtils.success(keyString);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "生成密钥失败: " + e.getMessage());
        }
    }

}
