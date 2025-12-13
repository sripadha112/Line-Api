package com.example.auth.service;

import com.example.auth.dto.NotificationRequestDto;
import com.example.auth.dto.NotificationResponseDto;

/**
 * Service interface for Firebase Cloud Messaging operations
 */
public interface NotificationService {

    /**
     * Send a push notification to a specific device using FCM
     *
     * @param deviceToken FCM device token of the target device
     * @param title       Notification title
     * @param body        Notification body message
     * @return NotificationResponseDto containing the result of the operation
     */
    NotificationResponseDto sendNotificationToDevice(String deviceToken, String title, String body);

    /**
     * Send a push notification to a specific device using FCM with additional data
     *
     * @param notificationRequest Request object containing all notification details
     * @return NotificationResponseDto containing the result of the operation
     */
    NotificationResponseDto sendNotificationToDevice(NotificationRequestDto notificationRequest);

    /**
     * Send notification to multiple devices
     *
     * @param deviceTokens Array of FCM device tokens
     * @param title        Notification title
     * @param body         Notification body message
     * @return Array of NotificationResponseDto containing results for each device
     */
    NotificationResponseDto[] sendNotificationToMultipleDevices(String[] deviceTokens, String title, String body);
}