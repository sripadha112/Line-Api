package com.app.auth.controller;

import com.app.auth.service.AppointmentSchedulerService;
import com.app.auth.service.EnhancedAppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/scheduler")
public class SchedulerAdminController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerAdminController.class);
    private static int keepAliveCounter = 0;

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

    /**
     * Keep-alive scheduler that runs every 30 seconds to prevent Render free tier from sleeping
     * This method logs activity to keep the app warm
     */
    @Scheduled(fixedRate = 30000)// Run every 30 seconds (30000 milliseconds)
    @GetMapping("/live-api")
    public void keepAlive() {
        keepAliveCounter++;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("Keep-alive ping #{} at {}", keepAliveCounter, timestamp);
    }

    /**
     * Get keep-alive status
     */
    @GetMapping("/keep-alive/status")
    public ResponseEntity<Map<String, Object>> getKeepAliveStatus() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return ResponseEntity.ok(Map.of(
            "status", "ACTIVE",
            "message", "Keep-alive scheduler is running every 30 seconds",
            "pingCount", keepAliveCounter,
            "currentTime", timestamp,
            "intervalSeconds", 30
        ));
    }
}
