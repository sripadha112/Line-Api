package com.example.auth.dto;

import java.time.OffsetDateTime;

/**
 * DTO for individual appointment in doctor's history
 * Contains patient details and appointment information
 */
public class DoctorHistoryAppointmentDto {
    
    private Long appointmentId;
    private Long userId;
    private String patientFullName;
    private Integer age;
    private String mobileNumber;
    private String appointmentDate;
    private String timeSlot;
    private OffsetDateTime appointmentTime;
    private String status;
    
    public DoctorHistoryAppointmentDto() {}
    
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getPatientFullName() {
        return patientFullName;
    }
    
    public void setPatientFullName(String patientFullName) {
        this.patientFullName = patientFullName;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getMobileNumber() {
        return mobileNumber;
    }
    
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
    
    public String getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public String getTimeSlot() {
        return timeSlot;
    }
    
    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }
    
    public OffsetDateTime getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(OffsetDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
