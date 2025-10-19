package com.example.auth.dto;

import java.util.List;
import java.util.Map;

public class AvailableSlotsResponseDto {
    private Map<String, List<String>> slotsByDate; // Date -> List of available slots
    private Long doctorId;
    private Long workplaceId;
    private String workplaceName;
    private String doctorName;

    // Constructors
    public AvailableSlotsResponseDto() {}

    public AvailableSlotsResponseDto(Map<String, List<String>> slotsByDate, Long doctorId, Long workplaceId, String workplaceName, String doctorName) {
        this.slotsByDate = slotsByDate;
        this.doctorId = doctorId;
        this.workplaceId = workplaceId;
        this.workplaceName = workplaceName;
        this.doctorName = doctorName;
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
}
