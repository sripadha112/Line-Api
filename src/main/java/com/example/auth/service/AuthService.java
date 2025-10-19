package com.example.auth.service;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.OtpRequestDto;
import com.example.auth.dto.OtpVerifyRequestDto;
import com.example.auth.dto.OtpVerifySimpleDto;

public interface AuthService {
    void requestOtp(OtpRequestDto request);
    AuthResponse verifyOtp(OtpVerifyRequestDto request);
    AuthResponse verifyOtpSimple(OtpVerifySimpleDto request);
    void logout(String token);
}
