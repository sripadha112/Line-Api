package com.app.auth.controller;

import com.app.auth.dto.UserProfileDto;
import com.app.auth.service.UserMedicalProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserMedicalProfileController {

    private final UserMedicalProfileService medicalProfileService;

    public UserMedicalProfileController(UserMedicalProfileService medicalProfileService) {
        this.medicalProfileService = medicalProfileService;
    }

    /**
     * Get complete user profile including medical data and prescription history
     * Usage: GET /api/user/{userId}/profile
     * 
     * Returns all user information including:
     * - Basic details (name, email, address, etc.)
     * - Medical profile (vitals, medications, allergies, etc.)
     * - Current medical notes
     * - Prescription history (previous medical notes)
     * - Emergency contact information
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDto> getUserCompleteProfile(@PathVariable("userId") Long userId) {
        UserProfileDto profile = medicalProfileService.getUserCompleteProfile(userId);
        
        if (profile != null) {
            return ResponseEntity.ok(profile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update user's complete profile (basic + medical data)
     * Usage: PUT /api/user/{userId}/edit-profile
     * 
     * This endpoint supports partial updates - only send the fields you want to update.
     * Both users and doctors can use this API to update any information.
     * 
     * PRESCRIPTION LOGIC:
     * - When updating medicalNotes, existing medical notes are automatically moved to the prescription field
     * - Previous prescriptions are preserved by concatenating with existing medical notes
     * - This creates a history of all previous medical notes/prescriptions
     * 
     * Examples:
     * - Update only basic info: {"fullName": "John Doe", "email": "john@email.com"}
     * - Update only vitals: {"heightCm": 175.5, "weightKg": 70.0, "bloodPressureSystolic": 120}
     * - Update only medications: {"currentMedications": ["Vitamin D", "Calcium"]}
     * - Update medical notes: {"medicalNotes": "New diagnosis: Common cold"} 
     *   (Previous notes will be moved to prescription field automatically)
     * - Update mixed fields: {"city": "Mumbai", "bloodGroup": "A+", "allergies": ["Peanuts"]}
     */
    @PutMapping("/{userId}/edit-profile")
    public ResponseEntity<UserProfileDto> editUserProfile(
            @PathVariable("userId") Long userId,
            @RequestBody UserProfileDto userProfileDto) {
        
        try {
            UserProfileDto updatedProfile = medicalProfileService.updateUserProfile(userId, userProfileDto);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
