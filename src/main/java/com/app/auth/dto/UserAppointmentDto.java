package com.app.auth.dto;

import java.time.OffsetDateTime;

public class UserAppointmentDto {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long workplaceId;
    private String workplaceName;
    private String workplaceType;
    private String workplaceAddress;
    private OffsetDateTime appointmentTime;
    private String appointmentDate;
    private String slot;
    private Integer durationMinutes;
    private Integer queuePosition;
    private String status;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Constructors
    public UserAppointmentDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    public Long getWorkplaceId() { return workplaceId; }
    public void setWorkplaceId(Long workplaceId) { this.workplaceId = workplaceId; }

    public String getWorkplaceName() { return workplaceName; }
    public void setWorkplaceName(String workplaceName) { this.workplaceName = workplaceName; }

    public String getWorkplaceType() { return workplaceType; }
    public void setWorkplaceType(String workplaceType) { this.workplaceType = workplaceType; }

    public String getWorkplaceAddress() { return workplaceAddress; }
    public void setWorkplaceAddress(String workplaceAddress) { this.workplaceAddress = workplaceAddress; }

    public OffsetDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(OffsetDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getQueuePosition() { return queuePosition; }
    public void setQueuePosition(Integer queuePosition) { this.queuePosition = queuePosition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
