package com.tma.job_fusion_backend.utils;

import com.tma.job_fusion_backend.exceptions.EncryptionException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private final SecretKeySpec secretKey;

    private final String ALGORITHM = "AES";

    public EncryptionUtil(@Value("${app.encryption-key:JobFusionDefaultSecretKey123}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] key16 = new byte[16];
        System.arraycopy(keyBytes, 0, key16, 0, Math.min(keyBytes.length, 16));
        this.secretKey = new SecretKeySpec(key16, ALGORITHM);
    }

    public String encrypt(String data) {
        if (StringUtils.isEmpty(data)) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new EncryptionException("Error encrypting data: " + e.getMessage(), e);
        }
    }

    public String decrypt(String encryptedData) {
        if (StringUtils.isEmpty(encryptedData)) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Error decrypting data: " + e.getMessage(), e);
        }
    }
}
