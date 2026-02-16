package com.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Expo push notification responses
 */
@Schema(description = "Response object for Expo Push Notification operations")
public class NotificationResponseDto {

    @Schema(description = "Indicates if the notification was sent successfully", example = "true")
    @JsonProperty("success")
    private boolean success;

    @Schema(description = "Expo ticket ID returned upon successful send", example = "ExponentPushTicket[xxxxxxxxxxxxxxxxxxxxxx]")
    @JsonProperty("ticketId")
    private String ticketId;

    @Schema(description = "Error message if notification sending failed", example = "Invalid device token")
    @JsonProperty("errorMessage")
    private String errorMessage;

    @Schema(description = "HTTP status code of the operation", example = "200")
    @JsonProperty("statusCode")
    private int statusCode;

    @Schema(description = "Timestamp when the notification was processed")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Expo push token that was used for sending", example = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]")
    @JsonProperty("expoPushToken")
    private String expoPushToken;

    @Schema(description = "Expo message ID returned upon successful send", example = "ExponentPushMessage[xxxxxxxxxxxxxxxxxxxxxx]")
    @JsonProperty("messageId")
    private String messageId;

    // Default constructor
    public NotificationResponseDto() {
        this.timestamp = LocalDateTime.now();
    }



    // Static factory methods for common responses
    public static NotificationResponseDto success(String ticketId, String expoPushToken) {
        NotificationResponseDto response = new NotificationResponseDto();
        response.success = true;
        response.ticketId = ticketId;
        response.statusCode = 200;
        response.expoPushToken = expoPushToken;
        return response;
    }

    public static NotificationResponseDto error(String errorMessage, int statusCode, String expoPushToken) {
        NotificationResponseDto response = new NotificationResponseDto();
        response.success = false;
        response.errorMessage = errorMessage;
        response.statusCode = statusCode;
        response.expoPushToken = expoPushToken;
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
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

    public String getExpoPushToken() {
        return expoPushToken;
    }

    public void setExpoPushToken(String expoPushToken) {
        this.expoPushToken = expoPushToken;
    }

    @Override
    public String toString() {
        return "NotificationResponseDto{" +
                "success=" + success +
                ", messageId='" + messageId + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", statusCode=" + statusCode +
                ", timestamp=" + timestamp +
                ", expoPushToken='" + (expoPushToken != null ? expoPushToken.substring(0, Math.min(expoPushToken.length(), 10)) + "..." : "null") + '\'' +
                '}';
    }
}