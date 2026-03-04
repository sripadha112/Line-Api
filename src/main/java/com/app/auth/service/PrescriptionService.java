package com.app.auth.service;

import com.app.auth.dto.CreatePrescriptionRequest;
import com.app.auth.dto.PrescriptionDto;

import java.util.List;

public interface PrescriptionService {
    PrescriptionDto createPrescription(CreatePrescriptionRequest request);
    PrescriptionDto updatePrescription(Integer prescriptionId, CreatePrescriptionRequest request);
    PrescriptionDto getPrescriptionById(Integer id);
    List<PrescriptionDto> getPrescriptionsByUserId(Integer userId);
    List<PrescriptionDto> getPrescriptionsByDoctorId(Integer doctorId);
    void deletePrescription(Integer id);
    byte[] generatePrescriptionPdf(Integer prescriptionId);
}
