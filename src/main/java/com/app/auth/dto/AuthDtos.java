package com.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthDtos {

    // ── Check if mobile number is registered ──────────────────────────────
    public static class CheckMobileRequest {
        @NotBlank
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Enter a valid 10-digit mobile number")
        private String mobileNumber;

        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    }

    public static class CheckMobileResponse {
        private boolean exists;
        public CheckMobileResponse(boolean exists) { this.exists = exists; }
        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }
    }

    // ── Register: mobile + PIN + role (+ fullName optional) ───────────────
    public static class RegisterRequest {
        @NotBlank
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Enter a valid 10-digit mobile number")
        private String mobileNumber;

        @NotBlank
        @Pattern(regexp = "^[0-9]{4,6}$", message = "PIN must be 4-6 digits")
        private String pin;

        @NotBlank
        private String role; // USER or DOCTOR

        private String fullName;

        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        public String getPin() { return pin; }
        public void setPin(String pin) { this.pin = pin; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

    // ── Login: mobile + PIN ────────────────────────────────────────────────
    public static class LoginRequest {
        @NotBlank
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Enter a valid 10-digit mobile number")
        private String mobileNumber;

        @NotBlank
        @Pattern(regexp = "^[0-9]{4,6}$", message = "PIN must be 4-6 digits")
        private String pin;

        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        public String getPin() { return pin; }
        public void setPin(String pin) { this.pin = pin; }
    }

    // ── Auth response: token + user info ──────────────────────────────────
    public static class AuthResponse {
        private String accessToken;
        private Long id;
        private String role;
        private String fullName;
        private String mobileNumber;

        public AuthResponse(String accessToken, Long id, String role, String fullName, String mobileNumber) {
            this.accessToken = accessToken;
            this.id = id;
            this.role = role;
            this.fullName = fullName;
            this.mobileNumber = mobileNumber;
        }

        public String getAccessToken() { return accessToken; }
        public Long getId() { return id; }
        public String getRole() { return role; }
        public String getFullName() { return fullName; }
        public String getMobileNumber() { return mobileNumber; }
    }
}
