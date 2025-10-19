package com.example.auth.service.impl;

import com.example.auth.dto.DoctorRegistrationDto;
import com.example.auth.dto.DoctorResponseDto;
import com.example.auth.dto.RegistrationResponse;
import com.example.auth.dto.UserRegistrationDto;
import com.example.auth.dto.WorkplaceResponseDto;
import com.example.auth.dto.WorkspaceDto;
import com.example.auth.entity.DoctorDetails;
import com.example.auth.entity.DoctorWorkplace;
import com.example.auth.entity.UserDetails;
import com.example.auth.repository.DoctorDetailsRepository;
import com.example.auth.repository.DoctorWorkplaceRepository;
import com.example.auth.repository.UserDetailsRepository;
import com.example.auth.service.RegistrationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final UserDetailsRepository userDetailsRepository;
    private final DoctorDetailsRepository doctorDetailsRepository;
    private final DoctorWorkplaceRepository doctorWorkplaceRepository;

    public RegistrationServiceImpl(UserDetailsRepository userDetailsRepository, 
                                   DoctorDetailsRepository doctorDetailsRepository,
                                   DoctorWorkplaceRepository doctorWorkplaceRepository) {
        this.userDetailsRepository = userDetailsRepository;
        this.doctorDetailsRepository = doctorDetailsRepository;
        this.doctorWorkplaceRepository = doctorWorkplaceRepository;
    }

    @Override
    @Transactional
    public RegistrationResponse registerUser(UserRegistrationDto userRegistrationDto) {
        try {
            // Check if user already exists
            Optional<UserDetails> existingUser = userDetailsRepository.findByMobileNumber(userRegistrationDto.getMobileNumber());
            if (existingUser.isPresent()) {
                return RegistrationResponse.error("User with this mobile number already exists");
            }

            // Check if email already exists
            Optional<UserDetails> existingEmail = userDetailsRepository.findByEmail(userRegistrationDto.getEmail());
            if (existingEmail.isPresent()) {
                return RegistrationResponse.error("User with this email already exists");
            }

            // Create new user
            UserDetails userDetails = new UserDetails();
            userDetails.setMobileNumber(userRegistrationDto.getMobileNumber());
            userDetails.setFullName(userRegistrationDto.getFullName());
            userDetails.setEmail(userRegistrationDto.getEmail());
            userDetails.setAddress(userRegistrationDto.getAddress());
            userDetails.setCity(userRegistrationDto.getCity());
            userDetails.setState(userRegistrationDto.getState());
            userDetails.setPincode(userRegistrationDto.getPincode());
            userDetails.setCountry(userRegistrationDto.getCountry());
            userDetails.setCreatedAt(OffsetDateTime.now());

            UserDetails savedUser = userDetailsRepository.save(userDetails);

            return RegistrationResponse.success("User registration successful", savedUser.getId());

        } catch (DataIntegrityViolationException e) {
            return RegistrationResponse.error("Registration failed: Duplicate mobile number or email");
        } catch (Exception e) {
            return RegistrationResponse.error("Registration failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RegistrationResponse registerDoctor(DoctorRegistrationDto doctorRegistrationDto) {
        try {
            // Check if doctor already exists
            Optional<DoctorDetails> existingDoctor = doctorDetailsRepository.findByMobileNumber(doctorRegistrationDto.getMobileNumber());
            if (existingDoctor.isPresent()) {
                return RegistrationResponse.error("Doctor with this mobile number already exists");
            }

            // Check if email already exists
            Optional<DoctorDetails> existingEmail = doctorDetailsRepository.findByEmail(doctorRegistrationDto.getEmail());
            if (existingEmail.isPresent()) {
                return RegistrationResponse.error("Doctor with this email already exists");
            }

            // Create new doctor
            DoctorDetails doctorDetails = new DoctorDetails();
            doctorDetails.setMobileNumber(doctorRegistrationDto.getMobileNumber());
            doctorDetails.setFullName(doctorRegistrationDto.getFullName());
            doctorDetails.setEmail(doctorRegistrationDto.getEmail());
            doctorDetails.setAddress(doctorRegistrationDto.getAddress());
            doctorDetails.setCity(doctorRegistrationDto.getCity());
            doctorDetails.setState(doctorRegistrationDto.getState());
            doctorDetails.setPincode(doctorRegistrationDto.getPincode());
            doctorDetails.setCountry(doctorRegistrationDto.getCountry());
            doctorDetails.setSpecialization(doctorRegistrationDto.getSpecialization());
            doctorDetails.setDesignation(doctorRegistrationDto.getDesignation());
            
            doctorDetails.setCreatedAt(OffsetDateTime.now());

            DoctorDetails savedDoctor = doctorDetailsRepository.save(doctorDetails);

            // Create workspaces
            if (doctorRegistrationDto.getWorkspaces() != null && !doctorRegistrationDto.getWorkspaces().isEmpty()) {
                boolean hasPrimary = false;
                
                for (int i = 0; i < doctorRegistrationDto.getWorkspaces().size(); i++) {
                    WorkspaceDto workspaceDto = doctorRegistrationDto.getWorkspaces().get(i);
                    
                    DoctorWorkplace workplace = new DoctorWorkplace();
                    workplace.setDoctor(savedDoctor);
                    workplace.setWorkplaceName(workspaceDto.getWorkplaceName());
                    workplace.setWorkplaceType(workspaceDto.getWorkplaceType());
                    workplace.setAddress(workspaceDto.getAddress());
                    workplace.setCity(workspaceDto.getCity());
                    workplace.setState(workspaceDto.getState());
                    workplace.setPincode(workspaceDto.getPincode());
                    workplace.setCountry(workspaceDto.getCountry());
                    workplace.setMorningStartTime(workspaceDto.getMorningStartTime());
                    workplace.setMorningEndTime(workspaceDto.getMorningEndTime());
                    workplace.setEveningStartTime(workspaceDto.getEveningStartTime());
                    workplace.setEveningEndTime(workspaceDto.getEveningEndTime());
                    workplace.setCheckingDurationMinutes(workspaceDto.getCheckingDurationMinutes() != null ? 
                        workspaceDto.getCheckingDurationMinutes() : 30);
                    
                    // Set the first workspace as primary if none is explicitly marked, or if explicitly marked
                    if ((i == 0 && !hasPrimary) || (workspaceDto.getIsPrimary() != null && workspaceDto.getIsPrimary())) {
                        workplace.setIsPrimary(true);
                        hasPrimary = true;
                    } else {
                        workplace.setIsPrimary(false);
                    }
                    
                    workplace.setCreatedAt(OffsetDateTime.now());
                    doctorWorkplaceRepository.save(workplace);
                }
            }

            return RegistrationResponse.successDoctor("Doctor registration successful", savedDoctor.getId());

        } catch (DataIntegrityViolationException e) {
            return RegistrationResponse.error("Registration failed: Duplicate mobile number or email");
        } catch (Exception e) {
            return RegistrationResponse.error("Registration failed: " + e.getMessage());
        }
    }

    @Override
    public UserDetails getUserByMobileNumber(String mobileNumber) {
        return userDetailsRepository.findByMobileNumber(mobileNumber).orElse(null);
    }

    @Override
    public DoctorDetails getDoctorByMobileNumber(String mobileNumber) {
        return doctorDetailsRepository.findByMobileNumber(mobileNumber).orElse(null);
    }

    @Override
    public UserDetails getUserById(Long userId) {
        return userDetailsRepository.findById(userId).orElse(null);
    }

    @Override
    public DoctorDetails getDoctorById(Long doctorId) {
        return doctorDetailsRepository.findById(doctorId).orElse(null);
    }

    @Override
    public DoctorResponseDto getDoctorResponseById(Long doctorId) {
        Optional<DoctorDetails> doctorOpt = doctorDetailsRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return null;
        }
        
        DoctorDetails doctor = doctorOpt.get();
        return convertToDoctorResponseDto(doctor);
    }

    private DoctorResponseDto convertToDoctorResponseDto(DoctorDetails doctor) {
        DoctorResponseDto dto = new DoctorResponseDto();
        dto.setId(doctor.getId());
        dto.setFullName(doctor.getFullName());
        dto.setEmail(doctor.getEmail());
        dto.setMobileNumber(doctor.getMobileNumber());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setDesignation(doctor.getDesignation());
        dto.setAddress(doctor.getAddress());
        dto.setCity(doctor.getCity());
        dto.setState(doctor.getState());
        dto.setPincode(doctor.getPincode());
        dto.setCountry(doctor.getCountry());
        dto.setCreatedAt(doctor.getCreatedAt());

        // Convert workplaces to DTO
        if (doctor.getWorkplaces() != null) {
            List<WorkplaceResponseDto> workplaceDtos = doctor.getWorkplaces().stream()
                .map(this::convertToWorkplaceResponseDto)
                .collect(Collectors.toList());
            dto.setWorkplaces(workplaceDtos);
        }

        return dto;
    }

    private WorkplaceResponseDto convertToWorkplaceResponseDto(DoctorWorkplace workplace) {
        WorkplaceResponseDto dto = new WorkplaceResponseDto();
        dto.setId(workplace.getId());
        dto.setDoctor(workplace.getDoctor().getFullName()); // Just the name, not the full object
        dto.setWorkplaceName(workplace.getWorkplaceName());
        dto.setWorkplaceType(workplace.getWorkplaceType());
        dto.setAddress(workplace.getAddress());
        dto.setCity(workplace.getCity());
        dto.setState(workplace.getState());
        dto.setPincode(workplace.getPincode());
        dto.setCountry(workplace.getCountry());
        dto.setContactNumber(workplace.getContactNumber());
        dto.setMorningStartTime(workplace.getMorningStartTime());
        dto.setMorningEndTime(workplace.getMorningEndTime());
        dto.setEveningStartTime(workplace.getEveningStartTime());
        dto.setEveningEndTime(workplace.getEveningEndTime());
        dto.setCheckingDurationMinutes(workplace.getCheckingDurationMinutes());
        dto.setIsPrimary(workplace.getIsPrimary());
        dto.setCreatedAt(workplace.getCreatedAt());
        return dto;
    }
}
