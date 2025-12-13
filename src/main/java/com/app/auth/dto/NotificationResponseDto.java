package com.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for FCM notification responses
 */
@Schema(description = "Response object for Firebase Cloud Messaging notification operations")
public class NotificationResponseDto {

    @Schema(description = "Indicates if the notification was sent successfully", example = "true")
    @JsonProperty("success")
    private boolean success;

    @Schema(description = "Firebase message ID returned upon successful send", example = "projects/myproject-b5ae1/messages/0:1234567890123456%31bd1c9631bd1c96")
    @JsonProperty("messageId")
    private String messageId;

    @Schema(description = "Error message if notification sending failed", example = "Invalid device token")
    @JsonProperty("errorMessage")
    private String errorMessage;

    @Schema(description = "HTTP status code of the operation", example = "200")
    @JsonProperty("statusCode")
    private int statusCode;

    @Schema(description = "Timestamp when the notification was processed")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Device token that was used for sending", example = "f1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    @JsonProperty("deviceToken")
    private String deviceToken;

    // Default constructor
    public NotificationResponseDto() {
        this.timestamp = LocalDateTime.now();
    }



    // Static factory methods for common responses
    public static NotificationResponseDto success(String messageId, String deviceToken) {
        NotificationResponseDto response = new NotificationResponseDto();
        response.success = true;
        response.messageId = messageId;
        response.statusCode = 200;
        response.deviceToken = deviceToken;
        return response;
    }

    public static NotificationResponseDto error(String errorMessage, int statusCode, String deviceToken) {
        NotificationResponseDto response = new NotificationResponseDto();
        response.success = false;
        response.errorMessage = errorMessage;
        response.statusCode = statusCode;
        response.deviceToken = deviceToken;
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public String toString() {
        return "NotificationResponseDto{" +
                "success=" + success +
                ", messageId='" + messageId + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", statusCode=" + statusCode +
                ", timestamp=" + timestamp +
                ", deviceToken='" + (deviceToken != null ? deviceToken.substring(0, Math.min(deviceToken.length(), 10)) + "..." : "null") + '\'' +
                '}';
    }
}