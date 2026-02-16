package com.app.auth.service.impl;

import com.app.auth.dto.*;
import com.app.auth.service.ExpoPushNotificationService;
import com.app.auth.service.NotificationService;
// Removed Firebase Admin SDK imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of NotificationService using Firebase Cloud Messaging
 * Now also supports Expo Push Notifications for Expo Push Tokens
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    @Autowired
    private ExpoPushNotificationService expoPushNotificationService;

    @Override
    public NotificationResponseDto sendNotificationToDevice(String deviceToken, String title, String body) {
        logger.info("Sending notification to device with token: {}...", 
                   deviceToken != null ? deviceToken.substring(0, Math.min(deviceToken.length(), 20)) : "null");

        if (!StringUtils.hasText(deviceToken)) {
            logger.error("Device token is null or empty");
            return NotificationResponseDto.error("Device token is required", 400, deviceToken);
        }

        // Only allow Expo Push Tokens
        if (expoPushNotificationService.isExpoPushToken(deviceToken)) {
            logger.info("[EXPO] Routing to Expo Push Notification Service");
            return expoPushNotificationService.sendNotification(deviceToken, title, body);
        } else {
            logger.error("Non-Expo push tokens are not supported. Token: {}", deviceToken);
            return NotificationResponseDto.error("Only Expo push tokens are supported in this deployment.", 400, deviceToken);
        }
    }

    @Override
    public NotificationResponseDto sendNotificationToDevice(NotificationRequestDto notificationRequest) {
        logger.info("Sending notification with request: {}", notificationRequest);

        if (notificationRequest == null) {
            logger.error("Notification request is null");
            return NotificationResponseDto.error("Notification request is required", 400, null);
        }

        String deviceToken = notificationRequest.getDeviceToken();
        
        // Only allow Expo Push Tokens
        if (deviceToken != null && expoPushNotificationService.isExpoPushToken(deviceToken)) {
            logger.info("[EXPO] Routing to Expo Push Notification Service");
            Map<String, String> data = notificationRequest.getData();
            return expoPushNotificationService.sendNotification(
                deviceToken, 
                notificationRequest.getTitle(), 
                notificationRequest.getBody(),
                data
            );
        } else {
            logger.error("Non-Expo push tokens are not supported. Token: {}", deviceToken);
            return NotificationResponseDto.error("Only Expo push tokens are supported in this deployment.", 400, deviceToken);
        }
    }

    @Override
    public NotificationResponseDto[] sendNotificationToMultipleDevices(String[] deviceTokens, String title, String body) {
        logger.info("Sending notification to {} devices", deviceTokens != null ? deviceTokens.length : 0);

        if (deviceTokens == null || deviceTokens.length == 0) {
            logger.error("Device tokens array is null or empty");
            return new NotificationResponseDto[]{
                NotificationResponseDto.error("Device tokens are required", 400, null)
            };
        }

        List<NotificationResponseDto> responses = new ArrayList<>();
        List<CompletableFuture<NotificationResponseDto>> futures = new ArrayList<>();

        // Send notifications asynchronously for better performance
        for (String deviceToken : deviceTokens) {
            CompletableFuture<NotificationResponseDto> future = CompletableFuture.supplyAsync(() ->
                sendNotificationToDevice(deviceToken, title, body)
            );
            futures.add(future);
        }

        // Wait for all notifications to complete
        for (CompletableFuture<NotificationResponseDto> future : futures) {
            try {
                responses.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error waiting for notification completion: {}", e.getMessage(), e);
                responses.add(NotificationResponseDto.error("Failed to process notification: " + e.getMessage(), 500, null));
            }
        }

        logger.info("Completed sending notifications to {} devices. Success: {}, Errors: {}", 
                   deviceTokens.length,
                   responses.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum(),
                   responses.stream().mapToInt(r -> r.isSuccess() ? 0 : 1).sum());

        return responses.toArray(new NotificationResponseDto[0]);
    }

    /**
     * Maps Firebase messaging error codes to appropriate HTTP status codes
     * 
     * @param errorCode Firebase messaging error code
     * @return HTTP status code
     */
    // Removed Firebase error code mapping, not needed for Expo only

    /**
     * Attempts to detect the platform from the FCM token structure
     * Note: This is a heuristic approach and may not be 100% accurate
     * 
     * @param deviceToken FCM device token
     * @return Detected platform or ANDROID as default
     */
    // Removed platform detection, not needed for Expo only

    /**
     * Configures platform-specific notification settings
     * 
     * @param messageBuilder Message builder to configure
     * @param request Notification request with platform configurations
     * @param platform Target platform
     */
    // Removed platform-specific config, not needed for Expo only

    /**
     * Configures iOS-specific APNS settings
     * 
     * @param messageBuilder Message builder to configure
     * @param request Notification request with iOS configuration
     */
    // Removed iOS config, not needed for Expo only

    /**
     * Configures Android-specific notification settings
     * 
     * @param messageBuilder Message builder to configure
     * @param request Notification request with Android configuration
     */
    // Removed Android config, not needed for Expo only

    /**
     * Maps string priority to AndroidConfig.Priority enum
     * 
     * @param priority Priority string
     * @return AndroidConfig.Priority enum value
     */
    // Removed Android priority mapping, not needed for Expo only
}