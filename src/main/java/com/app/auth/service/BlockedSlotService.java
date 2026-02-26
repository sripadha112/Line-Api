package com.app.auth.service;

import com.app.auth.dto.BlockedSlotDto;
import com.app.auth.dto.BlockSlotRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BlockedSlotService {
    
    /**
     * Create a new blocked slot for a doctor
     */
    BlockedSlotDto createBlockedSlot(Long doctorId, BlockSlotRequest request);
    
    /**
     * Get all active blocked slots for a doctor
     */
    List<BlockedSlotDto> getBlockedSlotsByDoctor(Long doctorId);
    
    /**
     * Get all active blocked slots for a workplace
     */
    List<BlockedSlotDto> getBlockedSlotsByWorkplace(Long workplaceId);
    
    /**
     * Get blocked slots for a doctor on a specific date
     */
    List<BlockedSlotDto> getBlockedSlotsByDoctorAndDate(Long doctorId, LocalDate date);
    
    /**
     * Get blocked slots for a workplace on a specific date
     */
    List<BlockedSlotDto> getBlockedSlotsByWorkplaceAndDate(Long doctorId, Long workplaceId, LocalDate date);
    
    /**
     * Check if a specific time slot is blocked
     */
    boolean isTimeBlocked(Long doctorId, Long workplaceId, LocalDate date, LocalTime time);
    
    /**
     * Check if entire day is blocked for a workplace
     */
    BlockedSlotDto getFullDayBlock(Long doctorId, Long workplaceId, LocalDate date);
    
    /**
     * Remove a blocked slot (deactivate)
     */
    void removeBlockedSlot(Long blockedSlotId);
    
    /**
     * Get blocked slots for a date range
     */
    List<BlockedSlotDto> getBlockedSlotsForDateRange(Long doctorId, Long workplaceId, LocalDate fromDate, LocalDate toDate);
}
