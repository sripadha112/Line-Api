package com.app.auth.controller;

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
            @PathVariable("doctorId") Long doctorId,
            @Valid @RequestBody BlockSlotRequest request) {
        
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
            @PathVariable("doctorId") Long doctorId) {
        
        List<BlockedSlotDto> blockedSlots = blockedSlotService.getBlockedSlotsByDoctor(doctorId);
        return ResponseEntity.ok(blockedSlots);
    }

    /**
     * Get blocked slots for a specific date
     */
    @GetMapping("/{doctorId}/blocked-slots/date/{date}")
    public ResponseEntity<List<BlockedSlotDto>> getBlockedSlotsByDate(
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("date") LocalDate date) {
        
        List<BlockedSlotDto> blockedSlots = blockedSlotService.getBlockedSlotsByDoctorAndDate(doctorId, date);
        return ResponseEntity.ok(blockedSlots);
    }

    /**
     * Get blocked slots for a specific workplace and date range
     */
    @GetMapping("/{doctorId}/workplace/{workplaceId}/blocked-slots")
    public ResponseEntity<List<BlockedSlotDto>> getBlockedSlotsByWorkplace(
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("workplaceId") Long workplaceId,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate) {
        
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
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("workplaceId") Long workplaceId,
            @RequestParam("date") LocalDate date) {
        
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
            @PathVariable("blockedSlotId") Long blockedSlotId) {
        
        blockedSlotService.removeBlockedSlot(blockedSlotId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Blocked slot removed successfully"
        ));
    }
}
