package com.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OtpVerifySimpleDto {

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp="^\\d{10}$", message = "Mobile number must be 10-15 digits")
    private String mobileNumber;

    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 6, message = "OTP must be 4-6 digits")
    private String otpCode;

    // Constructors
    public OtpVerifySimpleDto() {}

    public OtpVerifySimpleDto(String mobileNumber, String otpCode) {
        this.mobileNumber = mobileNumber;
        this.otpCode = otpCode;
    }

    // Getters and Setters
    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
