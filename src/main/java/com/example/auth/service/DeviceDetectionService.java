package com.example.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to detect device OS from request headers
 */
@Service
public class DeviceDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceDetectionService.class);
    
    public enum DeviceOS {
        IOS, ANDROID, UNKNOWN
    }
    
    /**
     * Detect device OS from HTTP request headers
     * @param request HTTP request
     * @return Detected device OS
     */
    public DeviceOS detectDeviceOS(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String clientInfo = request.getHeader("X-Client-Info");
        String deviceType = request.getHeader("X-Device-Type");
        
        logger.debug("Detecting device OS - User-Agent: {}, Client-Info: {}, Device-Type: {}", 
                    userAgent, clientInfo, deviceType);
        
        // Check custom headers first (recommended approach for mobile apps)
        if (deviceType != null) {
            String deviceTypeLower = deviceType.toLowerCase();
            if (deviceTypeLower.contains("ios") || deviceTypeLower.contains("iphone") || deviceTypeLower.contains("ipad")) {
                logger.info("Device detected as iOS from X-Device-Type header");
                return DeviceOS.IOS;
            }
            if (deviceTypeLower.contains("android")) {
                logger.info("Device detected as Android from X-Device-Type header");
                return DeviceOS.ANDROID;
            }
        }
        
        // Check client info header
        if (clientInfo != null) {
            String clientInfoLower = clientInfo.toLowerCase();
            if (clientInfoLower.contains("ios") || clientInfoLower.contains("iphone") || clientInfoLower.contains("ipad")) {
                logger.info("Device detected as iOS from X-Client-Info header");
                return DeviceOS.IOS;
            }
            if (clientInfoLower.contains("android")) {
                logger.info("Device detected as Android from X-Client-Info header");
                return DeviceOS.ANDROID;
            }
        }
        
        // Fallback to User-Agent parsing
        if (userAgent != null) {
            String userAgentLower = userAgent.toLowerCase();
            
            // Check for iOS devices
            if (userAgentLower.contains("iphone") || 
                userAgentLower.contains("ipad") || 
                userAgentLower.contains("ipod") ||
                (userAgentLower.contains("mobile") && userAgentLower.contains("safari") && !userAgentLower.contains("chrome"))) {
                logger.info("Device detected as iOS from User-Agent");
                return DeviceOS.IOS;
            }
            
            // Check for Android devices
            if (userAgentLower.contains("android")) {
                logger.info("Device detected as Android from User-Agent");
                return DeviceOS.ANDROID;
            }
        }
        
        logger.warn("Unable to detect device OS, defaulting to UNKNOWN");
        return DeviceOS.UNKNOWN;
    }
    
    /**
     * Detect device OS from custom header
     * This is the recommended approach for mobile applications
     * @param deviceType Custom device type header value
     * @return Detected device OS
     */
    public DeviceOS detectDeviceOSFromHeader(String deviceType) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            return DeviceOS.UNKNOWN;
        }
        
        String deviceTypeLower = deviceType.toLowerCase().trim();
        
        if (deviceTypeLower.equals("ios") || deviceTypeLower.equals("iphone") || deviceTypeLower.equals("ipad")) {
            return DeviceOS.IOS;
        }
        
        if (deviceTypeLower.equals("android")) {
            return DeviceOS.ANDROID;
        }
        
        return DeviceOS.UNKNOWN;
    }
    
    /**
     * Check if the device is iOS
     * @param request HTTP request
     * @return true if iOS device
     */
    public boolean isIOS(HttpServletRequest request) {
        return detectDeviceOS(request) == DeviceOS.IOS;
    }
    
    /**
     * Check if the device is Android
     * @param request HTTP request
     * @return true if Android device
     */
    public boolean isAndroid(HttpServletRequest request) {
        return detectDeviceOS(request) == DeviceOS.ANDROID;
    }
}