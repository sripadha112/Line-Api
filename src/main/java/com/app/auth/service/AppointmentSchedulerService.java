package com.app.auth.service;

import com.app.auth.service.EnhancedAppointmentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AppointmentSchedulerService {

    private final EnhancedAppointmentService enhancedAppointmentService;

    public AppointmentSchedulerService(EnhancedAppointmentService enhancedAppointmentService) {
        this.enhancedAppointmentService = enhancedAppointmentService;
    }

    /**
     * Run every day at 12:01 AM to move future appointments to current day
     * and move past appointments to past appointments table
     */
    @Scheduled(cron = "0 1 0 * * ?") // Every day at 12:01 AM
    public void moveAppointments() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("=".repeat(80));
        System.out.println("[SCHEDULER] Starting daily appointment movement at " + timestamp);
        System.out.println("=".repeat(80));
        
        try {
            // Step 1: Move future appointments that are now current
            System.out.println("[SCHEDULER] Step 1: Moving future appointments to current day...");
            enhancedAppointmentService.moveAppointmentsToCurrentDay();
            System.out.println("[SCHEDULER] ✓ Step 1 completed successfully");
            
            // Step 2: Move past appointments to past table
            System.out.println("[SCHEDULER] Step 2: Moving past appointments to past table...");
            enhancedAppointmentService.movePastAppointments();
            System.out.println("[SCHEDULER] ✓ Step 2 completed successfully");
            
            System.out.println("=".repeat(80));
            System.out.println("[SCHEDULER] ✓ Daily appointment movement completed successfully at " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            System.err.println("=".repeat(80));
            System.err.println("[SCHEDULER] ✗ ERROR during appointment movement at " + timestamp);
            System.err.println("[SCHEDULER] Error message: " + e.getMessage());
            System.err.println("[SCHEDULER] Use manual trigger API: POST /api/admin/scheduler/trigger-movement");
            System.err.println("=".repeat(80));
            e.printStackTrace();
        }
    }

    /**
     * Manual trigger for testing or admin purposes
     * Can be called via API in case of scheduler failure
     */
    public void triggerManualAppointmentMovement() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("=".repeat(80));
        System.out.println("[MANUAL TRIGGER] Starting appointment movement at " + timestamp);
        System.out.println("=".repeat(80));
        
        moveAppointments();
        
        System.out.println("[MANUAL TRIGGER] Manual appointment movement completed");
    }

    /**
     * Test scheduler - runs every minute for testing (comment out in production)
     * Uncomment this method for testing purposes only
     */
    // @Scheduled(cron = "0 * * * * ?") // Every minute - FOR TESTING ONLY
    public void testScheduler() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("[TEST SCHEDULER] Test run at " + timestamp + " - Scheduler is working!");
    }
}
