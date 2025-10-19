package com.example.auth.dto;

public class RegistrationResponse {
    private String message;
    private String status;
    private Long userId;
    private Long doctorId;

    public RegistrationResponse() {}

    public RegistrationResponse(String message, String status) {
        this.message = message;
        this.status = status;
    }

    public RegistrationResponse(String message, String status, Long userId) {
        this.message = message;
        this.status = status;
        this.userId = userId;
    }

    public static RegistrationResponse success(String message) {
        return new RegistrationResponse(message, "SUCCESS");
    }

    public static RegistrationResponse success(String message, Long userId) {
        RegistrationResponse response = new RegistrationResponse(message, "SUCCESS");
        response.setUserId(userId);
        return response;
    }

    public static RegistrationResponse successDoctor(String message, Long doctorId) {
        RegistrationResponse response = new RegistrationResponse(message, "SUCCESS");
        response.setDoctorId(doctorId);
        return response;
    }

    public static RegistrationResponse error(String message) {
        return new RegistrationResponse(message, "ERROR");
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}
