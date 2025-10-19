package com.example.auth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for bulk rescheduling appointments by workspace
 * Supports extending appointments by hours/minutes or moving to specific date
 */
public class WorkspaceBulkRescheduleDto {
    
    private Long workspaceId;
    
    @Min(value = 0, message = "Hours must be non-negative")
    private Integer extendHours;
    
    @Min(value = 0, message = "Minutes must be non-negative")
    private Integer extendMinutes;
    
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
    private String newDate;
    
    private String reason;
    
    public WorkspaceBulkRescheduleDto() {}
    
    public Long getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }
    
    public Integer getExtendHours() {
        return extendHours;
    }
    
    public void setExtendHours(Integer extendHours) {
        this.extendHours = extendHours;
    }
    
    public Integer getExtendMinutes() {
        return extendMinutes;
    }
    
    public void setExtendMinutes(Integer extendMinutes) {
        this.extendMinutes = extendMinutes;
    }
    
    public String getNewDate() {
        return newDate;
    }
    
    public void setNewDate(String newDate) {
        this.newDate = newDate;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    /**
     * Check if any time extension is specified
     */
    public boolean hasTimeExtension() {
        return (extendHours != null && extendHours > 0) || (extendMinutes != null && extendMinutes > 0);
    }
    
    /**
     * Check if new date is specified
     */
    public boolean hasNewDate() {
        return newDate != null && !newDate.trim().isEmpty();
    }
    
    /**
     * Get total extension in minutes
     */
    public int getTotalExtensionMinutes() {
        int totalMinutes = 0;
        if (extendHours != null) {
            totalMinutes += extendHours * 60;
        }
        if (extendMinutes != null) {
            totalMinutes += extendMinutes;
        }
        return totalMinutes;
    }
}
