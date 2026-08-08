package com.app.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller to verify notification mode configuration
 */
@RestController
@RequestMapping("/api/test")
public class FCMTestController {

    private static final Logger logger = LoggerFactory.getLogger(FCMTestController.class);

    @GetMapping("/firebase-status")
    public ResponseEntity<String> checkFirebaseStatus() {
        try {
            logger.info("Notification mode status check: Expo-only mode");
            return ResponseEntity.ok("Notifications: EXPO-ONLY MODE");
            
        } catch (Exception e) {
            logger.error("Error checking Firebase status: {}", e.getMessage(), e);
            return ResponseEntity.ok("Firebase: ERROR - " + e.getMessage());
        }
    }
}