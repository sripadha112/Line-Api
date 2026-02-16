package com.app.auth.service;

import com.app.auth.dto.NotificationRequestDto;
import com.app.auth.dto.NotificationResponseDto;

/**
 * Service interface for Expo Push Notification operations (Expo-only)
 */
public interface NotificationService {

    /**
     * Send a push notification to a specific device using Expo Push Token
     *
     * @param deviceToken Expo push token of the target device (ExponentPushToken[...])
     * @param title       Notification title
     * @param body        Notification body message
     * @return NotificationResponseDto containing the result of the operation
     */
    NotificationResponseDto sendNotificationToDevice(String deviceToken, String title, String body);

    /**
     * Send a push notification to a specific device using Expo Push Token with additional data
     *
     * @param notificationRequest Request object containing all notification details
     * @return NotificationResponseDto containing the result of the operation
     */
    NotificationResponseDto sendNotificationToDevice(NotificationRequestDto notificationRequest);

    /**
     * Send notification to multiple devices (Expo push tokens only)
     *
     * @param deviceTokens Array of Expo push tokens
     * @param title        Notification title
     * @param body         Notification body message
     * @return Array of NotificationResponseDto containing results for each device
     */
    NotificationResponseDto[] sendNotificationToMultipleDevices(String[] deviceTokens, String title, String body);
}