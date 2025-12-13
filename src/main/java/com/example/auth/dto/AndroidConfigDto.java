package com.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Configuration for Android-specific notification settings
 */
@Schema(description = "Android-specific notification configuration")
public class AndroidConfigDto {

    @Schema(description = "Android notification channel ID", example = "appointment_reminders")
    @JsonProperty("channelId")
    private String channelId = "default";

    @Schema(description = "Notification priority (high, normal, low)", example = "high")
    @JsonProperty("priority")
    private String priority = "high";

    @Schema(description = "Notification sound URI", example = "default_notification_sound")
    @JsonProperty("sound")
    private String sound;

    @Schema(description = "Notification icon resource name", example = "ic_notification")
    @JsonProperty("icon")
    private String icon;

    @Schema(description = "Notification color in hex format", example = "#FF5722")
    @JsonProperty("color")
    private String color;

    @Schema(description = "Notification tag for grouping", example = "appointments")
    @JsonProperty("tag")
    private String tag;

    @Schema(description = "Time to live in seconds", example = "3600")
    @JsonProperty("ttl")
    private Long ttl;

    @Schema(description = "Restrict delivery to non-idle devices", example = "false")
    @JsonProperty("restrictedPackageName")
    private String restrictedPackageName;

    @Schema(description = "Click action for notification", example = "OPEN_APPOINTMENT_ACTIVITY")
    @JsonProperty("clickAction")
    private String clickAction;

    // Default constructor
    public AndroidConfigDto() {
    }

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public String getRestrictedPackageName() {
        return restrictedPackageName;
    }

    public void setRestrictedPackageName(String restrictedPackageName) {
        this.restrictedPackageName = restrictedPackageName;
    }

    public String getClickAction() {
        return clickAction;
    }

    public void setClickAction(String clickAction) {
        this.clickAction = clickAction;
    }

    @Override
    public String toString() {
        return "AndroidConfigDto{" +
                "channelId='" + channelId + '\'' +
                ", priority='" + priority + '\'' +
                ", sound='" + sound + '\'' +
                ", icon='" + icon + '\'' +
                ", color='" + color + '\'' +
                ", tag='" + tag + '\'' +
                ", ttl=" + ttl +
                ", restrictedPackageName='" + restrictedPackageName + '\'' +
                ", clickAction='" + clickAction + '\'' +
                '}';
    }
}