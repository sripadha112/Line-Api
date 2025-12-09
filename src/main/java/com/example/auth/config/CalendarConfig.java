package com.example.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for calendar integration
 */
@Configuration
@ConfigurationProperties(prefix = "app.calendar")
public class CalendarConfig {
    
    private Google google = new Google();
    private Apple apple = new Apple();
    
    public Google getGoogle() {
        return google;
    }
    
    public void setGoogle(Google google) {
        this.google = google;
    }
    
    public Apple getApple() {
        return apple;
    }
    
    public void setApple(Apple apple) {
        this.apple = apple;
    }
    
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String applicationName = "Line App Appointment Booking";
        
        public String getClientId() {
            return clientId;
        }
        
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
        
        public String getClientSecret() {
            return clientSecret;
        }
        
        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
        
        public String getRedirectUri() {
            return redirectUri;
        }
        
        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
        
        public String getApplicationName() {
            return applicationName;
        }
        
        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }
    }
    
    public static class Apple {
        private String teamId;
        private String keyId;
        private String privateKey;
        private String bundleId;
        
        public String getTeamId() {
            return teamId;
        }
        
        public void setTeamId(String teamId) {
            this.teamId = teamId;
        }
        
        public String getKeyId() {
            return keyId;
        }
        
        public void setKeyId(String keyId) {
            this.keyId = keyId;
        }
        
        public String getPrivateKey() {
            return privateKey;
        }
        
        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }
        
        public String getBundleId() {
            return bundleId;
        }
        
        public void setBundleId(String bundleId) {
            this.bundleId = bundleId;
        }
    }
}