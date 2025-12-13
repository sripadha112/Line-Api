package com.app.auth.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "past_appointments",
        indexes = {
                @Index(name = "idx_past_appointments_doctor_time", columnList = "doctor_id, appointment_time"),
                @Index(name = "idx_past_appointments_user", columnList = "user_id"),
                @Index(name = "idx_past_appointments_date", columnList = "appointment_date")
        })
public class PastAppointment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "workplace_id")
    private Long workplaceId;

    @Column(name = "workplace_name", length = 150)
    private String workplaceName;

    @Column(name = "workplace_type", length = 50)
    private String workplaceType;

    @Column(name = "workplace_address")
    private String workplaceAddress;

    @Column(name = "appointment_time", nullable = false)
    private OffsetDateTime appointmentTime;

    @Column(name = "appointment_date", nullable = false)
    private String appointmentDate; // YYYY-MM-DD format

    @Column(name = "slot", length = 50)
    private String slot; // e.g., "9:00AM - 9:15AM"

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 30;

    @Column(name = "queue_position", nullable = false)
    private Integer queuePosition = 1;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "COMPLETED"; // COMPLETED, CANCELLED

    @Column(name = "notes")
    private String notes;

    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    @Column(name = "doctor_specialization", length = 100)
    private String doctorSpecialization;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // Constructors
    public PastAppointment() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
