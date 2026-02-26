package com.app.auth.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * Request DTO for creating a blocked time slot
 */
public class BlockSlotRequest {
    
    @NotNull(message = "Date is required")
    private String date; // Format: YYYY-MM-DD
    
    private Long workplaceId; // null means all workplaces
    
    private String startTime; // Format: HH:mm (null for full day)
    
    private String endTime; // Format: HH:mm (null for full day)
    
    private Boolean isFullDay = false; // true if blocking entire day
    
    private String reason;
    
    private Boolean cancelExistingAppointments = true; // Whether to cancel existing appointments in blocked time

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Long getWorkplaceId() { return workplaceId; }
    public void setWorkplaceId(Long workplaceId) { this.workplaceId = workplaceId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Boolean getIsFullDay() { return isFullDay; }
    public void setIsFullDay(Boolean isFullDay) { this.isFullDay = isFullDay; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public Boolean getCancelExistingAppointments() { return cancelExistingAppointments; }
    public void setCancelExistingAppointments(Boolean cancelExistingAppointments) { 
        this.cancelExistingAppointments = cancelExistingAppointments; 
    }
}
