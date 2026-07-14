package com.app.auth.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA converter for encrypting/decrypting sensitive string fields at application level.
 *
 * Key source (required in production): APP_MEDICAL_DATA_KEY_BASE64
 * - Base64 encoded 32-byte key for AES-256
 *
 * Storage format: ENCv1:{base64(iv + ciphertext)}
 */
@Converter(autoApply = false)
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String PREFIX = "ENCv1:";
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LENGTH = 12;

    private static final SecretKeySpec KEY = loadKey();
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        // Avoid double encryption on already encrypted values
        if (attribute.startsWith(PREFIX)) {
            return attribute;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt medical field", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }

        // Backward compatibility: old/plain rows read as-is
        if (!dbData.startsWith(PREFIX)) {
            return dbData;
        }

        try {
            String payload = dbData.substring(PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(payload);

            if (combined.length <= IV_LENGTH) {
                throw new IllegalArgumentException("Encrypted payload too short");
            }

            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt medical field", e);
        }
    }

    private static SecretKeySpec loadKey() {
        String keyB64 = System.getenv("APP_MEDICAL_DATA_KEY_BASE64");
        if (keyB64 == null || keyB64.isBlank()) {
            throw new IllegalStateException(
                "APP_MEDICAL_DATA_KEY_BASE64 is required for medical-data encryption"
            );
        }

        byte[] key = Base64.getDecoder().decode(keyB64);
        if (key.length != 32) {
            throw new IllegalStateException(
                "APP_MEDICAL_DATA_KEY_BASE64 must decode to exactly 32 bytes (AES-256 key)"
            );
        }

        return new SecretKeySpec(key, "AES");
    }
}
