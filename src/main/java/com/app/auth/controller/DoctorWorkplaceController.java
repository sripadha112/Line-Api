package com.app.auth.controller;

import com.app.auth.dto.BulkAppointmentStatusUpdateDto;
import com.app.auth.dto.DailyAppointmentStatusDto;
import com.app.auth.dto.DoctorAppointmentViewDto;
import com.app.auth.dto.DoctorWorkplaceCreateDto;
import com.app.auth.dto.DoctorWorkplaceDto;
import com.app.auth.dto.WorkspaceAppointmentDto;
import com.app.auth.dto.WorkspaceDateAppointmentsDto;
import com.app.auth.entity.Appointment;
import com.app.auth.entity.DoctorDetails;
import com.app.auth.entity.DoctorWorkplace;
// FutureTwoDayAppointment entity/repository kept for safety but not used here
import com.app.auth.entity.UserDetails;
import com.app.auth.repository.AppointmentRepository;
import com.app.auth.repository.DoctorDetailsRepository;
import com.app.auth.repository.DoctorWorkplaceRepository;
import com.app.auth.repository.UserDetailsRepository;
import com.app.auth.service.DailyAppointmentService;
import com.app.auth.service.EnhancedAppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
public class DoctorWorkplaceController {

    private final DoctorWorkplaceRepository workplaceRepository;
    private final AppointmentRepository appointmentRepository;
    // private final FutureTwoDayAppointmentRepository futureAppointmentRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final DoctorDetailsRepository doctorDetailsRepository;
    private final DailyAppointmentService dailyAppointmentService;
    private final EnhancedAppointmentService enhancedAppointmentService;

    public DoctorWorkplaceController(DoctorWorkplaceRepository workplaceRepository,
                                   AppointmentRepository appointmentRepository,
                                   /* FutureTwoDayAppointmentRepository futureAppointmentRepository, */
                                   UserDetailsRepository userDetailsRepository,
                                   DoctorDetailsRepository doctorDetailsRepository,
                                   DailyAppointmentService dailyAppointmentService,
                                   EnhancedAppointmentService enhancedAppointmentService) {
        this.workplaceRepository = workplaceRepository;
        this.appointmentRepository = appointmentRepository;
    // this.futureAppointmentRepository = futureAppointmentRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.doctorDetailsRepository = doctorDetailsRepository;
        this.dailyAppointmentService = dailyAppointmentService;
        this.enhancedAppointmentService = enhancedAppointmentService;
    }


