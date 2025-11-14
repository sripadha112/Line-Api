package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class AlertRequestDto {
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid mobile number format")
    private String mobileNumber;
    
    @NotNull(message = "Appointment details are required")
    private AppointmentAlertDto appointmentDetails;
    
    @NotNull(message = "Status is required")
    private AlertStatus status;
    
    // Getters and Setters
    public String getMobileNumber() {
        return mobileNumber;
    }
    
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
    
    public AppointmentAlertDto getAppointmentDetails() {
        return appointmentDetails;
    }
    
    public void setAppointmentDetails(AppointmentAlertDto appointmentDetails) {
        this.appointmentDetails = appointmentDetails;
    }
    
    public AlertStatus getStatus() {
        return status;
    }
    
    public void setStatus(AlertStatus status) {
        this.status = status;
    }
}