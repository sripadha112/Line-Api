package com.app.auth.controller;

import com.app.auth.config.AuthAccess;
import com.app.auth.config.QueryParamIdCrypto;
import com.app.auth.dto.*;
import com.app.auth.service.AppointmentService;
import com.app.auth.service.EnhancedAppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AppointmentService svc;
    private final EnhancedAppointmentService enhancedAppointmentService;

    public UserController(AppointmentService svc, EnhancedAppointmentService enhancedAppointmentService) { 
        this.svc = svc; 
        this.enhancedAppointmentService = enhancedAppointmentService;
    }

    // ========================
    // NEW ENHANCED APIs
    // ========================
    
    /**
     * 1. Get all appointments for a user (current + future) segregated by date
     * For UI home screen - "Your Appointments" section
     */
    @GetMapping("/{userId}/appointments/all")
    public ResponseEntity<UserAppointmentsResponseDto> getAllUserAppointments(@PathVariable("userId") String encodedUserId) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        UserAppointmentsResponseDto appointments = enhancedAppointmentService.getUserAppointments(userId);
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * 2. Get available slots for a doctor at a specific workplace
     * Returns slots for current day + next 2 days (if no date provided)
     * or slots for specific date (if date provided)
     */
    @GetMapping("/available-slots")
    public ResponseEntity<AvailableSlotsResponseDto> getAvailableSlots(
            @RequestParam("doctorId") String doctorId,
            @RequestParam("workplaceId") String workplaceId,
            @RequestParam(value = "date", required = false) String date) {
        AvailableSlotsResponseDto slots = enhancedAppointmentService.getAvailableSlots(
                QueryParamIdCrypto.decodeLong(doctorId),
                QueryParamIdCrypto.decodeLong(workplaceId),
                date);
        return ResponseEntity.ok(slots);
    }
    
    /**
     * 3. Book an appointment (stores in appropriate table based on date)
     */
//    @PostMapping("/appointments/book")
//    public ResponseEntity<UserAppointmentDto> bookAppointment(@Valid @RequestBody BookAppointmentRequestDto request) {
//        UserAppointmentDto appointment = enhancedAppointmentService.bookAppointment(request);
//        return ResponseEntity.status(201).body(appointment);
//    }
    
    /**
     * 4. Cancel an appointment
     */
    @DeleteMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable("appointmentId") Long appointmentId) {
        String result = enhancedAppointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.ok(Map.of("message", result));
    }
    
    /**
     * 5. Reschedule an appointment
     */
    // @PutMapping("/appointments/reschedule/{appointmentId}")
    // public ResponseEntity<Map<String, String>> rescheduleAppointment(
    //         @PathVariable("appointmentId") Long appointmentId,
    //         @Valid @RequestBody UserRescheduleRequestDto request) {
        
    //     // Set the appointment ID from the path variable
    //     request.setAppointmentId(appointmentId);
        
    //     String result = enhancedAppointmentService.rescheduleUserAppointment(request);
    //     return ResponseEntity.ok(Map.of("message", result));
    // }

    /**
     * 5b. Reschedule an appointment (alternative endpoint for frontend compatibility)
     * Usage: PUT /api/user/appointments/reschedule
     * Expects appointmentId in the request body instead of URL path
     */
    @PutMapping("/appointments/reschedule")
    public ResponseEntity<Map<String, String>> rescheduleAppointmentBody(
            @Valid @RequestBody UserRescheduleRequestDto request) {
        
        // The appointmentId should already be in the request body
        String result = enhancedAppointmentService.rescheduleUserAppointment(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    // ========================
    // LEGACY APIs (kept for backward compatibility)
    // ========================

    @PostMapping("/{userId}/appointments/book")
    public ResponseEntity<BookAppointmentResponse> book(@PathVariable("userId") String encodedUserId,
                                               @Valid @RequestBody BookAppointmentRequest req) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        BookAppointmentResponse response = svc.bookAppointmentEnhanced(userId, req);
        return ResponseEntity.status(201).body(response);
    }

//    @DeleteMapping("/{userId}/appointments/{appointmentId}")
//    public ResponseEntity<AppointmentDto> cancel(@PathVariable("userId") Long userId,
//                                                 @PathVariable("appointmentId") Long appointmentId) {
//        AppointmentDto ap = svc.cancelAppointment(userId, appointmentId);
//        return ResponseEntity.ok(ap);
//    }

    @PostMapping("/{userId}/appointments/{appointmentId}/push-to-end")
    public ResponseEntity<AppointmentDto> pushToEnd(@PathVariable("userId") String encodedUserId,
                                                    @PathVariable("appointmentId") String encodedAppointmentId,
                                                    @RequestBody(required = false) Map<String, String> body) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        Long appointmentId = QueryParamIdCrypto.decodeLong(encodedAppointmentId);
        AuthAccess.requireSelf(userId);
        String reason = body != null ? body.getOrDefault("reason", "pushed by user") : "pushed by user";
        AppointmentDto ap = svc.pushToEnd(userId, appointmentId, reason);
        return ResponseEntity.ok(ap);
    }

    @GetMapping("/{userId}/appointments")
    public ResponseEntity<List<AppointmentDto>> myAppointments(@PathVariable("userId") String encodedUserId) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        return ResponseEntity.ok(svc.getUserAppointments(userId));
    }
    
    /**
     * Register/Update FCM Token for Push Notifications
     * Call this when app starts or token is refreshed
     */
    @PostMapping("/{userId}/fcm-token")
    public ResponseEntity<Map<String, String>> registerFcmToken(
            @PathVariable("userId") String encodedUserId,
            @RequestBody FcmTokenRequestDto request) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        boolean success = enhancedAppointmentService.updateFcmToken(userId, request.getFcmToken(), request.getDeviceType());
        if (success) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "FCM token registered successfully"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", 
                "message", "Failed to register FCM token"
            ));
        }
    }
    
    /**
     * Enable/Disable Push Notifications
     */
    @PutMapping("/{userId}/notifications/toggle")
    public ResponseEntity<Map<String, String>> toggleNotifications(
            @PathVariable("userId") String encodedUserId,
            @RequestParam("enabled") Boolean enabled) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        boolean success = enhancedAppointmentService.toggleNotifications(userId, enabled);
        if (success) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Notification settings updated"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to update notification settings"
            ));
        }
    }
}
