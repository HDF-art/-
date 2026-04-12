package com.agri.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final SecureRandom secureRandom = new SecureRandom();

    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(KEY_SIZE, secureRandom);
        return keyGenerator.generateKey();
    }

    public static SecretKey getKeyFromString(String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String message, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        
        byte[] cipherText = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        
        byte[] cipherTextWithIv = new byte[GCM_IV_LENGTH + cipherText.length];
        System.arraycopy(iv, 0, cipherTextWithIv, 0, GCM_IV_LENGTH);
        System.arraycopy(cipherText, 0, cipherTextWithIv, GCM_IV_LENGTH, cipherText.length);
        
        return Base64.getEncoder().encodeToString(cipherTextWithIv);
    }

    public static String decrypt(String encryptedMessage, SecretKey key) throws Exception {
        byte[] cipherTextWithIv = Base64.getDecoder().decode(encryptedMessage);
        
        if (cipherTextWithIv.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("无效的密文数据");
        }
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, cipherTextWithIv, 0, GCM_IV_LENGTH);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        
        byte[] decryptedBytes = cipher.doFinal(cipherTextWithIv, GCM_IV_LENGTH, cipherTextWithIv.length - GCM_IV_LENGTH);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
