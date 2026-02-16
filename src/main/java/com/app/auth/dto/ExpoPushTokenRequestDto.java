package com.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for registering Expo push tokens
 */
public class ExpoPushTokenRequestDto {

    @NotBlank(message = "Expo push token cannot be empty")
    private String expoPushToken;

    @Pattern(regexp = "android|ios", flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "Device type must be either 'android' or 'ios'")
    private String deviceType;

    // Constructors
    public ExpoPushTokenRequestDto() {}

    public ExpoPushTokenRequestDto(String expoPushToken, String deviceType) {
        this.expoPushToken = expoPushToken;
        this.deviceType = deviceType;
    }

    // Getters and Setters
    public String getExpoPushToken() { return expoPushToken; }
    public void setExpoPushToken(String expoPushToken) { this.expoPushToken = expoPushToken; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    @Override
    public String toString() {
        return "ExpoPushTokenRequestDto{" +
                "expoPushToken='" + (expoPushToken != null ? expoPushToken.substring(0, Math.min(20, expoPushToken.length())) + "..." : "null") + '\'' +
                ", deviceType='" + deviceType + '\'' +
                '}';
    }
}