package com.app.auth.service.impl;

import com.app.auth.dto.NotificationResponseDto;
import com.app.auth.service.ExpoPushNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Implementation of ExpoPushNotificationService
 * Sends push notifications via Expo's Push Notification API
 * 
 * API Documentation: https://docs.expo.dev/push-notifications/sending-notifications/
 */
@Service
public class ExpoPushNotificationServiceImpl implements ExpoPushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ExpoPushNotificationServiceImpl.class);
    
    // Expo Push API endpoint
    private static final String EXPO_PUSH_API_URL = "https://exp.host/--/api/v2/push/send";
    
    private final RestTemplate restTemplate;

    public ExpoPushNotificationServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public NotificationResponseDto sendNotification(String expoPushToken, String title, String body) {
        return sendNotification(expoPushToken, title, body, null);
    }

    @Override
    public NotificationResponseDto sendNotification(String expoPushToken, String title, String body, Map<String, String> data) {
        logger.info("[EXPO PUSH] Sending notification to token: {}...", 
                   expoPushToken != null ? expoPushToken.substring(0, Math.min(30, expoPushToken.length())) : "null");

        // Validate token format
        if (!isExpoPushToken(expoPushToken)) {
            logger.error("[EXPO PUSH] Invalid Expo Push Token format: {}", expoPushToken);
            return NotificationResponseDto.error(
                "Invalid Expo Push Token format. Expected: ExponentPushToken[xxx]", 
                400, 
                expoPushToken
            );
        }

        try {
            // Build the notification payload
            Map<String, Object> notification = new HashMap<>();
            notification.put("to", expoPushToken);
            notification.put("title", title);
            notification.put("body", body);
            notification.put("sound", "default");
            notification.put("priority", "high");
            
            // Add channel for Android
            notification.put("channelId", "appointment_updates");
            
            // Add custom data if provided
            if (data != null && !data.isEmpty()) {
                notification.put("data", data);
            }

            logger.info("[EXPO PUSH] Payload: title='{}', body='{}'", title, body);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(notification, headers);

            // Send request to Expo Push API
            ResponseEntity<Map> response = restTemplate.exchange(
                EXPO_PUSH_API_URL,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );

            // Parse response - Expo returns { "data": [ { "status": "ok", "id": "xxx" } ] }
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Object dataField = responseBody.get("data");
                
                Map<String, Object> responseData = null;
                
                // Handle both array format (standard) and object format
                if (dataField instanceof List) {
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataField;
                    if (!dataList.isEmpty()) {
                        responseData = dataList.get(0);
                    }
                } else if (dataField instanceof Map) {
                    responseData = (Map<String, Object>) dataField;
                }
                
                if (responseData != null) {
                    String status = (String) responseData.get("status");
                    
                    if ("ok".equals(status)) {
                        String ticketId = (String) responseData.get("id");
                        logger.info("[EXPO PUSH] SUCCESS - Ticket ID: {}", ticketId);
                        return NotificationResponseDto.success(ticketId, expoPushToken);
                    } else {
                        // Error in delivery
                        String errorMessage = (String) responseData.get("message");
                        Object errorDetailsObj = responseData.get("details");
                        String errorDetails = errorDetailsObj != null ? errorDetailsObj.toString() : null;
                        logger.error("[EXPO PUSH] FAILED - Status: {}, Message: {}, Details: {}", 
                                    status, errorMessage, errorDetails);
                        
                        // Check for DeviceNotRegistered error - means token is invalid
                        if ("DeviceNotRegistered".equals(errorMessage) || 
                            (errorDetails != null && errorDetails.contains("DeviceNotRegistered"))) {
                            return NotificationResponseDto.error(
                                "Device not registered. Token is invalid or expired.", 
                                410, // Gone
                                expoPushToken
                            );
                        }
                        
                        return NotificationResponseDto.error(
                            errorMessage != null ? errorMessage : "Unknown error", 
                            400, 
                            expoPushToken
                        );
                    }
                }
            }

            logger.error("[EXPO PUSH] Unexpected response: {}", response);
            return NotificationResponseDto.error("Unexpected response from Expo Push API", 500, expoPushToken);

        } catch (Exception e) {
            logger.error("[EXPO PUSH] Exception while sending notification: {}", e.getMessage(), e);
            return NotificationResponseDto.error(
                "Failed to send notification: " + e.getMessage(), 
                500, 
                expoPushToken
            );
        }
    }

    @Override
    public List<NotificationResponseDto> sendBulkNotifications(List<String> expoPushTokens, String title, String body) {
        logger.info("[EXPO PUSH] Sending bulk notifications to {} tokens", expoPushTokens.size());

        List<NotificationResponseDto> responses = new ArrayList<>();

        // Expo supports sending up to 100 notifications in a single request
        // For simplicity, we'll send them one by one (can be optimized later)
        for (String token : expoPushTokens) {
            NotificationResponseDto response = sendNotification(token, title, body);
            responses.add(response);
        }

        long successCount = responses.stream().filter(NotificationResponseDto::isSuccess).count();
        logger.info("[EXPO PUSH] Bulk send complete. Success: {}/{}", successCount, expoPushTokens.size());

        return responses;
    }

    @Override
    public boolean isExpoPushToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        // Expo Push Token format: ExponentPushToken[xxx] or ExpoPushToken[xxx]
        return token.startsWith("ExponentPushToken[") || token.startsWith("ExpoPushToken[");
    }
}
