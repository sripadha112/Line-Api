package com.app.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public class BookAppointmentRequest {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Workplace ID is required")
    private Long workplaceId;

    @NotNull(message = "Requested time is required")
    private OffsetDateTime requestedTime;

    @NotBlank(message = "Slot is required")
    private String slot; // e.g., "02:30PM - 02:50PM"

    private String notes;
    private Long familyMemberId;

    // Getters and Setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getWorkplaceId() { return workplaceId; }
    public void setWorkplaceId(Long workplaceId) { this.workplaceId = workplaceId; }

    public OffsetDateTime getRequestedTime() { return requestedTime; }
    public void setRequestedTime(OffsetDateTime requestedTime) { this.requestedTime = requestedTime; }

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getFamilyMemberId() { return familyMemberId; }
    public void setFamilyMemberId(Long familyMemberId) { this.familyMemberId = familyMemberId; }
}
