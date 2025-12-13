package com.app.auth.controller;

import com.app.auth.dto.DoctorRegistrationDto;
import com.app.auth.dto.DoctorResponseDto;
import com.app.auth.dto.RegistrationResponse;
import com.app.auth.dto.UserRegistrationDto;
import com.app.auth.dto.UserProfileDto;
import com.app.auth.entity.DoctorDetails;
import com.app.auth.entity.UserDetails;
import com.app.auth.service.RegistrationService;
import com.app.auth.service.UserMedicalProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserMedicalProfileService medicalProfileService;

    public RegistrationController(RegistrationService registrationService,
                                  UserMedicalProfileService medicalProfileService) {
        this.registrationService = registrationService;
        this.medicalProfileService = medicalProfileService;
    }

    @PostMapping("/register/user")
    public ResponseEntity<RegistrationResponse> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        RegistrationResponse response = registrationService.registerUser(userRegistrationDto);
        
        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/registration/doctor")
    public ResponseEntity<RegistrationResponse> registerDoctor(@Valid @RequestBody DoctorRegistrationDto doctorRegistrationDto) {
        RegistrationResponse response = registrationService.registerDoctor(doctorRegistrationDto);
        
        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable("userId") Long userId) {
        UserProfileDto userProfile = medicalProfileService.getUserCompleteProfile(userId);
        
        if (userProfile != null) {
            return ResponseEntity.ok(userProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<DoctorResponseDto> getDoctorById(@PathVariable("doctorId") Long doctorId) {
        DoctorResponseDto doctor = registrationService.getDoctorResponseById(doctorId);
        
        if (doctor != null) {
            return ResponseEntity.ok(doctor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/mobile/{mobileNumber}")
    public ResponseEntity<UserProfileDto> getUserByMobileNumber(@PathVariable("mobileNumber") String mobileNumber) {
        UserDetails user = registrationService.getUserByMobileNumber(mobileNumber);
        
        if (user != null) {
            UserProfileDto userProfile = medicalProfileService.getUserCompleteProfile(user.getId());
            return ResponseEntity.ok(userProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/doctor/mobile/{mobileNumber}")
    public ResponseEntity<DoctorDetails> getDoctorByMobileNumber(@PathVariable("mobileNumber") String mobileNumber) {
        DoctorDetails doctor = registrationService.getDoctorByMobileNumber(mobileNumber);
        
        if (doctor != null) {
            return ResponseEntity.ok(doctor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
