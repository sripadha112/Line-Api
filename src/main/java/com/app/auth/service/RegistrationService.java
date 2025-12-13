package com.app.auth.service;

import com.app.auth.dto.DoctorRegistrationDto;
import com.app.auth.dto.DoctorResponseDto;
import com.app.auth.dto.RegistrationResponse;
import com.app.auth.dto.UserRegistrationDto;
import com.app.auth.entity.DoctorDetails;
import com.app.auth.entity.UserDetails;

public interface RegistrationService {
    
    RegistrationResponse registerUser(UserRegistrationDto userRegistrationDto);
    
    RegistrationResponse registerDoctor(DoctorRegistrationDto doctorRegistrationDto);
    
    UserDetails getUserByMobileNumber(String mobileNumber);
    
    DoctorDetails getDoctorByMobileNumber(String mobileNumber);
    
    UserDetails getUserById(Long userId);
    
    DoctorDetails getDoctorById(Long doctorId);
    
    DoctorResponseDto getDoctorResponseById(Long doctorId);
}
