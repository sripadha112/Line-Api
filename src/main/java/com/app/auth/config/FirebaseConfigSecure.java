package com.app.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Credential validation class with support for multiple credential sources:
 * 1. Environment variable with file path (GOOGLE_APPLICATION_CREDENTIALS)
 * 2. Environment variable with base64 encoded credentials (FIREBASE_CREDENTIALS_BASE64)
 * 3. Classpath resource (firebase-service-account-key.json)
 */
@Configuration
public class FirebaseConfigSecure {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfigSecure.class);
    private static final String FIREBASE_SERVICE_ACCOUNT_KEY = "line-application-84af7-firebase-adminsdk-fbsvc-a8505ed76a.json";
    private static final String ENV_CREDENTIALS_PATH = "GOOGLE_APPLICATION_CREDENTIALS";
    private static final String ENV_CREDENTIALS_BASE64 = "FIREBASE_CREDENTIALS_BASE64";

    /**
     * Validate service account availability during startup.
     * Tries multiple sources in order of preference:
     * 1. GOOGLE_APPLICATION_CREDENTIALS environment variable (file path)
     * 2. FIREBASE_CREDENTIALS_BASE64 environment variable (base64 encoded JSON)
     * 3. Classpath resource (firebase-service-account-key.json)
     */
    @PostConstruct
    public void initialize() {
        try {
            loadCredentials();
            logger.info("Firebase credentials validated successfully (Expo-only mode)");
        } catch (Exception e) {
            logger.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    /**
     * Load credentials from various sources as a stream validation step.
     * @return true if credentials are readable
     * @throws IOException if credentials cannot be loaded
     */
    private boolean loadCredentials() throws IOException {
        // Option 1: Load from GOOGLE_APPLICATION_CREDENTIALS environment variable (file path)
        String credentialsPath = System.getenv(ENV_CREDENTIALS_PATH);
        if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
            logger.info("Loading Firebase credentials from environment variable path: {}", ENV_CREDENTIALS_PATH);
            Path path = Paths.get(credentialsPath);
            if (Files.exists(path)) {
                try (InputStream inputStream = new FileInputStream(credentialsPath)) {
                    inputStream.readNBytes(1);
                    return true;
                }
            } else {
                logger.warn("Credentials file not found at path: {}", credentialsPath);
            }
        }

        // Option 2: Load from FIREBASE_CREDENTIALS_BASE64 environment variable (base64 encoded)
        String credentialsBase64 = System.getenv(ENV_CREDENTIALS_BASE64);
        if (credentialsBase64 != null && !credentialsBase64.trim().isEmpty()) {
            logger.info("Loading Firebase credentials from base64 environment variable: {}", ENV_CREDENTIALS_BASE64);
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(credentialsBase64);
                try (InputStream inputStream = new ByteArrayInputStream(decodedBytes)) {
                    inputStream.readNBytes(1);
                    return true;
                }
            } catch (IllegalArgumentException e) {
                logger.error("Invalid base64 encoding in {}: {}", ENV_CREDENTIALS_BASE64, e.getMessage());
                throw new IOException("Invalid base64 credentials", e);
            }
        }

        // Option 3: Load from classpath resource (fallback)
        logger.info("Loading Firebase credentials from classpath resource: {}", FIREBASE_SERVICE_ACCOUNT_KEY);
        ClassPathResource serviceAccount = new ClassPathResource(FIREBASE_SERVICE_ACCOUNT_KEY);
        
        if (!serviceAccount.exists()) {
            String errorMessage = String.format(
                "Firebase credentials not found. Tried:\n" +
                "1. Environment variable %s (file path)\n" +
                "2. Environment variable %s (base64 encoded)\n" +
                "3. Classpath resource %s\n" +
                "Please configure at least one of these options.",
                ENV_CREDENTIALS_PATH, ENV_CREDENTIALS_BASE64, FIREBASE_SERVICE_ACCOUNT_KEY
            );
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        try (InputStream serviceAccountStream = serviceAccount.getInputStream()) {
            serviceAccountStream.readNBytes(1);
            return true;
        }
    }

    @Bean
    public String firebaseMode() {
        return "expo-only";
    }
}