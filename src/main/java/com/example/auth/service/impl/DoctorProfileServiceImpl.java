package com.example.auth.service.impl;

import com.example.auth.dto.DoctorProfileUpdateDto;
import com.example.auth.entity.DoctorDetails;
import com.example.auth.entity.DoctorWorkplace;
import com.example.auth.repository.DoctorDetailsRepository;
import com.example.auth.repository.DoctorWorkplaceRepository;
import com.example.auth.service.DoctorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DoctorProfileServiceImpl implements DoctorProfileService {

    @Autowired
    private DoctorDetailsRepository doctorDetailsRepository;

    @Autowired
    private DoctorWorkplaceRepository doctorWorkplaceRepository;

    @Override
    public DoctorProfileUpdateResponse updateDoctorProfile(DoctorDetails doctor, DoctorProfileUpdateDto updateRequest) {
        int fieldsUpdated = 0;

        // Update basic profile fields (only if provided)
        fieldsUpdated += updateBasicProfileFields(doctor, updateRequest);

        // Handle workspace updates
        int workspaceUpdates = 0;
        if (updateRequest.getWorkspaces() != null && !updateRequest.getWorkspaces().isEmpty()) {
            workspaceUpdates = handleWorkspaceUpdates(doctor, updateRequest.getWorkspaces());
        }

        // Save the updated doctor profile
        doctorDetailsRepository.save(doctor);

        return new DoctorProfileUpdateResponse(
            "Doctor profile updated successfully",
            fieldsUpdated,
            workspaceUpdates
        );
    }

    /**
     * Update basic profile fields (only if provided and not empty)
     */
    private int updateBasicProfileFields(DoctorDetails doctor, DoctorProfileUpdateDto updateRequest) {
        int updateCount = 0;

        if (isNotEmptyString(updateRequest.getFullName())) {
            doctor.setFullName(updateRequest.getFullName());
            updateCount++;
        }
        if (isNotEmptyString(updateRequest.getMobileNumber())) {
            doctor.setMobileNumber(updateRequest.getMobileNumber());
            updateCount++;
        }
        if (isNotEmptyString(updateRequest.getEmail())) {
            doctor.setEmail(updateRequest.getEmail());
            updateCount++;
        }
        if (isNotEmptyString(updateRequest.getAddress())) {
            doctor.setAddress(updateRequest.getAddress());
            updateCount++;
        }
        if (isNotEmptyString(updateRequest.getCity())) {
            doctor.setCity(updateRequest.getCity());
            updateCount++;
        }
        if (isNotEmptyString(updateRequest.getState())) {
            doctor.setState(updateRequest.getState());
            updateCount++;
        }
        if (isNotEmptyString(updateRequest.getPincode())) {
            doctor.setPincode(updateRequest.getPincode());
            updateCount++;
        }
        if (isNotEmptyString(updateRequest.getCountry())) {
            doctor.setCountry(updateRequest.getCountry());
            updateCount++;
        }
        if (isNotEmptyString(updateRequest.getSpecialization())) {
            doctor.setSpecialization(updateRequest.getSpecialization());
            updateCount++;
        }
        if (isNotEmptyString(updateRequest.getDesignation())) {
            doctor.setDesignation(updateRequest.getDesignation());
            updateCount++;
        }

        return updateCount;
    }

    /**
     * Check if string is not null and not empty/blank
     */
    private boolean isNotEmptyString(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Handle workspace updates (create new or update existing)
     */
    private int handleWorkspaceUpdates(DoctorDetails doctor, List<DoctorProfileUpdateDto.WorkspaceUpdateDto> workspaceUpdates) {
        int processedCount = 0;

        for (DoctorProfileUpdateDto.WorkspaceUpdateDto workspaceDto : workspaceUpdates) {
            if (workspaceDto.getId() != null) {
                // Update existing workspace
                Optional<DoctorWorkplace> existingOpt = doctorWorkplaceRepository.findById(workspaceDto.getId());
                if (existingOpt.isPresent()) {
                    DoctorWorkplace existing = existingOpt.get();
                    updateWorkspaceFields(existing, workspaceDto);
                    doctorWorkplaceRepository.save(existing);
                    processedCount++;
                }
            } else {
                // Create new workspace
                DoctorWorkplace newWorkplace = new DoctorWorkplace();
                newWorkplace.setDoctor(doctor); // Set the doctor entity relationship
                updateWorkspaceFields(newWorkplace, workspaceDto);
                doctorWorkplaceRepository.save(newWorkplace);
                processedCount++;
            }
        }

        return processedCount;
    }

    /**
     * Update workspace fields (only if provided and not empty)
     */
    private void updateWorkspaceFields(DoctorWorkplace workplace, DoctorProfileUpdateDto.WorkspaceUpdateDto dto) {
        if (isNotEmptyString(dto.getWorkplaceName())) {
            workplace.setWorkplaceName(dto.getWorkplaceName());
        }
        if (isNotEmptyString(dto.getWorkplaceType())) {
            workplace.setWorkplaceType(dto.getWorkplaceType());
        }
        if (isNotEmptyString(dto.getAddress())) {
            workplace.setAddress(dto.getAddress());
        }
        if (isNotEmptyString(dto.getCity())) {
            workplace.setCity(dto.getCity());
        }
        if (isNotEmptyString(dto.getState())) {
            workplace.setState(dto.getState());
        }
        if (isNotEmptyString(dto.getPincode())) {
            workplace.setPincode(dto.getPincode());
        }
        if (isNotEmptyString(dto.getContactNumber())) {
            workplace.setContactNumber(dto.getContactNumber());
        }
        if (dto.getMorningStartTime() != null) {
            workplace.setMorningStartTime(dto.getMorningStartTime());
        }
        if (dto.getMorningEndTime() != null) {
            workplace.setMorningEndTime(dto.getMorningEndTime());
        }
        if (dto.getEveningStartTime() != null) {
            workplace.setEveningStartTime(dto.getEveningStartTime());
        }
        if (dto.getEveningEndTime() != null) {
            workplace.setEveningEndTime(dto.getEveningEndTime());
        }
        if (dto.getCheckingDurationMinutes() != null && dto.getCheckingDurationMinutes() > 0) {
            workplace.setCheckingDurationMinutes(dto.getCheckingDurationMinutes());
        }
        if (dto.getIsPrimary() != null) {
            workplace.setIsPrimary(dto.getIsPrimary());
        }
    }
}
