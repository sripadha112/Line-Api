package com.app.auth.controller;

import com.app.auth.config.QueryParamIdCrypto;
import com.app.auth.dto.DoctorSlotDto;
import com.app.auth.service.DoctorSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class DoctorSlotController {

    private final DoctorSlotService slotService;

    public DoctorSlotController(DoctorSlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping("/doctor/{doctorId}/available")
    public ResponseEntity<List<DoctorSlotDto>> getAvailableSlots(
            @PathVariable("doctorId") String encodedDoctorId,
            @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate) {
        Long doctorId = QueryParamIdCrypto.decodeLong(encodedDoctorId);
        
        List<DoctorSlotDto> slots;
        
        if (date != null) {
            // Get slots for specific date
            slots = slotService.getAvailableSlots(doctorId, date);
        } else {
            // Get slots for date range (default: current date + 2 days)
            if (fromDate == null) {
                fromDate = LocalDate.now();
            }
            if (toDate == null) {
                toDate = fromDate.plusDays(2);
            }
            slots = slotService.getAvailableSlots(doctorId, fromDate, toDate);
        }
        
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/doctor/{doctorId}/workplace/{workplaceId}/available")
    public ResponseEntity<List<DoctorSlotDto>> getAvailableSlotsByWorkplace(
            @PathVariable("doctorId") String encodedDoctorId,
            @PathVariable("workplaceId") String encodedWorkplaceId,
            @RequestParam("date") LocalDate date) {
        Long doctorId = QueryParamIdCrypto.decodeLong(encodedDoctorId);
        Long workplaceId = QueryParamIdCrypto.decodeLong(encodedWorkplaceId);
        
        List<DoctorSlotDto> slots = slotService.getAvailableSlotsByWorkplace(doctorId, workplaceId, date);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/doctor/{doctorId}/generate")
    public ResponseEntity<String> generateSlotsForDoctor(
            @PathVariable("doctorId") String encodedDoctorId,
            @RequestParam("date") LocalDate date) {
        Long doctorId = QueryParamIdCrypto.decodeLong(encodedDoctorId);
        
        slotService.generateSlotsForDoctorAndDate(doctorId, date);
        return ResponseEntity.ok("Slots generated successfully for doctor " + doctorId + " on " + date);
    }

    @PostMapping("/generate-all")
    public ResponseEntity<String> generateSlotsForAllDoctors(
            @RequestParam(value = "daysAhead", defaultValue = "7") int daysAhead) {
        
        slotService.generateSlotsForAllDoctors(daysAhead);
        return ResponseEntity.ok("Slots generated for all doctors for next " + (daysAhead + 1) + " days");
    }
}
