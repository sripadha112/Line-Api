package com.app.auth.dto;

import java.time.OffsetDateTime;

public class WorkspaceAppointmentDto {
    private Long appointmentId;
    private Long userId;
    private String patientName;
    private String mobileNumber;
    private OffsetDateTime appointmentTime;
    private String appointmentDate;
    private String timeSlot;
    private String status;
    private Integer queuePosition;
    
    // Family member field - if present, this is a family appointment
    private Long patientMemberId;
    
    // Medical details from user_details table
    private Integer age;
    private Double weightKg;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private String bloodGroup;
    private String notes;

    // Constructors
    public WorkspaceAppointmentDto() {}

    // Getters and Setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public OffsetDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(OffsetDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getQueuePosition() { return queuePosition; }
    public void setQueuePosition(Integer queuePosition) { this.queuePosition = queuePosition; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Integer getBloodPressureSystolic() { return bloodPressureSystolic; }
    public void setBloodPressureSystolic(Integer bloodPressureSystolic) { this.bloodPressureSystolic = bloodPressureSystolic; }

    public Integer getBloodPressureDiastolic() { return bloodPressureDiastolic; }
    public void setBloodPressureDiastolic(Integer bloodPressureDiastolic) { this.bloodPressureDiastolic = bloodPressureDiastolic; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getPatientMemberId() { return patientMemberId; }
    public void setPatientMemberId(Long patientMemberId) { this.patientMemberId = patientMemberId; }
}
