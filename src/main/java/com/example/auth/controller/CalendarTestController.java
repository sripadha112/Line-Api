package com.example.auth.controller;

import com.example.auth.service.CalendarFactoryService;
import com.example.auth.service.DeviceDetectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for calendar integration testing and device detection
 */
@RestController
@RequestMapping("/api/calendar")
public class CalendarTestController {
    
    @Autowired
    private DeviceDetectionService deviceDetectionService;
    
    @Autowired
    private CalendarFactoryService calendarFactoryService;
    
    /**
     * Test endpoint to check device detection
     */
    @GetMapping("/device-info")
    public ResponseEntity<Map<String, Object>> getDeviceInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        DeviceDetectionService.DeviceOS deviceOS = deviceDetectionService.detectDeviceOS(request);
        String calendarServiceType = calendarFactoryService.getCalendarServiceType(request);
        boolean isSupported = calendarFactoryService.isCalendarSupportedForDevice(request);
        
        response.put("deviceOS", deviceOS.toString());
        response.put("calendarService", calendarServiceType);
        response.put("calendarSupported", isSupported);
        response.put("userAgent", request.getHeader("User-Agent"));
        response.put("deviceTypeHeader", request.getHeader("X-Device-Type"));
        response.put("clientInfoHeader", request.getHeader("X-Client-Info"));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint to check device detection with custom header
     */
    @PostMapping("/device-info")
    public ResponseEntity<Map<String, Object>> testDeviceDetection(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String deviceType = request.get("deviceType");
        DeviceDetectionService.DeviceOS deviceOS = deviceDetectionService.detectDeviceOSFromHeader(deviceType);
        String calendarServiceType = calendarFactoryService.getCalendarService(deviceType).getServiceType().toString();
        
        response.put("inputDeviceType", deviceType);
        response.put("detectedOS", deviceOS.toString());
        response.put("selectedCalendarService", calendarServiceType);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get calendar integration documentation
     */
    @GetMapping("/integration-guide")
    public ResponseEntity<Map<String, Object>> getIntegrationGuide() {
        Map<String, Object> guide = new HashMap<>();
        
        guide.put("bookingExample", Map.of(
            "method", "POST",
            "url", "/api/user/appointments/book",
            "headers", Map.of(
                "Content-Type", "application/json",
                "X-Device-Type", "android or ios"
            ),
            "body", Map.of(
                "userId", 123,
                "doctorId", 456,
                "workplaceId", 789,
                "appointmentDate", "2025-12-05",
                "slot", "9:00AM - 9:30AM",
                "notes", "Regular checkup",
                "userCalendarAccessToken", "your_calendar_access_token",
                "deviceType", "android"
            )
        ));
        
        guide.put("reschedulingExample", Map.of(
            "method", "PUT",
            "url", "/api/user/appointments/reschedule",
            "headers", Map.of(
                "Content-Type", "application/json",
                "X-Device-Type", "android or ios"
            ),
            "body", Map.of(
                "appointmentId", 123,
                "reason", "Schedule conflict",
                "newAppointmentDate", "2025-12-06",
                "newTimeSlot", "10:00AM - 10:30AM",
                "userCalendarAccessToken", "your_calendar_access_token",
                "deviceType", "android"
            )
        ));
        
        guide.put("cancellationExample", Map.of(
            "method", "DELETE",
            "url", "/api/user/appointments/{appointmentId}/cancel",
            "headers", Map.of(
                "X-Calendar-Token", "your_calendar_access_token",
                "X-Device-Type", "android or ios"
            )
        ));
        
        guide.put("supportedDevices", Map.of(
            "android", "Google Calendar API integration",
            "ios", "Apple Calendar (EventKit) integration",
            "unknown", "Defaults to Google Calendar"
        ));
        
        guide.put("requiredConfiguration", Map.of(
            "googleCalendar", Map.of(
                "clientId", "GOOGLE_CLIENT_ID environment variable",
                "clientSecret", "GOOGLE_CLIENT_SECRET environment variable",
                "redirectUri", "GOOGLE_REDIRECT_URI environment variable"
            ),
            "appleCalendar", Map.of(
                "teamId", "APPLE_TEAM_ID environment variable",
                "keyId", "APPLE_KEY_ID environment variable",
                "privateKey", "APPLE_PRIVATE_KEY environment variable",
                "bundleId", "APPLE_BUNDLE_ID environment variable"
            )
        ));
        
        return ResponseEntity.ok(guide);
    }
}