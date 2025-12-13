package com.app.auth.dto;

/**
 * Enumeration for mobile platforms
 */
public enum Platform {
    ANDROID("android"),
    IOS("ios"),
    AUTO_DETECT("auto");

    private final String value;

    Platform(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Platform fromString(String platform) {
        if (platform == null) {
            return AUTO_DETECT;
        }
        
        for (Platform p : Platform.values()) {
            if (p.value.equalsIgnoreCase(platform)) {
                return p;
            }
        }
        return AUTO_DETECT;
    }
}