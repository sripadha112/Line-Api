package com.app.auth.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreatePrescriptionRequest {
    @NotNull
    private Integer userId;
    
    @NotNull
    private Integer doctorId;
    
    private Integer appointmentId;
    
    private String medicalNotes;
    
    private List<PrescriptionMedicineDto> medicines;

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public List<PrescriptionMedicineDto> getMedicines() { return medicines; }
    public void setMedicines(List<PrescriptionMedicineDto> medicines) { this.medicines = medicines; }
}
