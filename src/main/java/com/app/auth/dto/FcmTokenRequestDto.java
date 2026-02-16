package com.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for registering Expo push tokens
 */
public class FcmTokenRequestDto {

    @NotBlank(message = "Expo push token cannot be empty")
    @JsonAlias({"fcmToken", "expoPushToken", "token"})  // Accept multiple field names
    private String fcmToken;

    @Pattern(regexp = "android|ios", flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "Device type must be either 'android' or 'ios'")
    private String deviceType;

    // Constructors
    public FcmTokenRequestDto() {
    }

    public FcmTokenRequestDto(String fcmToken, String deviceType) {
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
    }

    // Getters and Setters
    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
    // Alias getter for backward compatibility
    public String getExpoPushToken() {
        return fcmToken;
    }

    public void setExpoPushToken(String expoPushToken) {
        this.fcmToken = expoPushToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String toString() {
        return "FcmTokenRequestDto{" +
                "fcmToken='" + (fcmToken != null ? fcmToken.substring(0, Math.min(20, fcmToken.length())) + "..." : "null") + '\'' +
                ", deviceType='" + deviceType + '\'' +
                '}';
    }
}