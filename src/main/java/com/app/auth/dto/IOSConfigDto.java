package com.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Configuration for iOS-specific APNS settings
 */
@Schema(description = "iOS-specific APNS notification configuration")
public class IOSConfigDto {

    @Schema(description = "iOS notification sound file name", example = "default.caf")
    @JsonProperty("sound")
    private String sound = "default";

    @Schema(description = "Badge count to display on app icon", example = "1")
    @JsonProperty("badge")
    private Integer badge;

    @Schema(description = "Enable content-available for background processing", example = "true")
    @JsonProperty("contentAvailable")
    private Boolean contentAvailable;

    @Schema(description = "Enable mutable-content for notification service extensions", example = "true")
    @JsonProperty("mutableContent")
    private Boolean mutableContent;

    @Schema(description = "iOS notification category identifier", example = "APPOINTMENT_REMINDER")
    @JsonProperty("category")
    private String category;

    @Schema(description = "Thread identifier for grouping notifications", example = "appointment-thread")
    @JsonProperty("threadId")
    private String threadId;

    // Default constructor
    public IOSConfigDto() {
    }

    // Getters and Setters
    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public Integer getBadge() {
        return badge;
    }

    public void setBadge(Integer badge) {
        this.badge = badge;
    }

    public Boolean getContentAvailable() {
        return contentAvailable;
    }

    public void setContentAvailable(Boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
    }

    public Boolean getMutableContent() {
        return mutableContent;
    }

    public void setMutableContent(Boolean mutableContent) {
        this.mutableContent = mutableContent;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    @Override
    public String toString() {
        return "IOSConfigDto{" +
                "sound='" + sound + '\'' +
                ", badge=" + badge +
                ", contentAvailable=" + contentAvailable +
                ", mutableContent=" + mutableContent +
                ", category='" + category + '\'' +
                ", threadId='" + threadId + '\'' +
                '}';
    }
}