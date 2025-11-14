package com.example.auth.service;

public interface OtpService {
    
    /**
     * Send OTP via SMS to the specified mobile number
     * @param mobileNumber recipient's mobile number
     * @param otpCode the OTP code to send
     * @return true if OTP was sent successfully, false otherwise
     * @throws Exception if there's an error sending the OTP
     */
    boolean sendOtp(String mobileNumber, String otpCode) throws Exception;
}