package com.app.auth.controller;

import com.app.auth.dto.CreatePrescriptionRequest;
import com.app.auth.dto.PrescriptionDto;
import com.app.auth.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    /**
     * Create a new prescription
     * POST /api/prescriptions
     */
    @PostMapping
    public ResponseEntity<PrescriptionDto> createPrescription(
            @Valid @RequestBody CreatePrescriptionRequest request) {
        PrescriptionDto prescription = prescriptionService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
    }

    /**
     * Update existing prescription
     * PUT /api/prescriptions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionDto> updatePrescription(
            @PathVariable Integer id,
            @Valid @RequestBody CreatePrescriptionRequest request) {
        PrescriptionDto prescription = prescriptionService.updatePrescription(id, request);
        return ResponseEntity.ok(prescription);
    }

    /**
     * Get prescription by ID
     * GET /api/prescriptions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionDto> getPrescriptionById(@PathVariable Integer id) {
        PrescriptionDto prescription = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(prescription);
    }

    /**
     * Get all prescriptions for a user
     * GET /api/prescriptions/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptionsByUserId(@PathVariable Integer userId) {
        List<PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByUserId(userId);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get all prescriptions by a doctor
     * GET /api/prescriptions/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptionsByDoctorId(@PathVariable Integer doctorId) {
        List<PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByDoctorId(doctorId);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Delete prescription
     * DELETE /api/prescriptions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrescription(@PathVariable Integer id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Generate PDF for prescription
     * GET /api/prescriptions/{id}/pdf
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePrescriptionPdf(@PathVariable Integer id) {
        byte[] pdfBytes = prescriptionService.generatePrescriptionPdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDispositionFormData("inline", "prescription_" + id + ".html");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
