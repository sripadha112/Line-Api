package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserRescheduleRequestDto {
    
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    @NotBlank(message = "Reason for reschedule is required")
    private String reason;
    
    @NotBlank(message = "New appointment date is required")
    private String newAppointmentDate; // Format: yyyy-MM-dd
    
    @NotBlank(message = "New time slot is required")
    private String newTimeSlot; // Format: "HH:mm-HH:mm" (e.g., "09:00-09:30")
    
    // Default constructor
    public UserRescheduleRequestDto() {}
    
    // Constructor
    public UserRescheduleRequestDto(Long appointmentId, String reason, String newAppointmentDate, String newTimeSlot) {
        this.appointmentId = appointmentId;
        this.reason = reason;
        this.newAppointmentDate = newAppointmentDate;
        this.newTimeSlot = newTimeSlot;
    }
    
    // Getters and Setters
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getNewAppointmentDate() {
        return newAppointmentDate;
    }
    
    public void setNewAppointmentDate(String newAppointmentDate) {
        this.newAppointmentDate = newAppointmentDate;
    }
    
    public String getNewTimeSlot() {
        return newTimeSlot;
    }
    
    public void setNewTimeSlot(String newTimeSlot) {
        this.newTimeSlot = newTimeSlot;
    }
}
