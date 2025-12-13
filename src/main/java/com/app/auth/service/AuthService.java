package com.app.auth.service;

import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.OtpRequestDto;
import com.app.auth.dto.OtpVerifyRequestDto;
import com.app.auth.dto.OtpVerifySimpleDto;

public interface AuthService {
    void requestOtp(OtpRequestDto request);
    AuthResponse verifyOtp(OtpVerifyRequestDto request);
    AuthResponse verifyOtpSimple(OtpVerifySimpleDto request);
    void logout(String token);
}
