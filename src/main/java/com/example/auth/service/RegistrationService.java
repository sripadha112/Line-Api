package com.example.auth.service;

import com.example.auth.dto.DoctorRegistrationDto;
import com.example.auth.dto.DoctorResponseDto;
import com.example.auth.dto.RegistrationResponse;
import com.example.auth.dto.UserRegistrationDto;
import com.example.auth.entity.DoctorDetails;
import com.example.auth.entity.UserDetails;

public interface RegistrationService {
    
    RegistrationResponse registerUser(UserRegistrationDto userRegistrationDto);
    
    RegistrationResponse registerDoctor(DoctorRegistrationDto doctorRegistrationDto);
    
    UserDetails getUserByMobileNumber(String mobileNumber);
    
    DoctorDetails getDoctorByMobileNumber(String mobileNumber);
    
    UserDetails getUserById(Long userId);
    
    DoctorDetails getDoctorById(Long doctorId);
    
    DoctorResponseDto getDoctorResponseById(Long doctorId);
}
