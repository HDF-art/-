package com.agri.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 加密工具类
 */
public class EncryptionUtils {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128;

    /**
     * 生成密钥
     * @return 密钥
     * @throws Exception 异常
     */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }

    /**
     * 从Base64字符串生成密钥
     * @param keyString Base64格式的密钥
     * @return 密钥
     * @throws Exception 异常
     */
    public static SecretKey getKeyFromString(String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 加密消息
     * @param message 原始消息
     * @param key 密钥
     * @return 加密后的消息（Base64格式）
     * @throws Exception 异常
     */
    public static String encrypt(String message, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 解密消息
     * @param encryptedMessage 加密后的消息（Base64格式）
     * @param key 密钥
     * @return 解密后的消息
     * @throws Exception 异常
     */
    public static String decrypt(String encryptedMessage, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    /**
     * 将密钥转换为Base64字符串
     * @param key 密钥
     * @return Base64格式的密钥
     */
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

}
