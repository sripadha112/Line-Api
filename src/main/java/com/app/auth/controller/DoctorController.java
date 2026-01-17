package com.app.auth.controller;

import com.app.auth.dto.*;
import com.app.auth.entity.Appointment;
// FutureTwoDayAppointment entity remains in the project for safety but is no longer used here
import com.app.auth.entity.PastAppointment;
import com.app.auth.entity.UserDetails;
import com.app.auth.repository.AppointmentRepository;
import com.app.auth.repository.PastAppointmentRepository;
import com.app.auth.repository.UserDetailsRepository;
import com.app.auth.service.AppointmentService;
import com.app.auth.service.EnhancedAppointmentService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final AppointmentService svc;
    private final EnhancedAppointmentService enhancedAppointmentService;
    private final AppointmentRepository appointmentRepository;
    private final PastAppointmentRepository pastAppointmentRepository;
    private final UserDetailsRepository userDetailsRepository;

    public DoctorController(AppointmentService svc, 
                          EnhancedAppointmentService enhancedAppointmentService,
                          AppointmentRepository appointmentRepository,
                          /* FutureTwoDayAppointmentRepository futureAppointmentRepository, */
                          PastAppointmentRepository pastAppointmentRepository,
                          UserDetailsRepository userDetailsRepository) { 
        this.svc = svc; 
        this.enhancedAppointmentService = enhancedAppointmentService;
    this.appointmentRepository = appointmentRepository;
        this.pastAppointmentRepository = pastAppointmentRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

//    @GetMapping("/{doctorId}/appointments")
//    public ResponseEntity<List<AppointmentDto>> calendar(
//            @PathVariable("doctorId") Long doctorId,
//            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
//            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
//        return ResponseEntity.ok(svc.getDoctorAppointments(doctorId, from, to));
//    }

    /**
     * Get doctor's appointment history grouped by workspace
     * Returns all past appointments organized by workplace with patient details
     */
    @GetMapping("/{doctorId}/appointments/history")
    public ResponseEntity<List<DoctorHistoryWorkspaceDto>> history(@PathVariable("doctorId") Long doctorId) {
        List<DoctorHistoryWorkspaceDto> historyByWorkspace = getDoctorHistoryGroupedByWorkspace(doctorId);
        return ResponseEntity.ok(historyByWorkspace);
    }

    /**
     * Bulk reschedule all appointments for a workspace
     * Supports extending appointments by hours/minutes or moving to specific date
     */
    @PostMapping("/{doctorId}/appointments/bulk-reschedule")
    public ResponseEntity<Map<String, String>> reschedule(@PathVariable("doctorId") Long doctorId,
                                                         @Valid @RequestBody WorkspaceBulkRescheduleDto request) {
        // Collect all appointment IDs for the doctor and workspace with status BOOKED
        List<Long> appointmentIds = new ArrayList<>();
        List<Appointment> allCurrentAppointments = appointmentRepository.findByWorkplaceIdOrderByAppointmentDateAndTime(request.getWorkspaceId());
        for (Appointment appointment : allCurrentAppointments) {
            if (appointment.getDoctorId().equals(doctorId) && "BOOKED".equals(appointment.getStatus())) {
                appointmentIds.add(appointment.getId());
            }
        }
        // List<FutureTwoDayAppointment> allFutureAppointments = futureAppointmentRepository.findByWorkplaceIdOrderByAppointmentDateAndTime(request.getWorkspaceId());
        // for (FutureTwoDayAppointment futureAppt : allFutureAppointments) {
        //     if (futureAppt.getDoctorId().equals(doctorId) && "BOOKED".equals(futureAppt.getStatus())) {
        //         appointmentIds.add(futureAppt.getId());
        //     }
        // }
        if (appointmentIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No booked appointments found for the specified workspace."));
        }

        // Calculate total minutes
        int totalMinutes = 0;
        if (request.hasTimeExtension()) {
            if (request.getExtendHours() != null) {
                totalMinutes += request.getExtendHours() * 60;
            }
            if (request.getExtendMinutes() != null) {
                totalMinutes += request.getExtendMinutes();
            }
        }

        String dbResult = "";
        try {
            // If newDate is present but empty string, treat it as not provided and call non-date function
            String newDate = request.getNewDate();
            boolean hasValidNewDate = newDate != null && !newDate.trim().isEmpty();

            if (hasValidNewDate) {
                // Call DB function with date
                dbResult = callIncreaseTimeRangeWithDate(appointmentIds, totalMinutes, newDate);
            } else if (totalMinutes > 0) {
                // Call DB function with only minutes
                dbResult = callIncreaseTimeRange(appointmentIds, totalMinutes);
            } else {
                return ResponseEntity.ok(Map.of("message", "No time extension or new date provided."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to reschedule appointments: " + e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", dbResult));
    }
    private String callIncreaseTimeRange(List<Long> appointmentIds, int totalMinutes) {
        String minutesStr = totalMinutes + " minutes";
        // Build ARRAY literal like ARRAY[1,2,3]
        String arrayLiteral = "ARRAY[" + appointmentIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
        // Build final SQL to match the examples: SELECT increase_time_range(ARRAY[...], '100 minutes');
        String sql = "SELECT increase_time_range(" + arrayLiteral + ", '" + minutesStr + "')";
        jdbcTemplate.execute(sql);
        return "reschedule successful";
    }

    private String callIncreaseTimeRangeWithDate(List<Long> appointmentIds, int totalMinutes, String newDate) {
        String minutesStr = totalMinutes + " minutes";
        String arrayLiteral = "ARRAY[" + appointmentIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
        // Build SQL matching the example: SELECT increase_time_range(ARRAY[...], '30 minutes', '2025-10-21');
        String sql = "SELECT increase_time_range(" + arrayLiteral + ", '" + minutesStr + "', '" + newDate + "')";
        jdbcTemplate.execute(sql);
        return "reschedule successful";
    }

    /**
     * Cancel all appointments for a workspace on a specific date
     * Marks all BOOKED appointments as CANCELLED for the given workspace and date
     */
    @PostMapping("/workspaces/{workspaceId}/appointments/cancel-day")
    public ResponseEntity<Map<String, String>> cancelWorkspaceDay(@PathVariable("workspaceId") Long workspaceId,
                                                                 @Valid @RequestBody CancelDayRequest req) {
        String result = cancelAllWorkspaceAppointments(workspaceId, req);
        return ResponseEntity.ok(Map.of("message", result));
    }
    
    /**
     * Get doctor's appointment history grouped by workspace
     */
    private List<DoctorHistoryWorkspaceDto> getDoctorHistoryGroupedByWorkspace(Long doctorId) {
        // Get all past appointments for the doctor
        List<PastAppointment> pastAppointments = pastAppointmentRepository.findByDoctorIdOrderByAppointmentTimeDesc(doctorId);
        
        // Group appointments by workspace
        Map<String, List<DoctorHistoryAppointmentDto>> appointmentsByWorkspace = new LinkedHashMap<>();
        Map<String, PastAppointment> workspaceInfo = new LinkedHashMap<>();
        
        for (PastAppointment appointment : pastAppointments) {
            String workspaceKey = appointment.getWorkplaceId() + "_" + appointment.getWorkplaceName();
            
            // Store workspace info for later use
            workspaceInfo.put(workspaceKey, appointment);
            
            // Convert to DTO with patient details
            DoctorHistoryAppointmentDto appointmentDto = convertToDoctorHistoryAppointmentDto(appointment);
            
            appointmentsByWorkspace
                .computeIfAbsent(workspaceKey, k -> new ArrayList<>())
                .add(appointmentDto);
        }
        
        // Convert grouped appointments to workspace DTOs
        return appointmentsByWorkspace.entrySet().stream()
                .map(entry -> {
                    PastAppointment workspaceData = workspaceInfo.get(entry.getKey());
                    return new DoctorHistoryWorkspaceDto(
                        workspaceData.getWorkplaceId(),
                        workspaceData.getWorkplaceName(),
                        workspaceData.getWorkplaceType(),
                        workspaceData.getWorkplaceAddress(),
                        entry.getValue()
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Convert PastAppointment to DoctorHistoryAppointmentDto with patient details
     */
    private DoctorHistoryAppointmentDto convertToDoctorHistoryAppointmentDto(PastAppointment appointment) {
        DoctorHistoryAppointmentDto dto = new DoctorHistoryAppointmentDto();
        dto.setAppointmentId(appointment.getId());
        dto.setUserId(appointment.getUserId());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setTimeSlot(appointment.getSlot());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        
        // Fetch user details for patient information
        Optional<UserDetails> userDetails = userDetailsRepository.findById(appointment.getUserId());
        if (userDetails.isPresent()) {
            UserDetails user = userDetails.get();
            dto.setPatientFullName(user.getFullName());
            dto.setAge(user.getAge());
            dto.setMobileNumber(user.getMobileNumber());
        }
        
        return dto;
    }
    
    /**
     * Bulk reschedule all appointments for a workspace
     * Updates existing appointments with new time/date instead of creating duplicates
     */
    private String bulkRescheduleWorkspaceAppointments(Long doctorId, WorkspaceBulkRescheduleDto request) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Get all appointments for the workspace from appointments table (current/today's appointments)
        List<Appointment> allCurrentAppointments = appointmentRepository.findByWorkplaceIdOrderByAppointmentDateAndTime(request.getWorkspaceId());
        
        List<Appointment> currentWorkspaceAppointments = new ArrayList<>();
        
        // Filter for doctor's BOOKED appointments from current table
        for (Appointment appointment : allCurrentAppointments) {
            if (appointment.getDoctorId().equals(doctorId) && 
                "BOOKED".equals(appointment.getStatus())) {
                currentWorkspaceAppointments.add(appointment);
            }
        }
        
        // Get all appointments for the workspace (appointments table contains both current and future entries now)
        List<Appointment> allAppointments = appointmentRepository.findByWorkplaceIdOrderByAppointmentDateAndTime(request.getWorkspaceId());

        List<Appointment> futureWorkspaceAppointments = new ArrayList<>();

        // Filter for doctor's BOOKED appointments that are not today => treat as future appointments
        for (Appointment appt : allAppointments) {
            if (appt.getDoctorId().equals(doctorId) && "BOOKED".equals(appt.getStatus())) {
                String apptDate = appt.getAppointmentDate();
                if (!apptDate.equals(today)) {
                    futureWorkspaceAppointments.add(appt);
                }
            }
        }
        
        if (currentWorkspaceAppointments.isEmpty() && futureWorkspaceAppointments.isEmpty()) {
            return "No booked appointments found for the specified workspace.";
        }
        
        int rescheduledCount = 0;
        String rescheduleInfo = "";
        String reason = "Rescheduled by doctor";
        if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
            reason += ": " + request.getReason();
        }
        
        // Update current appointments
        for (Appointment appointment : currentWorkspaceAppointments) {
            // Store original appointment time before updating
            OffsetDateTime originalAppointmentTime = appointment.getAppointmentTime();
            
            // Calculate new appointment time
            OffsetDateTime newAppointmentTime = calculateNewAppointmentTime(
                originalAppointmentTime, request);
            
            // Update the existing appointment directly
            appointment.setAppointmentTime(newAppointmentTime);
            appointment.setAppointmentDate(newAppointmentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            // Update slot based on extension or new time (use original time for calculation)
            String oldSlot = appointment.getSlot(); // Store old slot for debugging
            String newSlot = calculateNewSlot(originalAppointmentTime, newAppointmentTime, appointment.getDurationMinutes(), request);
            
            // Debug info (can be removed later)
            System.out.println("DEBUG - Appointment ID: " + appointment.getId());
            System.out.println("DEBUG - Original Time: " + originalAppointmentTime);
            System.out.println("DEBUG - New Time: " + newAppointmentTime);
            System.out.println("DEBUG - Duration: " + appointment.getDurationMinutes() + " minutes");
            System.out.println("DEBUG - Old Slot: " + oldSlot);
            System.out.println("DEBUG - New Slot: " + newSlot);
            System.out.println("DEBUG - Has Time Extension: " + request.hasTimeExtension());
            System.out.println("DEBUG - Extend Hours: " + request.getExtendHours());
            System.out.println("DEBUG - Extend Minutes: " + request.getExtendMinutes());
            System.out.println("DEBUG ---");
            
            appointment.setSlot(newSlot);
            appointment.setNotes(reason);
            
            // Check if the new date requires moving to future table
            String newDate = newAppointmentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!newDate.equals(today)) {
                // Keep in appointments table but with updated date/time and slot
                appointmentRepository.save(appointment);
            } else {
                // Stay in current appointments table
                appointmentRepository.save(appointment);
            }
            
            rescheduledCount++;
        }
        
        // Update future appointments (now stored in appointments table)
        for (Appointment futureAppt : futureWorkspaceAppointments) {
            // Store original appointment time before updating
            OffsetDateTime originalAppointmentTime = futureAppt.getAppointmentTime();

            // Calculate new appointment time
            OffsetDateTime newAppointmentTime = calculateNewAppointmentTime(
                originalAppointmentTime, request);

            // Update the existing future appointment directly
            futureAppt.setAppointmentTime(newAppointmentTime);
            futureAppt.setAppointmentDate(newAppointmentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            // Update slot based on extension or new time (use original time for calculation)
            String oldSlot = futureAppt.getSlot(); // Store old slot for debugging
            String newSlot = calculateNewSlot(originalAppointmentTime, newAppointmentTime, futureAppt.getDurationMinutes(), request);

            // Debug info (can be removed later)
            System.out.println("DEBUG FUTURE - Appointment ID: " + futureAppt.getId());
            System.out.println("DEBUG FUTURE - Original Time: " + originalAppointmentTime);
            System.out.println("DEBUG FUTURE - New Time: " + newAppointmentTime);
            System.out.println("DEBUG FUTURE - Duration: " + futureAppt.getDurationMinutes() + " minutes");
            System.out.println("DEBUG FUTURE - Old Slot: " + oldSlot);
            System.out.println("DEBUG FUTURE - New Slot: " + newSlot);
            System.out.println("DEBUG FUTURE - Has Time Extension: " + request.hasTimeExtension());
            System.out.println("DEBUG FUTURE - Extend Hours: " + request.getExtendHours());
            System.out.println("DEBUG FUTURE - Extend Minutes: " + request.getExtendMinutes());
            System.out.println("DEBUG FUTURE ---");

            futureAppt.setSlot(newSlot);
            futureAppt.setNotes(reason);

            // Check if the new date requires moving to current (today) - all still in appointments table so just save
            String newDate = newAppointmentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            appointmentRepository.save(futureAppt);

            rescheduledCount++;
        }
        
        // Generate response message
        if (request.hasNewDate()) {
            rescheduleInfo = "moved to " + request.getNewDate();
        } else if (request.hasTimeExtension()) {
            if (request.getExtendHours() != null && request.getExtendHours() > 0) {
                rescheduleInfo += request.getExtendHours() + " hours ";
            }
            if (request.getExtendMinutes() != null && request.getExtendMinutes() > 0) {
                rescheduleInfo += request.getExtendMinutes() + " minutes ";
            }
            rescheduleInfo = "extended by " + rescheduleInfo.trim();
        }
        
        return String.format("All %d appointments have been %s. You can recheck your schedule now.", 
                           rescheduledCount, rescheduleInfo);
    }
    
    /**
     * Calculate new appointment time based on request parameters
     */
    private OffsetDateTime calculateNewAppointmentTime(OffsetDateTime originalTime, WorkspaceBulkRescheduleDto request) {
        if (request.hasNewDate()) {
            // Move to new date, keep same time
            LocalDate newDate = LocalDate.parse(request.getNewDate());
            return originalTime.withYear(newDate.getYear())
                             .withMonth(newDate.getMonthValue())
                             .withDayOfMonth(newDate.getDayOfMonth());
        } else if (request.hasTimeExtension()) {
            // Extend by specified time
            OffsetDateTime newTime = originalTime;
            if (request.getExtendHours() != null) {
                newTime = newTime.plusHours(request.getExtendHours());
            }
            if (request.getExtendMinutes() != null) {
                newTime = newTime.plusMinutes(request.getExtendMinutes());
            }
            return newTime;
        }
        return originalTime; // No change if no parameters provided
    }
    
    /**
     * Calculate new slot based on the type of rescheduling
     * For time extension: shifts the slot by the extension amount
     * For date change: keeps the same time slot but on new date
     */
    private String calculateNewSlot(OffsetDateTime originalTime, OffsetDateTime newTime, Integer durationMinutes, WorkspaceBulkRescheduleDto request) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mma");
        
        if (request.hasTimeExtension()) {
            // For time extension: the new slot starts at the new time (which is original + extension)
            // Example: Original: 8:00 AM, Extension: 30 min, New time: 8:30 AM
            // New slot: 8:30 AM - 9:00 AM (duration remains same)
            OffsetDateTime slotStartTime = newTime;
            OffsetDateTime slotEndTime = slotStartTime.plusMinutes(durationMinutes);
            
            return slotStartTime.format(timeFormatter) + " - " + slotEndTime.format(timeFormatter);
        } else {
            // For date change: use the new time with original duration
            return generateTimeSlot(newTime, durationMinutes);
        }
    }
    
    /**
     * Generate time slot string based on appointment time and duration
     */
    private String generateTimeSlot(OffsetDateTime startTime, int durationMinutes) {
        OffsetDateTime endTime = startTime.plusMinutes(durationMinutes);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mma");
        return startTime.format(timeFormatter) + " - " + endTime.format(timeFormatter);
    }
    
    // FutureTwoDayAppointment conversion helpers removed — appointments table is now the single source of truth
    
    /**
     * Cancel all appointments for a workspace on a specific date
     * Marks all BOOKED appointments as CANCELLED
     * Sends FCM notifications to affected users
     */
    private String cancelAllWorkspaceAppointments(Long workspaceId, CancelDayRequest request) {
        String targetDate = request.getDate(); // Assuming CancelDayRequest has a date field
        
        int cancelledCount = 0;
        
        // Get all appointments for the workspace on the specified date from appointments table
        List<Appointment> currentAppointments = appointmentRepository.findByWorkplaceIdAndAppointmentDate(workspaceId, targetDate);
        
        for (Appointment appointment : currentAppointments) {
            if ("BOOKED".equals(appointment.getStatus())) {
                appointment.setStatus("CANCELLED");
                appointment.setNotes("Cancelled by doctor - " + (request.getReason() != null ? request.getReason() : "Day cancelled"));
                appointmentRepository.save(appointment);
                
                // Send FCM notification to the user
                enhancedAppointmentService.sendAppointmentNotification(
                    appointment.getUserId(),
                    "Appointment Cancelled ❌",
                    String.format("Your appointment with Dr. %s on %s at %s has been cancelled. Reason: %s",
                        appointment.getDoctorName() != null ? appointment.getDoctorName() : "Doctor",
                        appointment.getAppointmentDate(),
                        appointment.getSlot() != null ? appointment.getSlot() : "scheduled time",
                        request.getReason() != null ? request.getReason() : "Doctor cancelled all appointments for this day"),
                    "APPOINTMENT_CANCELLED_BY_DOCTOR"
                );
                
                cancelledCount++;
            }
        }
        
        // Get future appointments for the workspace on the specified date from future_2day_appointments table
        // List<FutureTwoDayAppointment> futureAppointments = futureAppointmentRepository.findByWorkplaceIdAndAppointmentDate(workspaceId, targetDate);
        
        // for (FutureTwoDayAppointment appointment : futureAppointments) {
        //     if ("BOOKED".equals(appointment.getStatus())) {
        //         appointment.setStatus("CANCELLED");
        //         appointment.setNotes("Cancelled by doctor - " + (request.getReason() != null ? request.getReason() : "Day cancelled"));
        //         futureAppointmentRepository.save(appointment);
        //         cancelledCount++;
        //     }
        // }
        
        if (cancelledCount == 0) {
            return "No booked appointments found for the specified workspace and date.";
        }
        
        return String.format("Successfully cancelled %d appointments for workspace on %s.", cancelledCount, targetDate);
    }
    
    // ==================== NEW DOCTOR MANAGEMENT APIS ====================
    
    /**
     * Get all appointments booked for a doctor on a specific date with user details
     * Returns appointments sorted by appointment time for doctor's appointment management screen
     */
//    @GetMapping("/{doctorId}/appointments/date/{appointmentDate}/users")
//    public ResponseEntity<List<DoctorAppointmentViewDto>> getDoctorAppointmentsWithUsers(
//            @PathVariable("doctorId") Long doctorId,
//            @PathVariable("appointmentDate") String appointmentDate) {
//
//        List<DoctorAppointmentViewDto> appointments = enhancedAppointmentService
//                .getDoctorAppointmentsWithUserDetails(doctorId, appointmentDate);
//        return ResponseEntity.ok(appointments);
//    }
    
    /**
     * Bulk update appointment statuses for multiple users
     * Supports COMPLETED, RESCHEDULED, and CANCELLED statuses
     * Used for doctor to mark appointments as completed, reschedule, or cancel multiple appointments
     */
//    @PutMapping("/{doctorId}/appointments/date/{appointmentDate}/bulk-status")
//    public ResponseEntity<String> bulkUpdateAppointmentStatus(
//            @PathVariable("doctorId") Long doctorId,
//            @PathVariable("appointmentDate") String appointmentDate,
//            @Valid @RequestBody BulkAppointmentStatusUpdateDto request) {
//
//        String result = enhancedAppointmentService
//                .bulkUpdateAppointmentStatus(doctorId, appointmentDate, request);
//        return ResponseEntity.ok(result);
//    }

}
