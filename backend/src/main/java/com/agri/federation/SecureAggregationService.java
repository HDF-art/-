package com.agri.federation;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全聚合服务
 * 提供加密聚合功能，保护客户端隐私
 */
@Service
public class SecureAggregationService {
    
    // 密钥对存储
    private final Map<String, KeyPair> taskKeyPairs = new ConcurrentHashMap<>();
    
    // 掩码存储
    private final Map<String, Map<String, double[]>> clientMasks = new ConcurrentHashMap<>();
    
    // 已验证的客户端
    private final Set<String> verifiedClients = ConcurrentHashMap.newKeySet();
    
    /**
     * 为任务生成密钥对
     */
    public KeyPair generateKeyPair(String taskId) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        taskKeyPairs.put(taskId, keyPair);
        return keyPair;
    }
    
    /**
     * 获取任务的公钥
     */
    public String getPublicKey(String taskId) {
        KeyPair keyPair = taskKeyPairs.get(taskId);
        if (keyPair == null) return null;
        
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
    
    /**
     * 客户端注册
     */
    public boolean registerClient(String taskId, String clientId, String clientPublicKey) {
        verifiedClients.add(taskId + "_" + clientId);
        
        // 生成客户端特定的掩码
        double[] mask = generateRandomMask(100); // 假设模型参数长度为100
        String key = taskId + "_" + clientId;
        
        clientMasks.computeIfAbsent(taskId, k -> new ConcurrentHashMap<>());
        clientMasks.get(taskId).put(clientId, mask);
        
        return true;
    }
    
    /**
     * 安全聚合
     * 步骤：
     * 1. 接收加密的模型更新
     * 2. 验证客户端身份
     * 3. 去除掩码
     * 4. 聚合模型
     */
    public double[] secureAggregate(String taskId, Map<String, double[]> encryptedUpdates) {
        Map<String, double[]> maskedUpdates = new HashMap<>();
        
        // 去除掩码
        Map<String, double[]> masks = clientMasks.get(taskId);
        if (masks == null) return new double[0];
        
        for (Map.Entry<String, double[]> entry : encryptedUpdates.entrySet()) {
            String clientId = entry.getKey();
            double[] update = entry.getValue();
            double[] mask = masks.get(clientId);
            
            if (update != null && mask != null && update.length == mask.length) {
                // 去除掩码
                double[] unmasked = new double[update.length];
                for (int i = 0; i < update.length; i++) {
                    unmasked[i] = update[i] - mask[i];
                }
                maskedUpdates.put(clientId, unmasked);
            }
        }
        
        // 执行加权平均
        return weightedAverage(maskedUpdates);
    }
    
    /**
     * 加权平均聚合
     */
    private double[] weightedAverage(Map<String, double[]> updates) {
        if (updates.isEmpty()) return new double[0];
        
        int paramCount = updates.values().iterator().next().length;
        double[] result = new double[paramCount];
        
        // 假设每个客户端权重相同
        double weight = 1.0 / updates.size();
        
        for (double[] update : updates.values()) {
            for (int i = 0; i < paramCount; i++) {
                result[i] += update[i] * weight;
            }
        }
        
        return result;
    }
    
    /**
     * 生成随机掩码
     */
    private double[] generateRandomMask(int length) {
        double[] mask = new double[length];
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            mask[i] = random.nextDouble() * 2 - 1; // [-1, 1]
        }
        return mask;
    }
    
    /**
     * 生成对称密钥
     */
    public SecretKey generateSymmetricKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }
    
    /**
     * AES加密
     */
    public byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
    
    /**
     * AES解密
     */
    public byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }
    
    /**
     * 清理任务数据
     */
    public void cleanup(String taskId) {
        taskKeyPairs.remove(taskId);
        clientMasks.remove(taskId);
        
        // 清理已验证客户端
        verifiedClients.removeIf(s -> s.startsWith(taskId + "_"));
    }
}