    //using this
    @GetMapping("/{doctorId}/workplaces")
    public ResponseEntity<List<DoctorWorkplaceDto>> getDoctorWorkplaces(@PathVariable("doctorId") Long doctorId) {
        List<DoctorWorkplace> workplaces = workplaceRepository.findByDoctorId(doctorId);
        List<DoctorWorkplaceDto> workplaceDtos = workplaces.stream()
                .map(workplace -> convertToDto(workplace, doctorId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(workplaceDtos);
    }

    /**
     * Add a new workplace for a doctor
     */
    @PostMapping("/{doctorId}/add-workplaces")
    public ResponseEntity<Map<String, Object>> addDoctorWorkplace(
            @PathVariable("doctorId") Long doctorId,
            @Valid @RequestBody DoctorWorkplaceCreateDto createRequest) {
        
        try {
            // Check if doctor exists
            Optional<DoctorDetails> doctorOpt = doctorDetailsRepository.findById(doctorId);
            if (!doctorOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            DoctorDetails doctor = doctorOpt.get();
            
            // Create new workplace
            DoctorWorkplace workplace = new DoctorWorkplace();
            workplace.setDoctor(doctor);
            workplace.setWorkplaceName(createRequest.getWorkplaceName());
            workplace.setWorkplaceType(createRequest.getWorkplaceType());
            workplace.setAddress(createRequest.getAddress());
            workplace.setCity(createRequest.getCity());
            workplace.setState(createRequest.getState());
            workplace.setPincode(createRequest.getPincode());
            workplace.setCountry(createRequest.getCountry());
            workplace.setContactNumber(createRequest.getContactNumber());
            workplace.setMorningStartTime(createRequest.getMorningStartTime());
            workplace.setMorningEndTime(createRequest.getMorningEndTime());
            workplace.setEveningStartTime(createRequest.getEveningStartTime());
            workplace.setEveningEndTime(createRequest.getEveningEndTime());
            workplace.setCheckingDurationMinutes(createRequest.getCheckingDurationMinutes());
            workplace.setIsPrimary(createRequest.getIsPrimary() != null ? createRequest.getIsPrimary() : false);
            
            // Save the workplace
            DoctorWorkplace savedWorkplace = workplaceRepository.save(workplace);
            
            // Build response
            Map<String, Object> response = Map.of(
                "message", "Workplace created successfully",
                "workplaceId", savedWorkplace.getId(),
                "workplaceName", savedWorkplace.getWorkplaceName()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to create workplace: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/{doctorId}/appointments/today/status")
    public ResponseEntity<DailyAppointmentStatusDto> getTodayAppointmentStatus(@PathVariable("doctorId") Long doctorId) {
        DailyAppointmentStatusDto status = dailyAppointmentService.getDoctorCurrentDateAppointmentStatus(doctorId);
        return ResponseEntity.ok(status);
    }
    
    // ==================== NEW DOCTOR MANAGEMENT APIS ====================
    
    /**
     * Alternative endpoint for getting doctor appointments with user details
     * Same functionality as in DoctorController but under /api/doctors path
     */
    @GetMapping("/{doctorId}/appointments/{appointmentDate}/patients")
    public ResponseEntity<List<DoctorAppointmentViewDto>> getDoctorPatientsForDate(
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("appointmentDate") String appointmentDate) {
        
        List<DoctorAppointmentViewDto> appointments = enhancedAppointmentService
                .getDoctorAppointmentsWithUserDetails(doctorId, appointmentDate);
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Alternative endpoint for bulk status update
     * Same functionality as in DoctorController but under /api/doctors path
     */
    @PutMapping("/{doctorId}/appointments/{appointmentDate}/update-status")
    public ResponseEntity<String> updatePatientsStatus(
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("appointmentDate") String appointmentDate,
            @Valid @RequestBody BulkAppointmentStatusUpdateDto request) {
        
        String result = enhancedAppointmentService
                .bulkUpdateAppointmentStatus(doctorId, appointmentDate, request);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all appointments for a specific workplace grouped by date
     * Returns appointments organized by date with patient medical details
     */
    @GetMapping("/workplaces/{workplaceId}/appointments")
    public ResponseEntity<List<WorkspaceDateAppointmentsDto>> getWorkspaceAppointments(
            @PathVariable("workplaceId") Long workplaceId) {
        
        List<WorkspaceDateAppointmentsDto> appointments = getWorkspaceAppointmentsGroupedByDate(workplaceId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get workspace appointments grouped by date with patient medical details
     * Only includes current and future appointments (ignores past appointments)
     */
    private List<WorkspaceDateAppointmentsDto> getWorkspaceAppointmentsGroupedByDate(Long workplaceId) {
        Map<String, List<WorkspaceAppointmentDto>> appointmentsByDate = new LinkedHashMap<>();
        
        // Get today's date for filtering
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Get all current appointments (from appointments table) - only today and future
        List<Appointment> currentAppointments = appointmentRepository.findByWorkplaceIdOrderByAppointmentDateAndTime(workplaceId);
        for (Appointment appointment : currentAppointments) {
            // Only include appointments from today onwards and with BOOKED status
            if (appointment.getAppointmentDate().compareTo(today) >= 0 && "BOOKED".equals(appointment.getStatus())) {
                WorkspaceAppointmentDto dto = convertToWorkspaceAppointmentDto(appointment);
                appointmentsByDate
                    .computeIfAbsent(appointment.getAppointmentDate(), k -> new ArrayList<>())
                    .add(dto);
            }
        }
        
        // Include appointments from appointments table that are future (date >= today)
        for (Appointment appointment : currentAppointments) {
            // (currentAppointments already fetched from appointmentRepository and includes today+future)
            // We already processed them in the loop above, so this block intentionally does nothing here.
        }
        
        // Convert map to list of DTOs
        return appointmentsByDate.entrySet().stream()
                .map(entry -> new WorkspaceDateAppointmentsDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Convert Appointment to WorkspaceAppointmentDto with medical details
     */
    private WorkspaceAppointmentDto convertToWorkspaceAppointmentDto(Appointment appointment) {
        WorkspaceAppointmentDto dto = new WorkspaceAppointmentDto();
        dto.setAppointmentId(appointment.getId());
        dto.setUserId(appointment.getUserId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setTimeSlot(appointment.getSlot());
        
        // Fetch user medical details
        Optional<UserDetails> userDetails = userDetailsRepository.findById(appointment.getUserId());
        if (userDetails.isPresent()) {
            UserDetails user = userDetails.get();
            dto.setPatientName(user.getFullName());
            dto.setAge(user.getAge());
            dto.setWeightKg(user.getWeightKg());
            dto.setBloodPressureSystolic(user.getBloodPressureSystolic());
            dto.setBloodPressureDiastolic(user.getBloodPressureDiastolic());
            dto.setBloodGroup(user.getBloodGroup());
            dto.setMobileNumber(user.getMobileNumber());
        }
        
        return dto;
    }

    // convertToWorkspaceAppointmentDto(FutureTwoDayAppointment) removed; use the Appointment overload instead

    private DoctorWorkplaceDto convertToDto(DoctorWorkplace workplace, Long doctorId) {
        DoctorWorkplaceDto dto = new DoctorWorkplaceDto();
        dto.setId(workplace.getId());
        dto.setWorkplaceName(workplace.getWorkplaceName());
        dto.setWorkplaceType(workplace.getWorkplaceType());
        dto.setAddress(workplace.getAddress());
        dto.setContactNumber(workplace.getContactNumber());
        dto.setIsPrimary(workplace.getIsPrimary());
        
        // Calculate appointment counts segregated by time period
        AppointmentCounts counts = getAppointmentCounts(doctorId, workplace.getId());
        dto.setTodayAppointmentsCount(counts.todayCount);
        dto.setFutureAppointmentsCount(counts.futureCount);
        dto.setActiveAppointmentsCount(counts.todayCount + counts.futureCount); // Total active
        
        return dto;
    }
    
    /**
     * Calculate appointment counts segregated by time period for a doctor at a specific workplace
     * Today's appointments go to todayCount, future dates go to futureCount
     */
    private AppointmentCounts getAppointmentCounts(Long doctorId, Long workplaceId) {
        // Get today's date as string
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        long todayCount = appointmentRepository.countByDoctorIdAndWorkplaceIdAndAppointmentDateAndStatus(doctorId, workplaceId, today, "BOOKED");
        long futureCount = appointmentRepository.countByDoctorIdAndWorkplaceIdAndAppointmentDateGreaterThanAndStatus(doctorId, workplaceId, today, "BOOKED");

        return new AppointmentCounts(todayCount, futureCount);
    }
    
    /**
     * Mark an appointment as completed
     * Updates the appointment status to "COMPLETED" in the appointments table
     * Usage: PUT /api/doctors/appointments/{appointmentId}/complete
     */
    @PutMapping("/appointments/{appointmentId}/complete")
    public ResponseEntity<Map<String, Object>> markAppointmentCompleted(@PathVariable("appointmentId") Long appointmentId) {
        try {
            // Find the appointment in the appointments table
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            
            if (!appointmentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Appointment appointment = appointmentOpt.get();
            
            // Check if appointment is in a status that can be completed
            if (!"BOOKED".equals(appointment.getStatus()) && !"RESCHEDULED".equals(appointment.getStatus())) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Appointment cannot be completed. Current status: " + appointment.getStatus(),
                    "success", false
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Update the status to COMPLETED
            appointment.setStatus("COMPLETED");
            appointment.setUpdatedAt(java.time.OffsetDateTime.now());
            
            // Save the updated appointment
            Appointment savedAppointment = appointmentRepository.save(appointment);
            
            // Build success response
            Map<String, Object> response = Map.of(
                "message", "Appointment marked as completed successfully",
                "success", true,
                "appointmentId", savedAppointment.getId(),
                "status", savedAppointment.getStatus(),
                "updatedAt", savedAppointment.getUpdatedAt()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to update appointment status: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Cancel an appointment
     * Updates the appointment status to "CANCELLED" in either appointments or future_2day_appointments table
     * Usage: PUT /api/doctors/appointments/{appointmentId}/cancel
     */
    @PutMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable("appointmentId") Long appointmentId) {
        try {
            // First try to find the appointment in the appointments table (current day)
            Optional<Appointment> currentAppointmentOpt = appointmentRepository.findById(appointmentId);
            
            if (currentAppointmentOpt.isPresent()) {
                Appointment appointment = currentAppointmentOpt.get();
                
                // Check if appointment is in a status that can be cancelled
                if (!"BOOKED".equals(appointment.getStatus()) && !"RESCHEDULED".equals(appointment.getStatus())) {
                    Map<String, Object> errorResponse = Map.of(
                        "error", "Appointment cannot be cancelled. Current status: " + appointment.getStatus(),
                        "success", false
                    );
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                
                // Update the status to CANCELLED
                appointment.setStatus("CANCELLED");
                appointment.setUpdatedAt(java.time.OffsetDateTime.now());
                
                // Save the updated appointment
                Appointment savedAppointment = appointmentRepository.save(appointment);
                
                // Build success response
                Map<String, Object> response = Map.of(
                    "message", "Appointment cancelled successfully",
                    "success", true,
                    "appointmentId", savedAppointment.getId(),
                    "status", savedAppointment.getStatus(),
                    "updatedAt", savedAppointment.getUpdatedAt(),
                    "table", "appointments"
                );
                
                return ResponseEntity.ok(response);
            }
            
            // No fallback to future table — appointments table contains both current and future now
            
            // Appointment not found in either table
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to cancel appointment: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Helper class to hold appointment counts
     */
    private static class AppointmentCounts {
        final long todayCount;
        final long futureCount;
        
        AppointmentCounts(long todayCount, long futureCount) {
            this.todayCount = todayCount;
            this.futureCount = futureCount;
        }
    }
}
