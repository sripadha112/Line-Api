package com.app.auth.controller;

import com.app.auth.dto.DailyAppointmentStatusDto;
import com.app.auth.service.DailyAppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments/daily")
public class DailyAppointmentController {

    private final DailyAppointmentService dailyAppointmentService;

    public DailyAppointmentController(DailyAppointmentService dailyAppointmentService) {
        this.dailyAppointmentService = dailyAppointmentService;
    }

    @GetMapping("/status")
    public ResponseEntity<DailyAppointmentStatusDto> getCurrentDateAppointmentStatus() {
        DailyAppointmentStatusDto status = dailyAppointmentService.getCurrentDateAppointmentStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/status/{date}")
    public ResponseEntity<DailyAppointmentStatusDto> getAppointmentStatusForDate(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        DailyAppointmentStatusDto status = dailyAppointmentService.getAppointmentStatusForDate(date);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/doctor/{doctorId}/status")
    public ResponseEntity<DailyAppointmentStatusDto> getDoctorCurrentDateAppointmentStatus(
            @PathVariable("doctorId") Long doctorId) {
        
        DailyAppointmentStatusDto status = dailyAppointmentService.getDoctorCurrentDateAppointmentStatus(doctorId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/doctor/{doctorId}/status/{date}")
    public ResponseEntity<DailyAppointmentStatusDto> getDoctorAppointmentStatusForDate(
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        DailyAppointmentStatusDto status = dailyAppointmentService.getDoctorAppointmentStatusForDate(doctorId, date);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/mark-completed")
    public ResponseEntity<Map<String, Object>> markPreviousDayAppointmentsAsCompleted() {
        int updatedCount = dailyAppointmentService.markPreviousDayAppointmentsAsCompleted();
        return ResponseEntity.ok(Map.of(
            "message", "Previous day appointments marked as completed",
            "updatedCount", updatedCount
        ));
    }
}
