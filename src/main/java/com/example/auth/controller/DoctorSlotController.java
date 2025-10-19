package com.example.auth.controller;

import com.example.auth.dto.DoctorSlotDto;
import com.example.auth.service.DoctorSlotService;
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
            @PathVariable("doctorId") Long doctorId,
            @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate) {
        
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
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("workplaceId") Long workplaceId,
            @RequestParam("date") LocalDate date) {
        
        List<DoctorSlotDto> slots = slotService.getAvailableSlotsByWorkplace(doctorId, workplaceId, date);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/doctor/{doctorId}/generate")
    public ResponseEntity<String> generateSlotsForDoctor(
            @PathVariable("doctorId") Long doctorId,
            @RequestParam("date") LocalDate date) {
        
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
