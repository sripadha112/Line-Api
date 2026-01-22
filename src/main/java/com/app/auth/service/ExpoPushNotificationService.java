package com.app.auth.service;

import com.app.auth.dto.NotificationResponseDto;
import java.util.List;
import java.util.Map;

/**
 * Service for sending push notifications via Expo's Push Notification Service
 * This is an alternative to direct FCM that works better with Expo-built apps
 */
public interface ExpoPushNotificationService {

    /**
     * Send a push notification to a single Expo Push Token
     * 
     * @param expoPushToken The Expo Push Token (format: ExponentPushToken[xxx])
     * @param title Notification title
     * @param body Notification body
     * @return NotificationResponseDto with success/failure status
     */
    NotificationResponseDto sendNotification(String expoPushToken, String title, String body);

    /**
     * Send a push notification with custom data
     * 
     * @param expoPushToken The Expo Push Token
     * @param title Notification title
     * @param body Notification body
     * @param data Custom data payload
     * @return NotificationResponseDto with success/failure status
     */
    NotificationResponseDto sendNotification(String expoPushToken, String title, String body, Map<String, String> data);

    /**
     * Send push notifications to multiple Expo Push Tokens
     * 
     * @param expoPushTokens List of Expo Push Tokens
     * @param title Notification title
     * @param body Notification body
     * @return List of NotificationResponseDto for each token
     */
    List<NotificationResponseDto> sendBulkNotifications(List<String> expoPushTokens, String title, String body);

    /**
     * Check if a token is a valid Expo Push Token format
     * 
     * @param token The token to validate
     * @return true if token is in Expo Push Token format
     */
    boolean isExpoPushToken(String token);
}
