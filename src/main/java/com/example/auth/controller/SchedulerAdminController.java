package com.example.auth.controller;

import com.example.auth.service.AppointmentSchedulerService;
import com.example.auth.service.EnhancedAppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/scheduler")
public class SchedulerAdminController {

    private final AppointmentSchedulerService schedulerService;
    private final EnhancedAppointmentService enhancedAppointmentService;

    public SchedulerAdminController(AppointmentSchedulerService schedulerService, 
                                  EnhancedAppointmentService enhancedAppointmentService) {
        this.schedulerService = schedulerService;
        this.enhancedAppointmentService = enhancedAppointmentService;
    }

    /**
     * Manual trigger for complete appointment movement process
     * Use this in case of scheduler failure or for testing
     */
    @PostMapping("/trigger-movement")
    public ResponseEntity<Map<String, String>> triggerAppointmentMovement() {
        try {
            schedulerService.triggerManualAppointmentMovement();
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Appointment movement triggered successfully",
                "timestamp", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", "Failed to trigger appointment movement: " + e.getMessage(),
                "timestamp", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
        }
    }

    /**
     * Manual trigger for moving future appointments to current day only
     */
    @PostMapping("/move-future-to-current")
    public ResponseEntity<Map<String, String>> moveFutureToCurrentDay() {
        try {
            enhancedAppointmentService.moveAppointmentsToCurrentDay();
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Future appointments moved to current day successfully",
                "date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", "Failed to move future appointments: " + e.getMessage()
            ));
        }
    }

    /**
     * Manual trigger for moving past appointments to past table only
     */
    @PostMapping("/move-past-appointments")
    public ResponseEntity<Map<String, String>> movePastAppointments() {
        try {
            enhancedAppointmentService.movePastAppointments();
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Past appointments moved to past table successfully",
                "date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", "Failed to move past appointments: " + e.getMessage()
            ));
        }
    }

    /**
     * Get scheduler status and information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            return ResponseEntity.ok(Map.of(
                "status", "ACTIVE",
                "currentDate", today,
                "schedulerCron", "0 1 0 * * ? (Every day at 12:01 AM)",
                "message", "Scheduler is active and will run automatically",
                "manualTriggerEndpoint", "/api/admin/scheduler/trigger-movement"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", "Failed to get scheduler status: " + e.getMessage()
            ));
        }
    }
}
