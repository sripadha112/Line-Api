package com.app.auth.service.impl;

import com.app.auth.dto.*;
import com.app.auth.service.NotificationService;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of NotificationService using Firebase Cloud Messaging
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public NotificationResponseDto sendNotificationToDevice(String deviceToken, String title, String body) {
        logger.info("Sending notification to device with token: {}...", 
                   deviceToken != null ? deviceToken.substring(0, Math.min(deviceToken.length(), 10)) : "null");

        if (!StringUtils.hasText(deviceToken)) {
            logger.error("Device token is null or empty");
            return NotificationResponseDto.error("Device token is required", 400, deviceToken);
        }

        if (!StringUtils.hasText(title)) {
            logger.error("Notification title is null or empty");
            return NotificationResponseDto.error("Title is required", 400, deviceToken);
        }

        if (!StringUtils.hasText(body)) {
            logger.error("Notification body is null or empty");
            return NotificationResponseDto.error("Body is required", 400, deviceToken);
        }

        try {
            // Build the notification
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Build the message
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification)
                    .build();

            // Send the message
            String response = FirebaseMessaging.getInstance().send(message);
            
            logger.info("Successfully sent notification. Message ID: {}", response);
            return NotificationResponseDto.success(response, deviceToken);

        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send notification to device token {}: {}", 
                        deviceToken.substring(0, Math.min(deviceToken.length(), 10)), e.getMessage(), e);
            
            // Map Firebase error codes to appropriate HTTP status codes
            int statusCode = mapFirebaseErrorToHttpStatus(e.getMessagingErrorCode());
            return NotificationResponseDto.error(e.getMessage(), statusCode, deviceToken);
            
        } catch (Exception e) {
            logger.error("Unexpected error while sending notification: {}", e.getMessage(), e);
            return NotificationResponseDto.error("Internal server error: " + e.getMessage(), 500, deviceToken);
        }
    }

    @Override
    public NotificationResponseDto sendNotificationToDevice(NotificationRequestDto notificationRequest) {
        logger.info("Sending notification with request: {}", notificationRequest);

        if (notificationRequest == null) {
            logger.error("Notification request is null");
            return NotificationResponseDto.error("Notification request is required", 400, null);
        }

        try {
            // Detect platform if auto-detect is enabled
            Platform targetPlatform = notificationRequest.getPlatform();
            if (targetPlatform == Platform.AUTO_DETECT) {
                targetPlatform = detectPlatformFromToken(notificationRequest.getDeviceToken());
            }

            // Build the notification
            Notification.Builder notificationBuilder = Notification.builder()
                    .setTitle(notificationRequest.getTitle())
                    .setBody(notificationRequest.getBody());

            // Add image if provided
            if (StringUtils.hasText(notificationRequest.getImageUrl())) {
                notificationBuilder.setImage(notificationRequest.getImageUrl());
            }

            Notification notification = notificationBuilder.build();

            // Build the message with platform-specific configurations
            Message.Builder messageBuilder = Message.builder()
                    .setToken(notificationRequest.getDeviceToken())
                    .setNotification(notification);

            // Add custom data if provided
            if (notificationRequest.getData() != null && !notificationRequest.getData().isEmpty()) {
                messageBuilder.putAllData(notificationRequest.getData());
            }

            // Configure platform-specific settings
            configurePlatformSpecificSettings(messageBuilder, notificationRequest, targetPlatform);

            Message message = messageBuilder.build();

            // Send the message
            String response = FirebaseMessaging.getInstance().send(message);
            
            logger.info("Successfully sent notification with data. Message ID: {}", response);
            return NotificationResponseDto.success(response, notificationRequest.getDeviceToken());

        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send notification: {}", e.getMessage(), e);
            int statusCode = mapFirebaseErrorToHttpStatus(e.getMessagingErrorCode());
            return NotificationResponseDto.error(e.getMessage(), statusCode, notificationRequest.getDeviceToken());
            
        } catch (Exception e) {
            logger.error("Unexpected error while sending notification: {}", e.getMessage(), e);
            return NotificationResponseDto.error("Internal server error: " + e.getMessage(), 500, notificationRequest.getDeviceToken());
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
    private int mapFirebaseErrorToHttpStatus(MessagingErrorCode errorCode) {
        if (errorCode == null) {
            return 500;
        }

        switch (errorCode) {
            case INVALID_ARGUMENT:
            case UNREGISTERED:
                return 400; // Bad Request
            case SENDER_ID_MISMATCH:
            case THIRD_PARTY_AUTH_ERROR:
                return 401; // Unauthorized
            case QUOTA_EXCEEDED:
                return 429; // Too Many Requests
            case UNAVAILABLE:
            case INTERNAL:
                return 503; // Service Unavailable
            default:
                return 500; // Internal Server Error
        }
    }

    /**
     * Attempts to detect the platform from the FCM token structure
     * Note: This is a heuristic approach and may not be 100% accurate
     * 
     * @param deviceToken FCM device token
     * @return Detected platform or ANDROID as default
     */
    private Platform detectPlatformFromToken(String deviceToken) {
        if (!StringUtils.hasText(deviceToken)) {
            logger.warn("Cannot detect platform from empty token, defaulting to Android");
            return Platform.ANDROID;
        }

        // iOS FCM tokens are typically longer and have specific patterns
        // This is a heuristic approach and may need adjustment based on actual token patterns
        if (deviceToken.length() > 150) {
            logger.debug("Token length suggests iOS device");
            return Platform.IOS;
        } else {
            logger.debug("Token length suggests Android device");
            return Platform.ANDROID;
        }
    }

    /**
     * Configures platform-specific notification settings
     * 
     * @param messageBuilder Message builder to configure
     * @param request Notification request with platform configurations
     * @param platform Target platform
     */
    private void configurePlatformSpecificSettings(Message.Builder messageBuilder, 
                                                 NotificationRequestDto request, 
                                                 Platform platform) {
        logger.debug("Configuring platform-specific settings for: {}", platform);

        if (platform == Platform.IOS) {
            configureIOSSettings(messageBuilder, request);
        } else if (platform == Platform.ANDROID) {
            configureAndroidSettings(messageBuilder, request);
        }

        // Set high priority if requested
        if (Boolean.TRUE.equals(request.getHighPriority())) {
            logger.debug("Setting high priority for immediate delivery");
            if (platform == Platform.ANDROID) {
                // High priority is handled in Android config
            } else {
                // For iOS, we use APNS push type
            }
        }
    }

    /**
     * Configures iOS-specific APNS settings
     * 
     * @param messageBuilder Message builder to configure
     * @param request Notification request with iOS configuration
     */
    private void configureIOSSettings(Message.Builder messageBuilder, NotificationRequestDto request) {
        logger.debug("Configuring iOS APNS settings");

        ApnsConfig.Builder apnsBuilder = ApnsConfig.builder();
        Aps.Builder apsBuilder = Aps.builder();

        IOSConfigDto iosConfig = request.getIosConfig();
        
        if (iosConfig != null) {
            // Configure sound
            if (StringUtils.hasText(iosConfig.getSound())) {
                apsBuilder.setSound(iosConfig.getSound());
            } else {
                apsBuilder.setSound("default");
            }

            // Configure badge
            if (iosConfig.getBadge() != null) {
                apsBuilder.setBadge(iosConfig.getBadge());
            }

            // Configure content-available for background processing
            if (Boolean.TRUE.equals(iosConfig.getContentAvailable())) {
                apsBuilder.setContentAvailable(true);
            }

            // Configure mutable-content for notification service extensions
            if (Boolean.TRUE.equals(iosConfig.getMutableContent())) {
                apsBuilder.setMutableContent(true);
            }

            // Configure category
            if (StringUtils.hasText(iosConfig.getCategory())) {
                apsBuilder.setCategory(iosConfig.getCategory());
            }

            // Configure thread ID
            if (StringUtils.hasText(iosConfig.getThreadId())) {
                apsBuilder.setThreadId(iosConfig.getThreadId());
            }
        } else {
            // Default iOS configuration
            apsBuilder.setSound("default");
        }

        // Set high priority for iOS if requested
        if (Boolean.TRUE.equals(request.getHighPriority())) {
            apnsBuilder.putHeader("apns-priority", "10");
            apnsBuilder.putHeader("apns-push-type", "alert");
        }

        apnsBuilder.setAps(apsBuilder.build());
        messageBuilder.setApnsConfig(apnsBuilder.build());
    }

    /**
     * Configures Android-specific notification settings
     * 
     * @param messageBuilder Message builder to configure
     * @param request Notification request with Android configuration
     */
    private void configureAndroidSettings(Message.Builder messageBuilder, NotificationRequestDto request) {
        logger.debug("Configuring Android notification settings");

        AndroidConfig.Builder androidBuilder = AndroidConfig.builder();
        AndroidNotification.Builder notificationBuilder = AndroidNotification.builder();

        AndroidConfigDto androidConfig = request.getAndroidConfig();

        if (androidConfig != null) {
            // Configure notification channel
            if (StringUtils.hasText(androidConfig.getChannelId())) {
                notificationBuilder.setChannelId(androidConfig.getChannelId());
            } else {
                notificationBuilder.setChannelId("default");
            }

            // Configure priority
            if (StringUtils.hasText(androidConfig.getPriority())) {
                AndroidConfig.Priority priority = mapAndroidPriority(androidConfig.getPriority());
                androidBuilder.setPriority(priority);
            } else {
                androidBuilder.setPriority(AndroidConfig.Priority.HIGH);
            }

            // Configure sound
            if (StringUtils.hasText(androidConfig.getSound())) {
                notificationBuilder.setSound(androidConfig.getSound());
            }

            // Configure icon
            if (StringUtils.hasText(androidConfig.getIcon())) {
                notificationBuilder.setIcon(androidConfig.getIcon());
            }

            // Configure color
            if (StringUtils.hasText(androidConfig.getColor())) {
                notificationBuilder.setColor(androidConfig.getColor());
            }

            // Configure tag
            if (StringUtils.hasText(androidConfig.getTag())) {
                notificationBuilder.setTag(androidConfig.getTag());
            }

            // Configure click action
            if (StringUtils.hasText(androidConfig.getClickAction())) {
                notificationBuilder.setClickAction(androidConfig.getClickAction());
            }

            // Configure TTL
            if (androidConfig.getTtl() != null) {
                androidBuilder.setTtl(androidConfig.getTtl() * 1000); // Convert to milliseconds
            }

            // Configure restricted package name
            if (StringUtils.hasText(androidConfig.getRestrictedPackageName())) {
                androidBuilder.setRestrictedPackageName(androidConfig.getRestrictedPackageName());
            }
        } else {
            // Default Android configuration
            notificationBuilder.setChannelId("default");
            androidBuilder.setPriority(AndroidConfig.Priority.HIGH);
        }

        // Set high priority for Android if requested
        if (Boolean.TRUE.equals(request.getHighPriority())) {
            androidBuilder.setPriority(AndroidConfig.Priority.HIGH);
        }

        androidBuilder.setNotification(notificationBuilder.build());
        messageBuilder.setAndroidConfig(androidBuilder.build());
    }

    /**
     * Maps string priority to AndroidConfig.Priority enum
     * 
     * @param priority Priority string
     * @return AndroidConfig.Priority enum value
     */
    private AndroidConfig.Priority mapAndroidPriority(String priority) {
        if (priority == null) {
            return AndroidConfig.Priority.NORMAL;
        }

        switch (priority.toLowerCase()) {
            case "high":
                return AndroidConfig.Priority.HIGH;
            case "normal":
                return AndroidConfig.Priority.NORMAL;
            default:
                logger.warn("Unknown Android priority: {}, defaulting to NORMAL", priority);
                return AndroidConfig.Priority.NORMAL;
        }
    }
}