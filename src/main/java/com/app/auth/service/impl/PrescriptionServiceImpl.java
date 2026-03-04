package com.app.auth.service.impl;

import com.app.auth.dto.CreatePrescriptionRequest;
import com.app.auth.dto.PrescriptionDto;
import com.app.auth.dto.PrescriptionMedicineDto;
import com.app.auth.entity.*;
import com.app.auth.repository.*;
import com.app.auth.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private DoctorDetailsRepository doctorDetailsRepository;

    @Override
    @Transactional
    public PrescriptionDto createPrescription(CreatePrescriptionRequest request) {
        // Create prescription
        Prescription prescription = new Prescription();
        prescription.setUserId(request.getUserId());
        prescription.setDoctorId(request.getDoctorId());
        prescription.setAppointmentId(request.getAppointmentId());
        prescription.setMedicalNotes(request.getMedicalNotes());
        prescription.setCreatedAt(OffsetDateTime.now());
        prescription.setUpdatedAt(OffsetDateTime.now());

        // Add medicines to JSONB
        if (request.getMedicines() != null && !request.getMedicines().isEmpty()) {
            List<Prescription.MedicineItem> medicineItems = new ArrayList<>();
            for (PrescriptionMedicineDto medicineDto : request.getMedicines()) {
                Prescription.MedicineItem item = new Prescription.MedicineItem();
                item.setMedicineId(medicineDto.getMedicineId());
                
                // Fetch medicine details
                Medicine medicine = medicineRepository.findById(medicineDto.getMedicineId()).orElse(null);
                if (medicine != null) {
                    item.setMedicineName(medicine.getMedicineName());
                    String composition = "";
                    if (medicine.getComposition1() != null) composition += medicine.getComposition1();
                    if (medicine.getComposition2() != null) composition += ", " + medicine.getComposition2();
                    item.setComposition(composition);
                    item.setManufacturer(medicine.getManufacturer());
                }
                
                item.setDosage(medicineDto.getDosage());
                item.setFrequency(medicineDto.getFrequency());
                item.setDuration(medicineDto.getDuration());
                item.setInstructions(medicineDto.getInstructions());
                
                medicineItems.add(item);
            }
            prescription.setMedicines(medicineItems);
        }

        prescription = prescriptionRepository.save(prescription);
        return convertToDto(prescription);
    }

    @Override
    @Transactional
    public PrescriptionDto updatePrescription(Integer prescriptionId, CreatePrescriptionRequest request) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));

        prescription.setMedicalNotes(request.getMedicalNotes());
        prescription.setUpdatedAt(OffsetDateTime.now());

        // Update medicines in JSONB
        if (request.getMedicines() != null) {
            List<Prescription.MedicineItem> medicineItems = new ArrayList<>();
            for (PrescriptionMedicineDto medicineDto : request.getMedicines()) {
                Prescription.MedicineItem item = new Prescription.MedicineItem();
                item.setMedicineId(medicineDto.getMedicineId());
                
                // Fetch medicine details
                Medicine medicine = medicineRepository.findById(medicineDto.getMedicineId()).orElse(null);
                if (medicine != null) {
                    item.setMedicineName(medicine.getMedicineName());
                    String composition = "";
                    if (medicine.getComposition1() != null) composition += medicine.getComposition1();
                    if (medicine.getComposition2() != null) composition += ", " + medicine.getComposition2();
                    item.setComposition(composition);
                    item.setManufacturer(medicine.getManufacturer());
                }
                
                item.setDosage(medicineDto.getDosage());
                item.setFrequency(medicineDto.getFrequency());
                item.setDuration(medicineDto.getDuration());
                item.setInstructions(medicineDto.getInstructions());
                
                medicineItems.add(item);
            }
            prescription.setMedicines(medicineItems);
        }

        prescription = prescriptionRepository.save(prescription);
        return convertToDto(prescription);
    }

    @Override
    public PrescriptionDto getPrescriptionById(Integer id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));
        return convertToDto(prescription);
    }

    @Override
    public List<PrescriptionDto> getPrescriptionsByUserId(Integer userId) {
        List<Prescription> prescriptions = prescriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return prescriptions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PrescriptionDto> getPrescriptionsByDoctorId(Integer doctorId) {
        List<Prescription> prescriptions = prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
        return prescriptions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePrescription(Integer id) {
        prescriptionRepository.deleteById(id);
    }

    @Override
    public byte[] generatePrescriptionPdf(Integer prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));

        // Get user and doctor details
        UserDetails user = userDetailsRepository.findById(prescription.getUserId().longValue()).orElse(null);
        DoctorDetails doctor = doctorDetailsRepository.findById(prescription.getDoctorId().longValue()).orElse(null);

        // Generate HTML for PDF with UTF-8 encoding
        String html = generatePrescriptionHtml(prescription, user, doctor);
        
        return html.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String generatePrescriptionHtml(Prescription prescription, UserDetails user, DoctorDetails doctor) {
        StringBuilder html = new StringBuilder();
        
        // Get primary workplace for clinic/hospital details
        DoctorWorkplace primaryWorkplace = null;
        if (doctor != null && doctor.getWorkplaces() != null && !doctor.getWorkplaces().isEmpty()) {
            primaryWorkplace = doctor.getWorkplaces().stream()
                .filter(DoctorWorkplace::getIsPrimary)
                .findFirst()
                .orElse(doctor.getWorkplaces().get(0));
        }
        
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("* { margin: 0; padding: 0; box-sizing: border-box; }");
        html.append("@page { size: A4; margin: 15mm; }");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 12px; line-height: 1.4; color: #222; }");
        html.append(".page { max-width: 210mm; margin: 0 auto; padding: 15px; border: 2px solid #1565C0; position: relative; min-height: 267mm; }");
        
        // Header styles - compact
        html.append(".header { margin-bottom: 12px; }");
        html.append(".doctor-name { font-size: 22px; font-weight: bold; color: #1565C0; margin-bottom: 3px; line-height: 1.2; }");
        html.append(".doctor-designation { font-size: 13px; color: #555; margin-bottom: 5px; }");
        html.append(".hospital-name { font-size: 15px; font-weight: bold; color: #333; margin-bottom: 3px; }");
        html.append(".hospital-address { font-size: 11px; color: #666; line-height: 1.3; }");
        
        // Separator - thinner
        html.append(".separator { border-top: 1.5px solid #333; margin: 10px 0; }");
        
        // Patient details - compact two-column
        html.append(".patient-details { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 12px; }");
        html.append(".patient-left { flex: 1; }");
        html.append(".patient-right { flex: 1; text-align: right; }");
        html.append(".detail-row { margin-bottom: 4px; }");
        html.append(".detail-label { font-weight: 600; color: #333; }");
        html.append(".detail-value { color: #555; }");
        
        // Medicines section - very compact
        html.append(".medicines-section { margin-top: 12px; margin-bottom: 60px; }");
        html.append(".medicine-item { margin-bottom: 12px; padding: 8px 10px; background: #f9f9f9; border-left: 3px solid #1565C0; page-break-inside: avoid; }");
        html.append(".medicine-name { font-size: 14px; font-weight: bold; color: #1565C0; margin-bottom: 5px; line-height: 1.3; }");
        html.append(".medicine-composition { font-size: 11px; font-weight: normal; color: #666; }");
        
        // Frequency and duration on same line
        html.append(".frequency-duration-row { display: flex; justify-content: space-between; align-items: center; margin: 5px 0; }");
        html.append(".frequency-section { flex: 1; }");
        html.append(".duration-section { text-align: right; font-size: 12px; }");
        html.append(".frequency-label { font-weight: 600; color: #333; margin-right: 8px; display: inline-block; font-size: 11px; }");
        html.append(".frequency-options { display: inline-flex; gap: 12px; flex-wrap: wrap; }");
        html.append(".frequency-option { font-size: 11px; color: #555; white-space: nowrap; }");
        html.append(".checkbox { margin-right: 3px; font-size: 14px; }");
        html.append(".checkbox-checked { margin-right: 3px; font-size: 14px; font-weight: bold; }");
        html.append(".duration-label { font-weight: 600; color: #333; margin-right: 5px; }");
        html.append(".duration-value { color: #555; }");
        
        html.append(".medicine-notes { margin-top: 5px; font-size: 11px; }");
        html.append(".notes-label { font-weight: 600; color: #333; margin-right: 5px; }");
        html.append(".notes-value { color: #555; }");
        
        // Footer styles - positioned at bottom right
        html.append(".footer { position: absolute; bottom: 30px; right: 30px; text-align: right; }");
        html.append(".signature-line { border-top: 2px solid #333; width: 200px; margin: 0 0 8px auto; }");
        html.append(".doctor-signature { font-weight: 600; color: #333; margin-bottom: 3px; font-size: 13px; }");
        html.append(".registration-number { font-size: 11px; color: #666; }");
        html.append(".digital-note { position: absolute; bottom: 8px; left: 20px; right: 20px; text-align: center; font-size: 9px; color: #999; font-style: italic; }");
        
        html.append("</style></head><body>");
        html.append("<div class='page'>");
        
        // Header Section
        html.append("<div class='header'>");
        if (doctor != null) {
            html.append("<div class='doctor-name'>Dr. ").append(escapeHtml(doctor.getFullName())).append("</div>");
            html.append("<div class='doctor-designation'>")
                .append(escapeHtml(doctor.getDesignation() != null ? doctor.getDesignation() : doctor.getSpecialization() != null ? doctor.getSpecialization() : "Medical Practitioner"))
                .append("</div>");
        }
        if (primaryWorkplace != null) {
            html.append("<div class='hospital-name'>").append(escapeHtml(primaryWorkplace.getWorkplaceName())).append("</div>");
            StringBuilder address = new StringBuilder();
            if (primaryWorkplace.getAddress() != null) address.append(primaryWorkplace.getAddress());
            if (primaryWorkplace.getCity() != null) {
                if (address.length() > 0) address.append(", ");
                address.append(primaryWorkplace.getCity());
            }
            if (primaryWorkplace.getState() != null) {
                if (address.length() > 0) address.append(", ");
                address.append(primaryWorkplace.getState());
            }
            if (primaryWorkplace.getPincode() != null) {
                if (address.length() > 0) address.append(" - ");
                address.append(primaryWorkplace.getPincode());
            }
            if (address.length() > 0) {
                html.append("<div class='hospital-address'>").append(escapeHtml(address.toString())).append("</div>");
            }
            if (primaryWorkplace.getContactNumber() != null) {
                html.append("<div class='hospital-address'>Contact: ").append(escapeHtml(primaryWorkplace.getContactNumber())).append("</div>");
            }
        }
        html.append("</div>");
        
        html.append("<div class='separator'></div>");
        
        // Patient Details Section
        html.append("<div class='patient-details'>");
        html.append("<div class='patient-left'>");
        if (user != null) {
            html.append("<div class='detail-row'><span class='detail-label'>Patient Name:</span> <span class='detail-value'>")
                .append(escapeHtml(user.getFullName())).append("</span></div>");
            
            if (user.getAge() != null) {
                html.append("<div class='detail-row'><span class='detail-label'>Age:</span> <span class='detail-value'>")
                    .append(user.getAge()).append(" years</span></div>");
            }
            
            if (user.getGender() != null && !user.getGender().isEmpty()) {
                html.append("<div class='detail-row'><span class='detail-label'>Gender:</span> <span class='detail-value'>")
                    .append(escapeHtml(user.getGender())).append("</span></div>");
            }
        }
        html.append("</div>");
        html.append("<div class='patient-right'>");
        html.append("<div class='detail-row'><span class='detail-label'>Date:</span> <span class='detail-value'>")
            .append(prescription.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))).append("</span></div>");
        if (user != null && user.getMobileNumber() != null) {
            html.append("<div class='detail-row'><span class='detail-label'>Phone:</span> <span class='detail-value'>")
                .append(escapeHtml(user.getMobileNumber())).append("</span></div>");
        }
        html.append("</div>");
        html.append("</div>");
        
        html.append("<div class='separator'></div>");
        
        // Medicines Section
        if (prescription.getMedicines() != null && !prescription.getMedicines().isEmpty()) {
            html.append("<div class='medicines-section'>");
            
            // Fetch all medicine details in one query for better performance
            java.util.Map<Integer, Medicine> medicineMap = new java.util.HashMap<>();
            java.util.List<Integer> medicineIds = prescription.getMedicines().stream()
                .map(Prescription.MedicineItem::getMedicineId)
                .filter(id -> id != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            
            if (!medicineIds.isEmpty()) {
                medicineRepository.findAllById(medicineIds).forEach(med -> medicineMap.put(med.getId(), med));
            }
            
            int sno = 1;
            for (Prescription.MedicineItem medicine : prescription.getMedicines()) {
                // Get medicine details from pre-fetched map
                Medicine medicineDetails = medicine.getMedicineId() != null ? medicineMap.get(medicine.getMedicineId()) : null;
                
                html.append("<div class='medicine-item'>");
                
                // Medicine name with composition in brackets
                html.append("<div class='medicine-name'>")
                    .append(sno++).append(". ")
                    .append(escapeHtml(medicine.getMedicineName() != null ? medicine.getMedicineName() : "N/A"));
                
                // Add composition if available
                if (medicineDetails != null && medicineDetails.getComposition1() != null && !medicineDetails.getComposition1().isEmpty()) {
                    html.append(" <span class='medicine-composition'>(")
                        .append(escapeHtml(medicineDetails.getComposition1()))
                        .append(")</span>");
                }
                
                // Add dosage/strength if available
                if (medicine.getDosage() != null && !medicine.getDosage().isEmpty()) {
                    html.append(" <span class='medicine-composition'>- ")
                        .append(escapeHtml(medicine.getDosage()))
                        .append("</span>");
                }
                html.append("</div>");
                
                // Frequency and Duration on same line
                html.append("<div class='frequency-duration-row'>");
                
                // Left side - Frequency with checkboxes
                html.append("<div class='frequency-section'>");
                html.append("<span class='frequency-label'>Frequency:</span>");
                html.append("<span class='frequency-options'>");
                
                String frequency = medicine.getFrequency() != null ? medicine.getFrequency().toLowerCase() : "";
                boolean morning = frequency.contains("morning");
                boolean afternoon = frequency.contains("afternoon");
                boolean evening = frequency.contains("evening");
                boolean fasting = frequency.contains("fasting");
                
                html.append("<span class='frequency-option'><span class='").append(morning ? "checkbox-checked" : "checkbox").append("'>").append(morning ? "☑" : "☐").append("</span>Morning</span>");
                html.append("<span class='frequency-option'><span class='").append(afternoon ? "checkbox-checked" : "checkbox").append("'>").append(afternoon ? "☑" : "☐").append("</span>Afternoon</span>");
                html.append("<span class='frequency-option'><span class='").append(evening ? "checkbox-checked" : "checkbox").append("'>").append(evening ? "☑" : "☐").append("</span>Evening</span>");
                html.append("<span class='frequency-option'><span class='").append(fasting ? "checkbox-checked" : "checkbox").append("'>").append(fasting ? "☑" : "☐").append("</span>Fasting</span>");
                
                html.append("</span>");
                html.append("</div>");
                
                // Right side - Duration
                if (medicine.getDuration() != null && !medicine.getDuration().isEmpty()) {
                    html.append("<div class='duration-section'><span class='duration-label'>Duration:</span><span class='duration-value'>")
                        .append(escapeHtml(medicine.getDuration())).append("</span></div>");
                }
                
                html.append("</div>");
                
                // Instructions/Notes on separate line if present
                if (medicine.getInstructions() != null && !medicine.getInstructions().isEmpty()) {
                    html.append("<div class='medicine-notes'><span class='notes-label'>Notes:</span><span class='notes-value'>")
                        .append(escapeHtml(medicine.getInstructions())).append("</span></div>");
                }
                
                html.append("</div>");
            }
            
            html.append("</div>");
        }
        
        // Footer with signature
        html.append("<div class='footer'>");
        html.append("<div class='signature-line'></div>");
        if (doctor != null) {
            html.append("<div class='doctor-signature'>Dr. ").append(escapeHtml(doctor.getFullName())).append("</div>");
            if (doctor.getEmail() != null) {
                html.append("<div class='registration-number'>Registration: ").append(escapeHtml(doctor.getEmail())).append("</div>");
            }
        }
        html.append("</div>");
        
        // Digital note at bottom
        html.append("<div class='digital-note'>This is a digitally generated prescription</div>");
        
        html.append("</div>");
        html.append("</body></html>");
        return html.toString();
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private PrescriptionDto convertToDto(Prescription prescription) {
        PrescriptionDto dto = new PrescriptionDto();
        dto.setId(prescription.getId());
        dto.setUserId(prescription.getUserId());
        dto.setDoctorId(prescription.getDoctorId());
        dto.setAppointmentId(prescription.getAppointmentId());
        dto.setMedicalNotes(prescription.getMedicalNotes());
        dto.setCreatedAt(prescription.getCreatedAt());
        dto.setUpdatedAt(prescription.getUpdatedAt());

        // Get patient name
        UserDetails user = userDetailsRepository.findById(prescription.getUserId().longValue()).orElse(null);
        if (user != null) {
            dto.setPatientName(user.getFullName());
        }

        // Get doctor details
        DoctorDetails doctor = doctorDetailsRepository.findById(prescription.getDoctorId().longValue()).orElse(null);
        if (doctor != null) {
            dto.setDoctorName(doctor.getFullName());
            dto.setDoctorSpecialization(doctor.getSpecialization());
        }

        // Convert medicines from JSONB
        List<PrescriptionMedicineDto> medicineDtos = new ArrayList<>();
        if (prescription.getMedicines() != null) {
            for (Prescription.MedicineItem item : prescription.getMedicines()) {
                PrescriptionMedicineDto medicineDto = new PrescriptionMedicineDto();
                medicineDto.setMedicineId(item.getMedicineId());
                medicineDto.setMedicineName(item.getMedicineName());
                medicineDto.setComposition(item.getComposition());
                medicineDto.setManufacturer(item.getManufacturer());
                medicineDto.setDosage(item.getDosage());
                medicineDto.setFrequency(item.getFrequency());
                medicineDto.setDuration(item.getDuration());
                medicineDto.setInstructions(item.getInstructions());
                medicineDtos.add(medicineDto);
            }
        }
        dto.setMedicines(medicineDtos);

        return dto;
    }
}
