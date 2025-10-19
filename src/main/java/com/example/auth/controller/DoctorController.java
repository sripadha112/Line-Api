package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.entity.Appointment;
import com.example.auth.entity.FutureTwoDayAppointment;
import com.example.auth.entity.PastAppointment;
import com.example.auth.entity.UserDetails;
import com.example.auth.repository.AppointmentRepository;
import com.example.auth.repository.FutureTwoDayAppointmentRepository;
import com.example.auth.repository.PastAppointmentRepository;
import com.example.auth.repository.UserDetailsRepository;
import com.example.auth.service.AppointmentService;
import com.example.auth.service.EnhancedAppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final AppointmentService svc;
    private final EnhancedAppointmentService enhancedAppointmentService;
    private final AppointmentRepository appointmentRepository;
    private final FutureTwoDayAppointmentRepository futureAppointmentRepository;
    private final PastAppointmentRepository pastAppointmentRepository;
    private final UserDetailsRepository userDetailsRepository;

    public DoctorController(AppointmentService svc, 
                          EnhancedAppointmentService enhancedAppointmentService,
                          AppointmentRepository appointmentRepository,
                          FutureTwoDayAppointmentRepository futureAppointmentRepository,
                          PastAppointmentRepository pastAppointmentRepository,
                          UserDetailsRepository userDetailsRepository) { 
        this.svc = svc; 
        this.enhancedAppointmentService = enhancedAppointmentService;
        this.appointmentRepository = appointmentRepository;
        this.futureAppointmentRepository = futureAppointmentRepository;
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
    @PostMapping("/appointments/bulk-reschedule")
    public ResponseEntity<Map<String, String>> reschedule(@PathVariable("doctorId") Long doctorId,
                                                         @Valid @RequestBody WorkspaceBulkRescheduleDto request) {
        
        String result = bulkRescheduleWorkspaceAppointments(doctorId, request);
        return ResponseEntity.ok(Map.of("message", result));
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
     * Marks existing appointments as RESCHEDULED and creates new ones with BOOKED status
     */
    private String bulkRescheduleWorkspaceAppointments(Long doctorId, WorkspaceBulkRescheduleDto request) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Get all current and future appointments for the workspace from appointments table
        List<Appointment> currentAppointments = appointmentRepository.findByDoctorIdAndWorkplaceIdAndAppointmentDate(
            doctorId, request.getWorkspaceId(), today);
        
        List<Appointment> allWorkspaceAppointments = new ArrayList<>();
        
        // Filter to get only current/future appointments that are BOOKED
        for (Appointment appointment : currentAppointments) {
            if ("BOOKED".equals(appointment.getStatus()) && 
                appointment.getAppointmentDate().compareTo(today) >= 0) {
                allWorkspaceAppointments.add(appointment);
            }
        }
        
        // Get future appointments from future_2day_appointments table
        List<FutureTwoDayAppointment> futureAppointments = futureAppointmentRepository.findByDoctorIdAndWorkplaceId(
            doctorId, request.getWorkspaceId());
        
        for (FutureTwoDayAppointment futureAppt : futureAppointments) {
            if ("BOOKED".equals(futureAppt.getStatus())) {
                // Convert to Appointment for uniform processing
                Appointment convertedAppt = convertFutureToAppointment(futureAppt);
                allWorkspaceAppointments.add(convertedAppt);
            }
        }
        
        if (allWorkspaceAppointments.isEmpty()) {
            return "No booked appointments found for the specified workspace.";
        }
        
        int rescheduledCount = 0;
        String rescheduleInfo = "";
        
        // Process each appointment
        for (Appointment originalAppointment : allWorkspaceAppointments) {
            // Mark original as RESCHEDULED
            originalAppointment.setStatus("RESCHEDULED");
            String reason = "Rescheduled by doctor";
            if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
                reason += ": " + request.getReason();
            }
            originalAppointment.setNotes(reason);
            
            // Calculate new appointment time
            OffsetDateTime newAppointmentTime = calculateNewAppointmentTime(
                originalAppointment.getAppointmentTime(), request);
            
            // Create new appointment with BOOKED status
            Appointment newAppointment = createRescheduledAppointment(originalAppointment, newAppointmentTime, reason);
            
            // Save both appointments
            appointmentRepository.save(originalAppointment);
            
            // Determine which table to save the new appointment to
            String newDate = newAppointmentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (newDate.equals(today)) {
                // Save to current appointments table
                appointmentRepository.save(newAppointment);
            } else {
                // Save to future appointments table
                FutureTwoDayAppointment futureAppt = convertAppointmentToFuture(newAppointment);
                futureAppointmentRepository.save(futureAppt);
            }
            
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
     * Create a new rescheduled appointment based on the original
     */
    private Appointment createRescheduledAppointment(Appointment original, OffsetDateTime newTime, String reason) {
        Appointment newAppointment = new Appointment();
        
        // Copy all fields from original
        newAppointment.setDoctorId(original.getDoctorId());
        newAppointment.setUserId(original.getUserId());
        newAppointment.setWorkplaceId(original.getWorkplaceId());
        newAppointment.setWorkplaceName(original.getWorkplaceName());
        newAppointment.setWorkplaceType(original.getWorkplaceType());
        newAppointment.setWorkplaceAddress(original.getWorkplaceAddress());
        newAppointment.setDurationMinutes(original.getDurationMinutes());
        newAppointment.setQueuePosition(original.getQueuePosition());
        newAppointment.setDoctorName(original.getDoctorName());
        newAppointment.setDoctorSpecialization(original.getDoctorSpecialization());
        
        // Set new time and date
        newAppointment.setAppointmentTime(newTime);
        newAppointment.setAppointmentDate(newTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // Update slot based on new time
        newAppointment.setSlot(generateTimeSlot(newTime, original.getDurationMinutes()));
        
        // Set as BOOKED and add reason
        newAppointment.setStatus("BOOKED");
        newAppointment.setNotes("Rescheduled from " + original.getAppointmentDate() + " - " + reason);
        
        return newAppointment;
    }
    
    /**
     * Generate time slot string based on appointment time and duration
     */
    private String generateTimeSlot(OffsetDateTime startTime, int durationMinutes) {
        OffsetDateTime endTime = startTime.plusMinutes(durationMinutes);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mma");
        return startTime.format(timeFormatter) + " - " + endTime.format(timeFormatter);
    }
    
    /**
     * Convert FutureTwoDayAppointment to Appointment for uniform processing
     */
    private Appointment convertFutureToAppointment(FutureTwoDayAppointment futureAppt) {
        Appointment appointment = new Appointment();
        appointment.setId(futureAppt.getId());
        appointment.setDoctorId(futureAppt.getDoctorId());
        appointment.setUserId(futureAppt.getUserId());
        appointment.setWorkplaceId(futureAppt.getWorkplaceId());
        appointment.setWorkplaceName(futureAppt.getWorkplaceName());
        appointment.setWorkplaceType(futureAppt.getWorkplaceType());
        appointment.setWorkplaceAddress(futureAppt.getWorkplaceAddress());
        appointment.setAppointmentTime(futureAppt.getAppointmentTime());
        appointment.setAppointmentDate(futureAppt.getAppointmentDate());
        appointment.setSlot(futureAppt.getSlot());
        appointment.setDurationMinutes(futureAppt.getDurationMinutes());
        appointment.setQueuePosition(futureAppt.getQueuePosition());
        appointment.setStatus(futureAppt.getStatus());
        appointment.setNotes(futureAppt.getNotes());
        appointment.setDoctorName(futureAppt.getDoctorName());
        appointment.setDoctorSpecialization(futureAppt.getDoctorSpecialization());
        return appointment;
    }
    
    /**
     * Convert Appointment to FutureTwoDayAppointment for saving to future table
     */
    private FutureTwoDayAppointment convertAppointmentToFuture(Appointment appointment) {
        FutureTwoDayAppointment futureAppt = new FutureTwoDayAppointment();
        futureAppt.setDoctorId(appointment.getDoctorId());
        futureAppt.setUserId(appointment.getUserId());
        futureAppt.setWorkplaceId(appointment.getWorkplaceId());
        futureAppt.setWorkplaceName(appointment.getWorkplaceName());
        futureAppt.setWorkplaceType(appointment.getWorkplaceType());
        futureAppt.setWorkplaceAddress(appointment.getWorkplaceAddress());
        futureAppt.setAppointmentTime(appointment.getAppointmentTime());
        futureAppt.setAppointmentDate(appointment.getAppointmentDate());
        futureAppt.setSlot(appointment.getSlot());
        futureAppt.setDurationMinutes(appointment.getDurationMinutes());
        futureAppt.setQueuePosition(appointment.getQueuePosition());
        futureAppt.setStatus(appointment.getStatus());
        futureAppt.setNotes(appointment.getNotes());
        futureAppt.setDoctorName(appointment.getDoctorName());
        futureAppt.setDoctorSpecialization(appointment.getDoctorSpecialization());
        return futureAppt;
    }
    
    /**
     * Cancel all appointments for a workspace on a specific date
     * Marks all BOOKED appointments as CANCELLED
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
                cancelledCount++;
            }
        }
        
        // Get future appointments for the workspace on the specified date from future_2day_appointments table
        List<FutureTwoDayAppointment> futureAppointments = futureAppointmentRepository.findByWorkplaceIdAndAppointmentDate(workspaceId, targetDate);
        
        for (FutureTwoDayAppointment appointment : futureAppointments) {
            if ("BOOKED".equals(appointment.getStatus())) {
                appointment.setStatus("CANCELLED");
                appointment.setNotes("Cancelled by doctor - " + (request.getReason() != null ? request.getReason() : "Day cancelled"));
                futureAppointmentRepository.save(appointment);
                cancelledCount++;
            }
        }
        
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
