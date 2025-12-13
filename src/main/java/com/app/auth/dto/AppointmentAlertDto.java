package com.app.auth.dto;

import java.time.OffsetDateTime;

public class AppointmentAlertDto {
    private Long appointmentId;
    private String patientName;
    private OffsetDateTime appointmentTime;
    private Integer durationMinutes;
    private Integer queuePosition;
    private String notes;
    private DoctorAlertDto doctor;
    private WorkplaceAlertDto workplace;
    
    // Getters and Setters
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public OffsetDateTime getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(OffsetDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public Integer getQueuePosition() {
        return queuePosition;
    }
    
    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public DoctorAlertDto getDoctor() {
        return doctor;
    }
    
    public void setDoctor(DoctorAlertDto doctor) {
        this.doctor = doctor;
    }
    
    public WorkplaceAlertDto getWorkplace() {
        return workplace;
    }
    
    public void setWorkplace(WorkplaceAlertDto workplace) {
        this.workplace = workplace;
    }
}