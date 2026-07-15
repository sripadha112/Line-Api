package com.app.auth.controller;

import com.app.auth.config.QueryParamIdCrypto;
import com.app.auth.dto.BlockedSlotDto;
import com.app.auth.dto.BlockSlotRequest;
import com.app.auth.service.BlockedSlotService;
import com.app.auth.service.impl.BlockedSlotServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class BlockedSlotController {

    private final BlockedSlotServiceImpl blockedSlotService;

    public BlockedSlotController(BlockedSlotServiceImpl blockedSlotService) {
        this.blockedSlotService = blockedSlotService;
    }

    /**
     * Create a new blocked slot for a doctor
     * This can block specific hours or entire days for one or all workplaces
     * If cancelExistingAppointments is true, existing appointments in the blocked time will be cancelled
     */
    @PostMapping("/{doctorId}/block-slots")
    public ResponseEntity<Map<String, Object>> createBlockedSlot(
            @PathVariable("doctorId") String encodedDoctorId,
            @Valid @RequestBody BlockSlotRequest request) {
        Long doctorId = QueryParamIdCrypto.decodeLong(encodedDoctorId);
        
        System.out.println("[BlockedSlotController] Creating blocked slot for doctor " + doctorId);
        System.out.println("[BlockedSlotController] Request: date=" + request.getDate() + 
                          ", workplaceId=" + request.getWorkplaceId() +
                          ", isFullDay=" + request.getIsFullDay() +
                          ", startTime=" + request.getStartTime() + 
                          ", endTime=" + request.getEndTime() +
                          ", cancelExisting=" + request.getCancelExistingAppointments());
        
        BlockedSlotDto blockedSlot = blockedSlotService.createBlockedSlot(doctorId, request);
        int cancelledCount = blockedSlotService.getLastCancelledCount();
        
        String message;
        if (blockedSlot.getIsFullDay()) {
            message = "Entire day blocked successfully for " + request.getDate();
        } else {
            message = "Time slot blocked successfully from " + request.getStartTime() + 
                     " to " + request.getEndTime() + " on " + request.getDate();
        }
        
        if (cancelledCount > 0) {
            message += ". " + cancelledCount + " existing appointment(s) were cancelled.";
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("blockedSlot", blockedSlot);
        response.put("cancelledAppointments", cancelledCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all blocked slots for a doctor
     */
    @GetMapping("/{doctorId}/blocked-slots")
    public ResponseEntity<List<BlockedSlotDto>> getBlockedSlotsByDoctor(
            @PathVariable("doctorId") String encodedDoctorId) {
        Long doctorId = QueryParamIdCrypto.decodeLong(encodedDoctorId);
        
        List<BlockedSlotDto> blockedSlots = blockedSlotService.getBlockedSlotsByDoctor(doctorId);
        return ResponseEntity.ok(blockedSlots);
    }

    /**
     * Get blocked slots for a specific date
     */
    @GetMapping("/{doctorId}/blocked-slots/date/{date}")
    public ResponseEntity<List<BlockedSlotDto>> getBlockedSlotsByDate(
            @PathVariable("doctorId") String encodedDoctorId,
            @PathVariable("date") LocalDate date) {
        Long doctorId = QueryParamIdCrypto.decodeLong(encodedDoctorId);
        
        List<BlockedSlotDto> blockedSlots = blockedSlotService.getBlockedSlotsByDoctorAndDate(doctorId, date);
        return ResponseEntity.ok(blockedSlots);
    }

    /**
     * Get blocked slots for a specific workplace and date range
     */
    @GetMapping("/{doctorId}/workplace/{workplaceId}/blocked-slots")
    public ResponseEntity<List<BlockedSlotDto>> getBlockedSlotsByWorkplace(
            @PathVariable("doctorId") String encodedDoctorId,
            @PathVariable("workplaceId") String encodedWorkplaceId,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate) {
        Long doctorId = QueryParamIdCrypto.decodeLong(encodedDoctorId);
        Long workplaceId = QueryParamIdCrypto.decodeLong(encodedWorkplaceId);
        
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }
        if (toDate == null) {
            toDate = fromDate.plusDays(30);
        }
        
        List<BlockedSlotDto> blockedSlots = blockedSlotService.getBlockedSlotsForDateRange(
            doctorId, workplaceId, fromDate, toDate);
        return ResponseEntity.ok(blockedSlots);
    }

    /**
     * Check if a full day is blocked
     */
    @GetMapping("/{doctorId}/workplace/{workplaceId}/is-day-blocked")
    public ResponseEntity<Map<String, Object>> isDayBlocked(
            @PathVariable("doctorId") String encodedDoctorId,
            @PathVariable("workplaceId") String encodedWorkplaceId,
            @RequestParam("date") LocalDate date) {
        Long doctorId = QueryParamIdCrypto.decodeLong(encodedDoctorId);
        Long workplaceId = QueryParamIdCrypto.decodeLong(encodedWorkplaceId);
        
        BlockedSlotDto fullDayBlock = blockedSlotService.getFullDayBlock(doctorId, workplaceId, date);
        
        if (fullDayBlock != null) {
            return ResponseEntity.ok(Map.of(
                "isBlocked", true,
                "reason", fullDayBlock.getReason() != null ? fullDayBlock.getReason() : "Doctor unavailable",
                "blockedSlot", fullDayBlock
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "isBlocked", false
        ));
    }

    /**
     * Remove (deactivate) a blocked slot
     */
    @DeleteMapping("/blocked-slots/{blockedSlotId}")
    public ResponseEntity<Map<String, Object>> removeBlockedSlot(
            @PathVariable("blockedSlotId") String encodedBlockedSlotId) {
        Long blockedSlotId = QueryParamIdCrypto.decodeLong(encodedBlockedSlotId);
        
        blockedSlotService.removeBlockedSlot(blockedSlotId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Blocked slot removed successfully"
        ));
    }
}
