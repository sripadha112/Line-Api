package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

/**
 * REST Controller for Firebase Cloud Messaging notification operations
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Firebase Cloud Messaging notification operations")
@Validated
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Send push notification to a single device
     *
     * @param notificationRequest Request containing notification details
     * @return Response containing the result of the notification operation
     */
    @PostMapping("/notify")
    @Operation(summary = "Send push notification to a device", 
               description = "Sends a Firebase Cloud Messaging push notification to a specific device using the provided FCM token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification sent successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid Firebase credentials"),
        @ApiResponse(responseCode = "429", description = "Too many requests - Rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponseDto.class)))
    })
    public ResponseEntity<NotificationResponseDto> sendNotification(@Valid @RequestBody NotificationRequestDto notificationRequest) {
        logger.info("Received notification request for device token ending with: {}",
                   notificationRequest.getDeviceToken() != null ? 
                   notificationRequest.getDeviceToken().substring(Math.max(0, notificationRequest.getDeviceToken().length() - 10)) : "null");

        try {
            NotificationResponseDto response = notificationService.sendNotificationToDevice(notificationRequest);
            
            // Return appropriate HTTP status based on the response
            HttpStatus httpStatus = response.isSuccess() ? 
                HttpStatus.OK : HttpStatus.valueOf(response.getStatusCode());
            
            return ResponseEntity.status(httpStatus).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error in notification controller: {}", e.getMessage(), e);
            NotificationResponseDto errorResponse = NotificationResponseDto.error(
                "Internal server error", 500, notificationRequest.getDeviceToken());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Simple endpoint for testing notification functionality
     *
     * @param deviceToken FCM device token
     * @param title       Notification title
     * @param body        Notification body
     * @return Response containing the result of the notification operation
     */
    @PostMapping("/notify/simple")
    @Operation(summary = "Send simple push notification", 
               description = "Sends a simple Firebase Cloud Messaging push notification with basic parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<NotificationResponseDto> sendSimpleNotification(
            @RequestParam @NotBlank(message = "Device token is required") String deviceToken,
            @RequestParam @NotBlank(message = "Title is required") 
            @Size(max = 100, message = "Title must not exceed 100 characters") String title,
            @RequestParam @NotBlank(message = "Body is required") 
            @Size(max = 500, message = "Body must not exceed 500 characters") String body) {
        
        logger.info("Received simple notification request: title='{}', body='{}'", title, body);

        try {
            NotificationResponseDto response = notificationService.sendNotificationToDevice(deviceToken, title, body);
            
            HttpStatus httpStatus = response.isSuccess() ? 
                HttpStatus.OK : HttpStatus.valueOf(response.getStatusCode());
            
            return ResponseEntity.status(httpStatus).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error in simple notification controller: {}", e.getMessage(), e);
            NotificationResponseDto errorResponse = NotificationResponseDto.error(
                "Internal server error", 500, deviceToken);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Send notification to multiple devices
     *
     * @param deviceTokens Array of FCM device tokens
     * @param title        Notification title
     * @param body         Notification body
     * @return Array of responses containing results for each device
     */
    @PostMapping("/notify/bulk")
    @Operation(summary = "Send push notification to multiple devices", 
               description = "Sends Firebase Cloud Messaging push notifications to multiple devices")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications processed (check individual results)"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<NotificationResponseDto[]> sendBulkNotification(
            @RequestBody @NotEmpty(message = "Device tokens array cannot be empty") String[] deviceTokens,
            @RequestParam @NotBlank(message = "Title is required") 
            @Size(max = 100, message = "Title must not exceed 100 characters") String title,
            @RequestParam @NotBlank(message = "Body is required") 
            @Size(max = 500, message = "Body must not exceed 500 characters") String body) {
        
        logger.info("Received bulk notification request for {} devices", deviceTokens.length);

        try {
            NotificationResponseDto[] responses = notificationService.sendNotificationToMultipleDevices(deviceTokens, title, body);
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Unexpected error in bulk notification controller: {}", e.getMessage(), e);
            NotificationResponseDto[] errorResponse = {NotificationResponseDto.error(
                "Internal server error", 500, null)};
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Send iOS-specific push notification
     *
     * @param deviceToken FCM device token
     * @param title       Notification title
     * @param body        Notification body
     * @param sound       iOS sound file (optional)
     * @param badge       Badge count (optional)
     * @return Response containing the result of the notification operation
     */
    @PostMapping("/notify/ios")
    @Operation(summary = "Send iOS-specific push notification", 
               description = "Sends a Firebase Cloud Messaging push notification optimized for iOS devices")
    public ResponseEntity<NotificationResponseDto> sendIOSNotification(
            @RequestParam @NotBlank(message = "Device token is required") String deviceToken,
            @RequestParam @NotBlank(message = "Title is required") String title,
            @RequestParam @NotBlank(message = "Body is required") String body,
            @RequestParam(required = false, defaultValue = "default") String sound,
            @RequestParam(required = false) Integer badge) {
        
        logger.info("Received iOS notification request: title='{}', sound='{}'", title, sound);

        try {
            NotificationRequestDto request = new NotificationRequestDto(deviceToken, title, body);
            request.setPlatform(Platform.IOS);
            request.setHighPriority(true);
            
            // Configure iOS-specific settings
            IOSConfigDto iosConfig = new IOSConfigDto();
            iosConfig.setSound(sound);
            if (badge != null) {
                iosConfig.setBadge(badge);
            }
            iosConfig.setContentAvailable(true);
            request.setIosConfig(iosConfig);

            NotificationResponseDto response = notificationService.sendNotificationToDevice(request);
            
            HttpStatus httpStatus = response.isSuccess() ? 
                HttpStatus.OK : HttpStatus.valueOf(response.getStatusCode());
            
            return ResponseEntity.status(httpStatus).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error in iOS notification controller: {}", e.getMessage(), e);
            NotificationResponseDto errorResponse = NotificationResponseDto.error(
                "Internal server error", 500, deviceToken);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Send Android-specific push notification
     *
     * @param deviceToken FCM device token
     * @param title       Notification title
     * @param body        Notification body
     * @param channelId   Android notification channel ID
     * @param priority    Notification priority
     * @param color       Notification color
     * @return Response containing the result of the notification operation
     */
    @PostMapping("/notify/android")
    @Operation(summary = "Send Android-specific push notification", 
               description = "Sends a Firebase Cloud Messaging push notification optimized for Android devices")
    public ResponseEntity<NotificationResponseDto> sendAndroidNotification(
            @RequestParam @NotBlank(message = "Device token is required") String deviceToken,
            @RequestParam @NotBlank(message = "Title is required") String title,
            @RequestParam @NotBlank(message = "Body is required") String body,
            @RequestParam(required = false, defaultValue = "default") String channelId,
            @RequestParam(required = false, defaultValue = "high") String priority,
            @RequestParam(required = false) String color) {
        
        logger.info("Received Android notification request: title='{}', channelId='{}'", title, channelId);

        try {
            NotificationRequestDto request = new NotificationRequestDto(deviceToken, title, body);
            request.setPlatform(Platform.ANDROID);
            request.setHighPriority(true);
            
            // Configure Android-specific settings
            AndroidConfigDto androidConfig = new AndroidConfigDto();
            androidConfig.setChannelId(channelId);
            androidConfig.setPriority(priority);
            if (color != null) {
                androidConfig.setColor(color);
            }
            androidConfig.setTtl(3600L); // 1 hour TTL
            request.setAndroidConfig(androidConfig);

            NotificationResponseDto response = notificationService.sendNotificationToDevice(request);
            
            HttpStatus httpStatus = response.isSuccess() ? 
                HttpStatus.OK : HttpStatus.valueOf(response.getStatusCode());
            
            return ResponseEntity.status(httpStatus).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error in Android notification controller: {}", e.getMessage(), e);
            NotificationResponseDto errorResponse = NotificationResponseDto.error(
                "Internal server error", 500, deviceToken);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Test cross-platform compatibility
     *
     * @param iosToken     iOS FCM device token
     * @param androidToken Android FCM device token
     * @param title        Notification title
     * @param body         Notification body
     * @return Array of responses for both platforms
     */
    @PostMapping("/notify/cross-platform-test")
    @Operation(summary = "Test cross-platform notification delivery", 
               description = "Sends optimized notifications to both iOS and Android devices for testing")
    public ResponseEntity<NotificationResponseDto[]> testCrossPlatform(
            @RequestParam(required = false) String iosToken,
            @RequestParam(required = false) String androidToken,
            @RequestParam @NotBlank(message = "Title is required") String title,
            @RequestParam @NotBlank(message = "Body is required") String body) {
        
        logger.info("Received cross-platform test request: title='{}'", title);

        try {
            java.util.List<NotificationResponseDto> responses = new java.util.ArrayList<>();

            // Send to iOS if token provided
            if (StringUtils.hasText(iosToken)) {
                NotificationRequestDto iosRequest = new NotificationRequestDto(iosToken, title, body);
                iosRequest.setPlatform(Platform.IOS);
                iosRequest.setHighPriority(true);
                
                IOSConfigDto iosConfig = new IOSConfigDto();
                iosConfig.setSound("default");
                iosConfig.setBadge(1);
                iosConfig.setContentAvailable(true);
                iosRequest.setIosConfig(iosConfig);

                responses.add(notificationService.sendNotificationToDevice(iosRequest));
            }

            // Send to Android if token provided
            if (StringUtils.hasText(androidToken)) {
                NotificationRequestDto androidRequest = new NotificationRequestDto(androidToken, title, body);
                androidRequest.setPlatform(Platform.ANDROID);
                androidRequest.setHighPriority(true);
                
                AndroidConfigDto androidConfig = new AndroidConfigDto();
                androidConfig.setChannelId("test_notifications");
                androidConfig.setPriority("high");
                androidConfig.setColor("#FF5722");
                androidRequest.setAndroidConfig(androidConfig);

                responses.add(notificationService.sendNotificationToDevice(androidRequest));
            }

            if (responses.isEmpty()) {
                responses.add(NotificationResponseDto.error(
                    "At least one device token (iOS or Android) is required", 400, null));
            }

            return ResponseEntity.ok(responses.toArray(new NotificationResponseDto[0]));
            
        } catch (Exception e) {
            logger.error("Unexpected error in cross-platform test controller: {}", e.getMessage(), e);
            NotificationResponseDto[] errorResponse = {NotificationResponseDto.error(
                "Internal server error", 500, null)};
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint for notification service
     *
     * @return Simple health status
     */
    @GetMapping("/health")
    @Operation(summary = "Check notification service health", 
               description = "Returns the health status of the notification service")
    public ResponseEntity<String> healthCheck() {
        logger.debug("Health check request received");
        return ResponseEntity.ok("Notification service is running - iOS & Android compatible");
    }
}