package com.example.auth.dto;

import java.time.OffsetDateTime;

public class AppointmentDto {
    private Long id;
    private Long doctorId;
    private Long userId;
    private DoctorWorkplaceDto workplace;
    private DoctorSlotDto slot;
    private OffsetDateTime appointmentTime;
    private Integer durationMinutes;
    private Integer queuePosition;
    private String status;
    private String notes;

    // getters & setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public DoctorWorkplaceDto getWorkplace() { return workplace; }
    public void setWorkplace(DoctorWorkplaceDto workplace) { this.workplace = workplace; }
    
    public DoctorSlotDto getSlot() { return slot; }
    public void setSlot(DoctorSlotDto slot) { this.slot = slot; }
    
    public OffsetDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(OffsetDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getQueuePosition() { return queuePosition; }
    public void setQueuePosition(Integer queuePosition) { this.queuePosition = queuePosition; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
