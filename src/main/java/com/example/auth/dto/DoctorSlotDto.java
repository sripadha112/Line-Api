package com.example.auth.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class DoctorSlotDto {
    private Long id;
    private Long doctorId;
    private DoctorWorkplaceDto workplace;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String sessionType;
    private Boolean isAvailable;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public DoctorWorkplaceDto getWorkplace() { return workplace; }
    public void setWorkplace(DoctorWorkplaceDto workplace) { this.workplace = workplace; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
