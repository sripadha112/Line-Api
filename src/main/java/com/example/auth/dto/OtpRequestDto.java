package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpRequestDto {
    @NotBlank
    @Pattern(regexp="^\\d{10}$", message = "mobileNumber must be exactly 10 digits")
    private String mobileNumber;

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
}
