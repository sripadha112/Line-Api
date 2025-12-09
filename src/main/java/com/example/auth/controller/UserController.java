package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AppointmentService;
import com.example.auth.service.EnhancedAppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
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
    public ResponseEntity<UserAppointmentsResponseDto> getAllUserAppointments(@PathVariable("userId") Long userId) {
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
            @RequestParam("doctorId") Long doctorId,
            @RequestParam("workplaceId") Long workplaceId,
            @RequestParam(value = "date", required = false) String date) {
        AvailableSlotsResponseDto slots = enhancedAppointmentService.getAvailableSlots(doctorId, workplaceId, date);
        return ResponseEntity.ok(slots);
    }
    
    /**
     * 3. Book an appointment (stores in appropriate table based on date)
     * Includes calendar integration based on device type
     */
    @PostMapping("/appointments/book")
    public ResponseEntity<UserAppointmentDto> bookAppointment(
            @Valid @RequestBody BookAppointmentRequestDto request,
            @RequestHeader(value = "X-Calendar-Token", required = false) String calendarToken,
            @RequestHeader(value = "X-Device-Type", required = false) String deviceType,
            HttpServletRequest httpRequest) {
        
        logger.info("📅 BOOK: Received booking request for user: {}, doctor: {}", 
                   request.getUserId(), request.getDoctorId());
        logger.info("🔑 BOOK: Calendar token present: {}", calendarToken != null);
        logger.info("📱 BOOK: Device type: {}", deviceType);
        logger.info("🌐 BOOK: Request from IP: {}", httpRequest.getRemoteAddr());
        logger.info("📝 BOOK: Request details: date={}, slot={}", request.getAppointmentDate(), request.getSlot());
        
        UserAppointmentDto appointment = enhancedAppointmentService.bookAppointment(request);
        
        logger.info("✅ BOOK: Successfully booked appointment with ID: {}", appointment.getId());
        return ResponseEntity.status(201).body(appointment);
    }
    
    /**
     * 4. Cancel an appointment
     * Includes calendar event deletion if calendar token is provided
     */
    @DeleteMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable("appointmentId") Long appointmentId,
            @RequestHeader(value = "X-Calendar-Token", required = false) String calendarToken,
            @RequestHeader(value = "X-Device-Type", required = false) String deviceType,
            HttpServletRequest httpRequest) {
        
        logger.info("❌ CANCEL: Received cancellation request for appointment ID: {}", appointmentId);
        logger.info("🔑 CANCEL: Calendar token present: {}", calendarToken != null);
        logger.info("📱 CANCEL: Device type: {}", deviceType);
        logger.info("🌐 CANCEL: Request from IP: {}", httpRequest.getRemoteAddr());
        
        String result = enhancedAppointmentService.cancelAppointment(appointmentId, calendarToken);
        
        logger.info("✅ CANCEL: Successfully cancelled appointment: {}", result);
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
            @Valid @RequestBody UserRescheduleRequestDto request,
            @RequestHeader(value = "X-Calendar-Token", required = false) String calendarToken,
            @RequestHeader(value = "X-Device-Type", required = false) String deviceType,
            HttpServletRequest httpRequest) {
        
        logger.info("🔄 RESCHEDULE: Received reschedule request for appointment ID: {}", request.getAppointmentId());
        logger.info("🔑 RESCHEDULE: Calendar token present: {}", calendarToken != null);
        logger.info("📱 RESCHEDULE: Device type: {}", deviceType);
        logger.info("🌐 RESCHEDULE: Request from IP: {}", httpRequest.getRemoteAddr());
        logger.info("📝 RESCHEDULE: New date={}, new slot={}", request.getNewAppointmentDate(), request.getNewTimeSlot());
        
        // The appointmentId should already be in the request body
        String result = enhancedAppointmentService.rescheduleUserAppointment(request);
        
        logger.info("✅ RESCHEDULE: Successfully rescheduled appointment: {}", result);
        return ResponseEntity.ok(Map.of("message", result));
    }

    // ========================
    // LEGACY APIs (kept for backward compatibility)
    // ========================

    // @PostMapping("/{userId}/appointments/book")
    // public ResponseEntity<BookAppointmentResponse> book(@PathVariable("userId") Long userId,
    //                                            @Valid @RequestBody BookAppointmentRequest req) {
    //     BookAppointmentResponse response = svc.bookAppointmentEnhanced(userId, req);
    //     return ResponseEntity.status(201).body(response);
    // }

//    @DeleteMapping("/{userId}/appointments/{appointmentId}")
//    public ResponseEntity<AppointmentDto> cancel(@PathVariable("userId") Long userId,
//                                                 @PathVariable("appointmentId") Long appointmentId) {
//        AppointmentDto ap = svc.cancelAppointment(userId, appointmentId);
//        return ResponseEntity.ok(ap);
//    }

    @PostMapping("/{userId}/appointments/{appointmentId}/push-to-end")
    public ResponseEntity<AppointmentDto> pushToEnd(@PathVariable("userId") Long userId,
                                                    @PathVariable("appointmentId") Long appointmentId,
                                                    @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "pushed by user") : "pushed by user";
        AppointmentDto ap = svc.pushToEnd(userId, appointmentId, reason);
        return ResponseEntity.ok(ap);
    }

    @GetMapping("/{userId}/appointments")
    public ResponseEntity<List<AppointmentDto>> myAppointments(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(svc.getUserAppointments(userId));
    }

    // ========================
    // CALENDAR INTEGRATION TEST
    // ========================
    
    /**
     * Test calendar integration functionality
     */
    @GetMapping("/calendar/test")
    public ResponseEntity<Map<String, Object>> testCalendarIntegration(
            @RequestHeader(value = "X-Calendar-Token", required = false) String calendarToken,
            @RequestHeader(value = "X-Device-Type", required = false) String deviceType,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Calendar integration is ready and functional");
        response.put("calendarTokenProvided", calendarToken != null);
        response.put("deviceTypeHeader", deviceType);
        response.put("userAgent", request.getHeader("User-Agent"));
        
        // OAuth2 endpoints
        response.put("googleOAuth2", Map.of(
            "authUrl", "/api/oauth2/google/auth-url",
            "callbackUrl", "/api/oauth2/google/callback",
            "configUrl", "/api/oauth2/google/config"
        ));
        
        // API endpoints that support calendar integration
        response.put("calendarIntegratedAPIs", Map.of(
            "booking", "POST /api/user/appointments/book",
            "cancellation", "DELETE /api/user/appointments/{appointmentId}/cancel",
            "rescheduling", "PUT /api/user/appointments/reschedule"
        ));
        
        return ResponseEntity.ok(response);
    }
}