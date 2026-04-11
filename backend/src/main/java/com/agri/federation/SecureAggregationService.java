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

@Service
public class SecureAggregationService {
    
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    private final Map<String, KeyPair> taskKeyPairs = new ConcurrentHashMap<>();
    private final Map<String, Map<String, double[]>> clientMasks = new ConcurrentHashMap<>();
    private final Set<String> verifiedClients = ConcurrentHashMap.newKeySet();
    private final SecureRandom secureRandom = new SecureRandom();
    
    public KeyPair generateKeyPair(String taskId) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, secureRandom);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        taskKeyPairs.put(taskId, keyPair);
        return keyPair;
    }
    
    public String getPublicKey(String taskId) {
        KeyPair keyPair = taskKeyPairs.get(taskId);
        if (keyPair == null) return null;
        
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
    
    public boolean registerClient(String taskId, String clientId, String clientPublicKey) {
        verifiedClients.add(taskId + "_" + clientId);
        
        double[] mask = generateRandomMask(100);
        String key = taskId + "_" + clientId;
        
        clientMasks.computeIfAbsent(taskId, k -> new ConcurrentHashMap<>());
        clientMasks.get(taskId).put(clientId, mask);
        
        return true;
    }
    
    public double[] secureAggregate(String taskId, Map<String, double[]> encryptedUpdates) {
        Map<String, double[]> maskedUpdates = new HashMap<>();
        
        Map<String, double[]> masks = clientMasks.get(taskId);
        if (masks == null) return new double[0];
        
        for (Map.Entry<String, double[]> entry : encryptedUpdates.entrySet()) {
            String clientId = entry.getKey();
            double[] update = entry.getValue();
            double[] mask = masks.get(clientId);
            
            if (update != null && mask != null && update.length == mask.length) {
                double[] unmasked = new double[update.length];
                for (int i = 0; i < update.length; i++) {
                    unmasked[i] = update[i] - mask[i];
                }
                maskedUpdates.put(clientId, unmasked);
            }
        }
        
        return weightedAverage(maskedUpdates);
    }
    
    private double[] weightedAverage(Map<String, double[]> updates) {
        if (updates.isEmpty()) return new double[0];
        
        int paramCount = updates.values().iterator().next().length;
        double[] result = new double[paramCount];
        
        double weight = 1.0 / updates.size();
        
        for (double[] update : updates.values()) {
            for (int i = 0; i < paramCount; i++) {
                result[i] += update[i] * weight;
            }
        }
        
        return result;
    }
    
    private double[] generateRandomMask(int length) {
        double[] mask = new double[length];
        for (int i = 0; i < length; i++) {
            mask[i] = secureRandom.nextDouble() * 2 - 1;
        }
        return mask;
    }
    
    public SecretKey generateSymmetricKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, secureRandom);
        return keyGen.generateKey();
    }
    
    public byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        
        byte[] cipherText = cipher.doFinal(data);
        
        byte[] cipherTextWithIv = new byte[GCM_IV_LENGTH + cipherText.length];
        System.arraycopy(iv, 0, cipherTextWithIv, 0, GCM_IV_LENGTH);
        System.arraycopy(cipherText, 0, cipherTextWithIv, GCM_IV_LENGTH, cipherText.length);
        
        return cipherTextWithIv;
    }
    
    public byte[] decrypt(byte[] cipherTextWithIv, SecretKey key) throws Exception {
        if (cipherTextWithIv == null || cipherTextWithIv.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("无效的密文数据");
        }
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, cipherTextWithIv, 0, GCM_IV_LENGTH);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        
        return cipher.doFinal(cipherTextWithIv, GCM_IV_LENGTH, cipherTextWithIv.length - GCM_IV_LENGTH);
    }
    
    public void cleanup(String taskId) {
        taskKeyPairs.remove(taskId);
        clientMasks.remove(taskId);
        verifiedClients.removeIf(s -> s.startsWith(taskId + "_"));
    }
}
