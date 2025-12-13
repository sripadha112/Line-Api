package com.app.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.FirebaseApp;

/**
 * Test controller to verify Firebase FCM integration
 */
@RestController
@RequestMapping("/api/test")
public class FCMTestController {

    private static final Logger logger = LoggerFactory.getLogger(FCMTestController.class);

    @GetMapping("/firebase-status")
    public ResponseEntity<String> checkFirebaseStatus() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                logger.warn("Firebase app is not initialized");
                return ResponseEntity.ok("Firebase: NOT INITIALIZED");
            }
            
            FirebaseApp app = FirebaseApp.getInstance();
            logger.info("Firebase app status check: {}", app.getName());
            return ResponseEntity.ok("Firebase: INITIALIZED ✓ (App: " + app.getName() + ")");
            
        } catch (Exception e) {
            logger.error("Error checking Firebase status: {}", e.getMessage(), e);
            return ResponseEntity.ok("Firebase: ERROR - " + e.getMessage());
        }
    }
}