package com.app.auth.service;

import com.app.auth.dto.UserProfileDto;

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
