package com.app.auth.service.impl;

import com.app.auth.dto.UserProfileDto;
import com.app.auth.entity.UserDetails;
import com.app.auth.repository.UserDetailsRepository;
import com.app.auth.service.UserMedicalProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserMedicalProfileServiceImpl implements UserMedicalProfileService {

    private final UserDetailsRepository userDetailsRepository;

    public UserMedicalProfileServiceImpl(UserDetailsRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserCompleteProfile(Long userId) {
        Optional<UserDetails> userOpt = userDetailsRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }

        UserDetails user = userOpt.get();
        return convertToUserProfileDto(user);
    }

    @Override
    public UserProfileDto updateUserProfile(Long userId, UserProfileDto userProfileDto) {
        // Get existing user
        Optional<UserDetails> userOpt = userDetailsRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        UserDetails user = userOpt.get();
        
        // Update user with new data (only update non-null fields)
        updateUserFromDto(user, userProfileDto);

        // Save and return
        UserDetails savedUser = userDetailsRepository.save(user);
        return convertToUserProfileDto(savedUser);
    }

    // Helper methods for conversion

    private UserProfileDto convertToUserProfileDto(UserDetails user) {
        UserProfileDto dto = new UserProfileDto();
        
        // Basic user details
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setAddress(user.getAddress());
        dto.setCity(user.getCity());
        dto.setState(user.getState());
        dto.setPincode(user.getPincode());
        dto.setCountry(user.getCountry());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Medical profile fields
        dto.setHeightCm(user.getHeightCm());
        dto.setWeightKg(user.getWeightKg());
        dto.setAge(user.getAge());
        dto.setBloodPressureSystolic(user.getBloodPressureSystolic());
        dto.setBloodPressureDiastolic(user.getBloodPressureDiastolic());
        dto.setBloodOxygenLevel(user.getBloodOxygenLevel());
        dto.setHeartRate(user.getHeartRate());
        dto.setBodyTemperature(user.getBodyTemperature());

        dto.setBloodGroup(user.getBloodGroup());
        dto.setHasDiabetes(user.getHasDiabetes());
        dto.setHasHypertension(user.getHasHypertension());
        dto.setHasHeartDisease(user.getHasHeartDisease());
        dto.setHasKidneyDisease(user.getHasKidneyDisease());
        dto.setHasLiverDisease(user.getHasLiverDisease());

        // Convert comma-separated strings to lists
        dto.setCurrentMedications(stringToList(user.getCurrentMedicationsStr()));
        dto.setAllergies(stringToList(user.getAllergiesStr()));
        dto.setChronicDiseases(stringToList(user.getChronicDiseasesStr()));
        dto.setPreviousSurgeries(stringToList(user.getPreviousSurgeriesStr()));
        dto.setVaccinations(stringToList(user.getVaccinationsStr()));

        dto.setEmergencyContactName(user.getEmergencyContactName());
        dto.setEmergencyContactNumber(user.getEmergencyContactNumber());
        dto.setEmergencyContactRelation(user.getEmergencyContactRelation());

        dto.setMedicalNotes(user.getMedicalNotes());
        dto.setPrescription(user.getPrescription());
        dto.setFamilyMedicalHistory(user.getFamilyMedicalHistory());

        return dto;
    }

    private void updateUserFromDto(UserDetails user, UserProfileDto dto) {
        // Basic user details (only update if not null)
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getMobileNumber() != null) user.setMobileNumber(dto.getMobileNumber());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getState() != null) user.setState(dto.getState());
        if (dto.getPincode() != null) user.setPincode(dto.getPincode());
        if (dto.getCountry() != null) user.setCountry(dto.getCountry());

        // Medical profile fields
        if (dto.getHeightCm() != null) user.setHeightCm(dto.getHeightCm());
        if (dto.getWeightKg() != null) user.setWeightKg(dto.getWeightKg());
        if (dto.getAge() != null) user.setAge(dto.getAge());
        if (dto.getBloodPressureSystolic() != null) user.setBloodPressureSystolic(dto.getBloodPressureSystolic());
        if (dto.getBloodPressureDiastolic() != null) user.setBloodPressureDiastolic(dto.getBloodPressureDiastolic());
        if (dto.getBloodOxygenLevel() != null) user.setBloodOxygenLevel(dto.getBloodOxygenLevel());
        if (dto.getHeartRate() != null) user.setHeartRate(dto.getHeartRate());
        if (dto.getBodyTemperature() != null) user.setBodyTemperature(dto.getBodyTemperature());

        if (dto.getBloodGroup() != null) user.setBloodGroup(dto.getBloodGroup());
        if (dto.getHasDiabetes() != null) user.setHasDiabetes(dto.getHasDiabetes());
        if (dto.getHasHypertension() != null) user.setHasHypertension(dto.getHasHypertension());
        if (dto.getHasHeartDisease() != null) user.setHasHeartDisease(dto.getHasHeartDisease());
        if (dto.getHasKidneyDisease() != null) user.setHasKidneyDisease(dto.getHasKidneyDisease());
        if (dto.getHasLiverDisease() != null) user.setHasLiverDisease(dto.getHasLiverDisease());

        // Convert lists to comma-separated strings
        if (dto.getCurrentMedications() != null) user.setCurrentMedicationsStr(listToString(dto.getCurrentMedications()));
        if (dto.getAllergies() != null) user.setAllergiesStr(listToString(dto.getAllergies()));
        if (dto.getChronicDiseases() != null) user.setChronicDiseasesStr(listToString(dto.getChronicDiseases()));
        if (dto.getPreviousSurgeries() != null) user.setPreviousSurgeriesStr(listToString(dto.getPreviousSurgeries()));
        if (dto.getVaccinations() != null) user.setVaccinationsStr(listToString(dto.getVaccinations()));

        if (dto.getEmergencyContactName() != null) user.setEmergencyContactName(dto.getEmergencyContactName());
        if (dto.getEmergencyContactNumber() != null) user.setEmergencyContactNumber(dto.getEmergencyContactNumber());
        if (dto.getEmergencyContactRelation() != null) user.setEmergencyContactRelation(dto.getEmergencyContactRelation());

        // Handle medical notes and prescription logic
        if (dto.getMedicalNotes() != null) {
            // Set the new medical notes in the medical_notes field
            user.setMedicalNotes(dto.getMedicalNotes());
            
            // Also append the new medical notes to prescription field for history
            String newNotes = dto.getMedicalNotes();
            String existingPrescription = user.getPrescription();
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String updatedPrescription;
            if (existingPrescription != null && !existingPrescription.trim().isEmpty()) {
                // Append new notes to existing prescription
                updatedPrescription = existingPrescription + "\n\n--- Medical Notes [" + timestamp + "] ---\n" + newNotes;
            } else {
                // First time adding to prescription
                updatedPrescription = "--- Medical Notes [" + timestamp + "] ---\n" + newNotes;
            }
            
            user.setPrescription(updatedPrescription);
        } else if (dto.getPrescription() != null) {
            // Handle prescription field updates only if medical notes are not being updated
            user.setPrescription(dto.getPrescription());
        }
        
        if (dto.getFamilyMedicalHistory() != null) user.setFamilyMedicalHistory(dto.getFamilyMedicalHistory());
    }

    // Utility methods for converting between List<String> and comma-separated strings
    private List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.joining(","));
    }
}
