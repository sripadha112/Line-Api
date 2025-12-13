package com.app.auth.service;

import com.app.auth.dto.DoctorSlotDto;
import java.time.LocalDate;
import java.util.List;

public interface DoctorSlotService {
    
    /**
     * Generate slots for a doctor for a specific date
     */
    void generateSlotsForDoctorAndDate(Long doctorId, LocalDate date);
    
    /**
     * Get available slots for a doctor on a specific date
     */
    List<DoctorSlotDto> getAvailableSlots(Long doctorId, LocalDate date);
    
    /**
     * Get available slots for a doctor within date range (current date to next 2 days if no date provided)
     */
    List<DoctorSlotDto> getAvailableSlots(Long doctorId, LocalDate fromDate, LocalDate toDate);
    
    /**
     * Get available slots for a doctor at a specific workplace
     */
    List<DoctorSlotDto> getAvailableSlotsByWorkplace(Long doctorId, Long workplaceId, LocalDate date);
    
    /**
     * Mark a slot as unavailable when booked
     */
    void markSlotAsBooked(Long slotId);
    
    /**
     * Generate slots for next few days for all doctors (scheduled task)
     */
    void generateSlotsForAllDoctors(int daysAhead);
}
