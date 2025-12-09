package com.example.auth.controller;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Google OAuth2 controller for calendar integration
 */
@RestController
@RequestMapping("/api/oauth2")
public class GoogleOAuth2Controller {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuth2Controller.class);
    
    @Value("${app.calendar.google.client-id}")
    private String clientId;
    
    @Value("${app.calendar.google.client-secret}")
    private String clientSecret;
    
    @Value("${app.calendar.google.redirect-uri}")
    private String redirectUri;
    
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    /**
     * Get Google OAuth2 authorization URL for calendar access
     */
    @GetMapping("/google/auth-url")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        logger.info("🔗 OAuth2: Received request for Google auth URL");
        logger.info("📝 OAuth2: Using clientId: {} and redirectUri: {}", 
                   clientId != null ? clientId.substring(0, Math.min(20, clientId.length())) + "..." : "null", 
                   redirectUri);
        
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    clientId,
                    clientSecret,
                    Collections.singletonList(CalendarScopes.CALENDAR)
            )
            .setAccessType("offline")
            .setApprovalPrompt("force")
            .build();
            
            String authUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .build();
            
            logger.info("✅ OAuth2: Successfully generated auth URL: {}", authUrl.substring(0, Math.min(100, authUrl.length())) + "...");
            
            Map<String, String> response = new HashMap<>();
            response.put("authUrl", authUrl);
            response.put("message", "Redirect user to this URL for Google Calendar authorization");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ OAuth2: Failed to generate auth URL", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate auth URL: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Handle OAuth2 callback and exchange authorization code for access token
     */
    @PostMapping("/google/callback")
    public ResponseEntity<Map<String, String>> handleGoogleCallback(@RequestBody Map<String, String> request) {
        logger.info("🔄 OAuth2: Received callback request");
        logger.info("📨 OAuth2: Request body keys: {}", request.keySet());
        
        try {
            String authorizationCode = request.get("code");
            if (authorizationCode == null) {
                logger.warn("⚠️ OAuth2: Missing authorization code in request");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authorization code is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            logger.info("🔑 OAuth2: Processing authorization code: {}...", 
                       authorizationCode.substring(0, Math.min(10, authorizationCode.length())));
            
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    clientId,
                    clientSecret,
                    Collections.singletonList(CalendarScopes.CALENDAR)
            )
            .setAccessType("offline")
            .build();
            
            // Exchange authorization code for access token
            var tokenResponse = flow.newTokenRequest(authorizationCode)
                    .setRedirectUri(redirectUri)
                    .execute();
            
            Credential credential = flow.createAndStoreCredential(tokenResponse, "user");
            
            logger.info("✅ OAuth2: Successfully obtained access token");
            logger.info("🔄 OAuth2: Access token exists: {}", credential.getAccessToken() != null);
            logger.info("🔄 OAuth2: Refresh token exists: {}", credential.getRefreshToken() != null);
            
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", credential.getAccessToken());
            response.put("refreshToken", credential.getRefreshToken());
            response.put("message", "Successfully obtained calendar access token");
            response.put("expiresIn", credential.getExpiresInSeconds() != null ? 
                        credential.getExpiresInSeconds().toString() : "3600");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ OAuth2: Failed to exchange code for token", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to exchange code for token: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/google/refresh")
    public ResponseEntity<Map<String, String>> refreshGoogleToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Refresh token is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    clientId,
                    clientSecret,
                    Collections.singletonList(CalendarScopes.CALENDAR)
            ).build();
            
            // Create credential with refresh token and refresh it
            Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setTokenServerEncodedUrl(flow.getTokenServerEncodedUrl())
                    .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
                    .build();
            
            credential.setRefreshToken(refreshToken);
            credential.refreshToken();
            
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", credential.getAccessToken());
            response.put("message", "Successfully refreshed access token");
            response.put("expiresIn", credential.getExpiresInSeconds() != null ? 
                        credential.getExpiresInSeconds().toString() : "3600");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to refresh token: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get OAuth2 configuration info for frontend
     */
    @GetMapping("/google/config")
    public ResponseEntity<Map<String, String>> getOAuth2Config() {
        logger.info("ℹ️ OAuth2: Configuration requested");
        
        Map<String, String> config = new HashMap<>();
        config.put("clientId", clientId);
        config.put("redirectUri", redirectUri);
        config.put("scope", CalendarScopes.CALENDAR);
        config.put("authUrl", "https://accounts.google.com/o/oauth2/auth");
        
        logger.info("✅ OAuth2: Configuration sent successfully");
        return ResponseEntity.ok(config);
    }
    
    /**
     * Test endpoint to verify OAuth2 controller is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        logger.info("🧪 TEST: OAuth2 test endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "working");
        response.put("message", "OAuth2 Controller is functioning properly");
        response.put("clientIdConfigured", clientId != null && !clientId.trim().isEmpty());
        response.put("clientSecretConfigured", clientSecret != null && !clientSecret.trim().isEmpty());
        response.put("redirectUriConfigured", redirectUri != null && !redirectUri.trim().isEmpty());
        response.put("timestamp", System.currentTimeMillis());
        
        logger.info("✅ TEST: OAuth2 controller test completed successfully");
        return ResponseEntity.ok(response);
    }
}