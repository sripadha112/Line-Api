package com.example.auth.service;

import com.example.auth.service.impl.AppleCalendarServiceImpl;
import com.example.auth.service.impl.GoogleCalendarServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Factory service to select the appropriate calendar service based on device OS
 */
@Service
public class CalendarFactoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CalendarFactoryService.class);
    
    @Autowired
    private DeviceDetectionService deviceDetectionService;
    
    @Autowired
    private GoogleCalendarServiceImpl googleCalendarService;
    
    @Autowired
    private AppleCalendarServiceImpl appleCalendarService;
    
    /**
     * Get the appropriate calendar service based on the device OS
     * @param request HTTP request to detect device OS
     * @return Appropriate calendar service
     */
    public CalendarService getCalendarService(HttpServletRequest request) {
        DeviceDetectionService.DeviceOS deviceOS = deviceDetectionService.detectDeviceOS(request);
        
        switch (deviceOS) {
            case IOS:
                logger.debug("Selected Apple Calendar Service for iOS device");
                return appleCalendarService;
            case ANDROID:
                logger.debug("Selected Google Calendar Service for Android device");
                return googleCalendarService;
            default:
                logger.warn("Unknown device OS, defaulting to Google Calendar Service");
                return googleCalendarService; // Default to Google Calendar
        }
    }
    
    /**
     * Get the appropriate calendar service based on device type header
     * @param deviceType Device type from custom header
     * @return Appropriate calendar service
     */
    public CalendarService getCalendarService(String deviceType) {
        DeviceDetectionService.DeviceOS deviceOS = deviceDetectionService.detectDeviceOSFromHeader(deviceType);
        
        switch (deviceOS) {
            case IOS:
                logger.debug("Selected Apple Calendar Service for iOS device (from header)");
                return appleCalendarService;
            case ANDROID:
                logger.debug("Selected Google Calendar Service for Android device (from header)");
                return googleCalendarService;
            default:
                logger.warn("Unknown device OS from header, defaulting to Google Calendar Service");
                return googleCalendarService; // Default to Google Calendar
        }
    }
    
    /**
     * Check if calendar integration is supported for the device
     * @param request HTTP request to detect device OS
     * @return true if calendar integration is supported
     */
    public boolean isCalendarSupportedForDevice(HttpServletRequest request) {
        DeviceDetectionService.DeviceOS deviceOS = deviceDetectionService.detectDeviceOS(request);
        return deviceOS == DeviceDetectionService.DeviceOS.IOS || 
               deviceOS == DeviceDetectionService.DeviceOS.ANDROID;
    }
    
    /**
     * Get calendar service type name for logging
     * @param request HTTP request to detect device OS
     * @return Calendar service type name
     */
    public String getCalendarServiceType(HttpServletRequest request) {
        DeviceDetectionService.DeviceOS deviceOS = deviceDetectionService.detectDeviceOS(request);
        
        switch (deviceOS) {
            case IOS:
                return "Apple Calendar";
            case ANDROID:
                return "Google Calendar";
            default:
                return "Google Calendar (Default)";
        }
    }
}