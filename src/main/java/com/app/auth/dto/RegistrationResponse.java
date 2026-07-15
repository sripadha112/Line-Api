package com.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationResponse {
    private Long doctorId;
    private String status;
    private String message;
    private Long userId;
    private String accessToken;   // NEW
    private String role;          // NEW
    private String fullName;

    public RegistrationResponse() {}

    public RegistrationResponse(String message, String success) {
        this.message = message;
        this.status = success;
    }

    public static RegistrationResponse success(String message, Long userId, String accessToken, String role, String fullName) {
        RegistrationResponse r = new RegistrationResponse();
        r.status = "SUCCESS";
        r.message = message;
        r.userId = userId;
        r.accessToken = accessToken;
        r.role = role;
        r.fullName = fullName;
        return r;
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

//    public static RegistrationResponse successDoctor(String message, Long doctorId) {
//        RegistrationResponse response = new RegistrationResponse(message, "SUCCESS");
//        response.setDoctorId(doctorId);
//        return response;
//    }

    public static RegistrationResponse error(String message) {
        return new RegistrationResponse(message, "ERROR");
    }

    public static RegistrationResponse error(String message, Long userId) {
        RegistrationResponse response = new RegistrationResponse(message, "ERROR");
        response.setUserId(userId);
        return response;
    }

    public static RegistrationResponse successDoctor(String doctorRegistrationSuccessful, Long id, String token, String role, String fullName) {
        RegistrationResponse response = new RegistrationResponse(doctorRegistrationSuccessful, "SUCCESS");
        response.setDoctorId(id);
        response.setFullName(fullName);
        response.setAccessToken(token);
        response.setRole(role);
        return response;
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

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
