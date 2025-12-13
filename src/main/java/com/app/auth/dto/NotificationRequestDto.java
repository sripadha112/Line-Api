package com.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Data Transfer Object for FCM notification requests
 */
@Schema(description = "Request object for sending push notifications via Firebase Cloud Messaging")
public class NotificationRequestDto {

    @NotBlank(message = "Device token is required")
    @Schema(description = "FCM device token of the target device", example = "f1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    @JsonProperty("deviceToken")
    private String deviceToken;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    @Schema(description = "Notification title", example = "Appointment Reminder")
    @JsonProperty("title")
    private String title;

    @NotBlank(message = "Body is required")
    @Size(max = 500, message = "Body must not exceed 500 characters")
    @Schema(description = "Notification body message", example = "You have an appointment with Dr. Smith at 2:00 PM today")
    @JsonProperty("body")
    private String body;

    @Schema(description = "Additional data to be sent with the notification")
    @JsonProperty("data")
    private Map<String, String> data;

    @Schema(description = "Image URL for rich notifications")
    @JsonProperty("imageUrl")
    private String imageUrl;

    @Schema(description = "Target platform (android, ios, auto)", example = "auto")
    @JsonProperty("platform")
    private Platform platform = Platform.AUTO_DETECT;

    @Schema(description = "iOS-specific APNS configuration")
    @JsonProperty("iosConfig")
    private IOSConfigDto iosConfig;

    @Schema(description = "Android-specific notification configuration")
    @JsonProperty("androidConfig")
    private AndroidConfigDto androidConfig;

    @Schema(description = "High priority notification for immediate delivery", example = "true")
    @JsonProperty("highPriority")
    private Boolean highPriority = false;

    // Default constructor
    public NotificationRequestDto() {
    }

    // Constructor with required fields
    public NotificationRequestDto(String deviceToken, String title, String body) {
        this.deviceToken = deviceToken;
        this.title = title;
        this.body = body;
    }

    // Getters and Setters
    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public IOSConfigDto getIosConfig() {
        return iosConfig;
    }

    public void setIosConfig(IOSConfigDto iosConfig) {
        this.iosConfig = iosConfig;
    }

    public AndroidConfigDto getAndroidConfig() {
        return androidConfig;
    }

    public void setAndroidConfig(AndroidConfigDto androidConfig) {
        this.androidConfig = androidConfig;
    }

    public Boolean getHighPriority() {
        return highPriority;
    }

    public void setHighPriority(Boolean highPriority) {
        this.highPriority = highPriority;
    }

    @Override
    public String toString() {
        return "NotificationRequestDto{" +
                "deviceToken='" + (deviceToken != null ? deviceToken.substring(0, Math.min(deviceToken.length(), 10)) + "..." : "null") + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", data=" + data +
                ", imageUrl='" + imageUrl + '\'' +
                ", platform=" + platform +
                ", iosConfig=" + iosConfig +
                ", androidConfig=" + androidConfig +
                ", highPriority=" + highPriority +
                '}';
    }
}