package com.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailableSlotsResponseDto {
    private Map<String, List<String>> slotsByDate; // Date -> List of available slots
    private Long doctorId;
    private Long workplaceId;
    private String workplaceName;
    private String doctorName;
    
    // Blocked dates information
    private Map<String, BlockedDateInfo> blockedDates; // Date -> BlockedDateInfo (for full day blocks)

    // Inner class for blocked date information
    public static class BlockedDateInfo {
        @JsonProperty("isBlocked")
        private boolean isBlocked;
        
        @JsonProperty("isFullDay")
        private boolean isFullDay;
        
        private String reason;
        private String startTime;
        private String endTime;

        public BlockedDateInfo() {}

        public BlockedDateInfo(boolean isBlocked, boolean isFullDay, String reason, String startTime, String endTime) {
            this.isBlocked = isBlocked;
            this.isFullDay = isFullDay;
            this.reason = reason;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        // Getters and Setters
        @JsonProperty("isBlocked")
        public boolean isBlocked() { return isBlocked; }
        public void setBlocked(boolean blocked) { isBlocked = blocked; }

        @JsonProperty("isFullDay")
        public boolean isFullDay() { return isFullDay; }
        public void setFullDay(boolean fullDay) { isFullDay = fullDay; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }

    // Constructors
    public AvailableSlotsResponseDto() {
        this.blockedDates = new HashMap<>();
    }

    public AvailableSlotsResponseDto(Map<String, List<String>> slotsByDate, Long doctorId, Long workplaceId, String workplaceName, String doctorName) {
        this.slotsByDate = slotsByDate;
        this.doctorId = doctorId;
        this.workplaceId = workplaceId;
        this.workplaceName = workplaceName;
        this.doctorName = doctorName;
        this.blockedDates = new HashMap<>();
    }

    // Getters and Setters
    public Map<String, List<String>> getSlotsByDate() {
        return slotsByDate;
    }

    public void setSlotsByDate(Map<String, List<String>> slotsByDate) {
        this.slotsByDate = slotsByDate;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(Long workplaceId) {
        this.workplaceId = workplaceId;
    }

    public String getWorkplaceName() {
        return workplaceName;
    }

    public void setWorkplaceName(String workplaceName) {
        this.workplaceName = workplaceName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Map<String, BlockedDateInfo> getBlockedDates() {
        return blockedDates;
    }

    public void setBlockedDates(Map<String, BlockedDateInfo> blockedDates) {
        this.blockedDates = blockedDates;
    }

    public void addBlockedDate(String date, BlockedDateInfo info) {
        if (this.blockedDates == null) {
            this.blockedDates = new HashMap<>();
        }
        this.blockedDates.put(date, info);
    }
}
