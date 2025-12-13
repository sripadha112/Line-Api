package com.app.auth.dto;

public class BookAppointmentResponse {
    private String message;
    private String workplaceName;
    private String slot;

    public BookAppointmentResponse() {}

    public BookAppointmentResponse(String message, String workplaceName, String slot) {
        this.message = message;
        this.workplaceName = workplaceName;
        this.slot = slot;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getWorkplaceName() { return workplaceName; }
    public void setWorkplaceName(String workplaceName) { this.workplaceName = workplaceName; }

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }
}
