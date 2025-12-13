package com.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class BulkAppointmentStatusUpdateDto {
    
    @NotEmpty(message = "User IDs list cannot be empty")
    private List<Long> userIds;
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(COMPLETED|RESCHEDULED|CANCELLED)$", 
             message = "Status must be one of: COMPLETED, RESCHEDULED, CANCELLED")
    private String status;
    
    private String notes;
    
    // For rescheduling - new appointment details
    private String newAppointmentDate; // YYYY-MM-DD format
    private String newTimeSlot; // e.g., "9:00AM - 9:15AM"
    private Long newWorkplaceId;

    // Constructors
    public BulkAppointmentStatusUpdateDto() {}

    // Getters and Setters
    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public Long getNewWorkplaceId() {
        return newWorkplaceId;
    }

    public void setNewWorkplaceId(Long newWorkplaceId) {
        this.newWorkplaceId = newWorkplaceId;
    }
}
