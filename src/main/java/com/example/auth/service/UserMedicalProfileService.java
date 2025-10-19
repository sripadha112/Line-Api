package com.example.auth.service;

import com.example.auth.dto.UserProfileDto;

public interface UserMedicalProfileService {
    
    /**
     * Get user's complete profile including medical data
     */
    UserProfileDto getUserCompleteProfile(Long userId);
    
    /**
     * Update user's profile including medical data
     */
    UserProfileDto updateUserProfile(Long userId, UserProfileDto userProfileDto);
}
