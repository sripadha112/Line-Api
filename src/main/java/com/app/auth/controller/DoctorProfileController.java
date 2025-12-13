package com.app.auth.controller;

import com.app.auth.dto.DoctorDetailsEnhancedDto;
import com.app.auth.dto.DoctorProfileUpdateDto;
import com.app.auth.dto.DoctorWorkplaceDto;
import com.app.auth.entity.DoctorDetails;
import com.app.auth.entity.DoctorWorkplace;
import com.app.auth.repository.DoctorDetailsRepository;
import com.app.auth.repository.DoctorWorkplaceRepository;
import com.app.auth.service.DoctorProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
public class DoctorProfileController {

    private final DoctorDetailsRepository doctorRepository;
    private final DoctorWorkplaceRepository workplaceRepository;
    private final DoctorProfileService doctorProfileService;

    public DoctorProfileController(DoctorDetailsRepository doctorRepository,
                                 DoctorWorkplaceRepository workplaceRepository,
                                 DoctorProfileService doctorProfileService) {
        this.doctorRepository = doctorRepository;
        this.workplaceRepository = workplaceRepository;
        this.doctorProfileService = doctorProfileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<DoctorDetailsEnhancedDto> getDoctorProfile() {
        // Extract doctor ID from JWT token
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Parse the subject (format: "DOCTOR:123")
        if (authentication == null || !authentication.startsWith("DOCTOR:")) {
            return ResponseEntity.badRequest().build();
        }
        
        Long doctorId;
        try {
            doctorId = Long.parseLong(authentication.substring(7)); // Remove "DOCTOR:" prefix
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        Optional<DoctorDetails> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        DoctorDetails doctor = doctorOpt.get();
        List<DoctorWorkplace> workplaces = workplaceRepository.findByDoctorId(doctorId);

        DoctorDetailsEnhancedDto dto = convertToDto(doctor, workplaces);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{doctorId}/profile")
    public ResponseEntity<DoctorDetailsEnhancedDto> getDoctorProfileById(@PathVariable("doctorId") Long doctorId) {
        Optional<DoctorDetails> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        DoctorDetails doctor = doctorOpt.get();
        List<DoctorWorkplace> workplaces = workplaceRepository.findByDoctorId(doctorId);

        DoctorDetailsEnhancedDto dto = convertToDto(doctor, workplaces);
        return ResponseEntity.ok(dto);
    }

    private DoctorDetailsEnhancedDto convertToDto(DoctorDetails doctor, List<DoctorWorkplace> workplaces) {
        DoctorDetailsEnhancedDto dto = new DoctorDetailsEnhancedDto();
        dto.setId(doctor.getId());
        dto.setFullName(doctor.getFullName());
        dto.setEmail(doctor.getEmail());
        dto.setMobileNumber(doctor.getMobileNumber());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setDesignation(doctor.getDesignation());
        dto.setAddress(doctor.getAddress());
        dto.setPincode(doctor.getPincode());

        List<DoctorWorkplaceDto> workplaceDtos = workplaces.stream()
                .map(this::convertWorkplaceToDto)
                .collect(Collectors.toList());
        dto.setWorkplaces(workplaceDtos);

        return dto;
    }

    private DoctorWorkplaceDto convertWorkplaceToDto(DoctorWorkplace workplace) {
        DoctorWorkplaceDto dto = new DoctorWorkplaceDto();
        dto.setId(workplace.getId());
        dto.setWorkplaceName(workplace.getWorkplaceName());
        dto.setWorkplaceType(workplace.getWorkplaceType());
        dto.setAddress(workplace.getAddress());
        dto.setContactNumber(workplace.getContactNumber());
        dto.setIsPrimary(workplace.getIsPrimary());
        return dto;
    }

    /**
     * Update doctor profile with flexible field updates
     * Only provided fields will be updated, supports partial updates
     */
    @PutMapping("/edit-profile")
    public ResponseEntity<Map<String, Object>> updateDoctorProfile(@Valid @RequestBody DoctorProfileUpdateDto updateRequest) {
        // Extract doctor ID from JWT token
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (authentication == null || !authentication.startsWith("DOCTOR:")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid authentication"));
        }
        
        Long doctorId;
        try {
            doctorId = Long.parseLong(authentication.substring(7));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid doctor ID"));
        }

        try {
            // Find doctor
            Optional<DoctorDetails> doctorOpt = doctorRepository.findById(doctorId);
            if (!doctorOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            DoctorDetails doctor = doctorOpt.get();

            // Use service to update profile
            DoctorProfileService.DoctorProfileUpdateResponse response = 
                doctorProfileService.updateDoctorProfile(doctor, updateRequest);

            // Build response
            Map<String, Object> responseMap = Map.of(
                "message", response.getMessage(),
                "fieldsUpdated", response.getFieldsUpdated(),
                "workspacesProcessed", response.getWorkspacesProcessed()
            );

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update doctor profile: " + e.getMessage()));
        }
    }

    /**
     * Update doctor profile by ID (for admin use)
     */
    @PutMapping("/{doctorId}/edit-profile")
    public ResponseEntity<Map<String, Object>> updateDoctorProfileById(@PathVariable("doctorId") Long doctorId,
                                                                      @Valid @RequestBody DoctorProfileUpdateDto updateRequest) {
        try {
            // Find doctor
            Optional<DoctorDetails> doctorOpt = doctorRepository.findById(doctorId);
            if (!doctorOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            DoctorDetails doctor = doctorOpt.get();

            // Use service to update profile
            DoctorProfileService.DoctorProfileUpdateResponse response = 
                doctorProfileService.updateDoctorProfile(doctor, updateRequest);

            // Build response
            Map<String, Object> responseMap = Map.of(
                "message", response.getMessage(),
                "fieldsUpdated", response.getFieldsUpdated(),
                "workspacesProcessed", response.getWorkspacesProcessed()
            );

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update doctor profile: " + e.getMessage()));
        }
    }
}
