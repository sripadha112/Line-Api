package com.app.auth.dto;

public enum AlertStatus {
    BOOKED("Appointment Booked"),
    RESCHEDULED("Appointment Rescheduled"),
    CANCELLED("Appointment Cancelled"),
    COMPLETED("Appointment Completed");
    
    private final String displayName;
    
    AlertStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}