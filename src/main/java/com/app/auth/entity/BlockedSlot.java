package com.app.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * Entity to store blocked time slots by doctors.
 * Doctors can block specific time ranges or entire days for one or all workplaces.
 */
@Entity
@Table(name = "blocked_slots",
        indexes = {
                @Index(name = "idx_blocked_doctor_date", columnList = "doctor_id, block_date"),
                @Index(name = "idx_blocked_workplace_date", columnList = "workplace_id, block_date"),
                @Index(name = "idx_blocked_doctor_workplace_date", columnList = "doctor_id, workplace_id, block_date")
        })
public class BlockedSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "workplace_id")
    private Long workplaceId; // null means all workplaces

    @Column(name = "block_date", nullable = false)
    private LocalDate blockDate;

    @Column(name = "start_time")
    private LocalTime startTime; // null means entire day blocked

    @Column(name = "end_time")
    private LocalTime endTime; // null means entire day blocked

    @Column(name = "is_full_day", nullable = false)
    private Boolean isFullDay = false;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Constructors
    public BlockedSlot() {
    }

    public BlockedSlot(Long doctorId, Long workplaceId, LocalDate blockDate, 
                       LocalTime startTime, LocalTime endTime, Boolean isFullDay, String reason) {
        this.doctorId = doctorId;
        this.workplaceId = workplaceId;
        this.blockDate = blockDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isFullDay = isFullDay;
        this.reason = reason;
        this.createdAt = OffsetDateTime.now();
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getWorkplaceId() { return workplaceId; }
    public void setWorkplaceId(Long workplaceId) { this.workplaceId = workplaceId; }

    public LocalDate getBlockDate() { return blockDate; }
    public void setBlockDate(LocalDate blockDate) { this.blockDate = blockDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Boolean getIsFullDay() { return isFullDay; }
    public void setIsFullDay(Boolean isFullDay) { this.isFullDay = isFullDay; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
