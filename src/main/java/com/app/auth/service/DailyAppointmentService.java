package com.app.auth.service;

import com.app.auth.dto.DailyAppointmentStatusDto;
import java.time.LocalDate;
import java.util.List;

public interface DailyAppointmentService {
    
    /**
     * Get appointment status for current date
     */
    DailyAppointmentStatusDto getCurrentDateAppointmentStatus();
    
    /**
     * Get appointment status for a specific date
     */
    DailyAppointmentStatusDto getAppointmentStatusForDate(LocalDate date);
    
    /**
     * Mark all previous day's pending appointments as completed
     * This should run automatically when date changes
     */
    int markPreviousDayAppointmentsAsCompleted();
    
    /**
     * Get appointment status for a doctor on current date
     */
    DailyAppointmentStatusDto getDoctorCurrentDateAppointmentStatus(Long doctorId);
    
    /**
     * Get appointment status for a doctor on specific date
     */
    DailyAppointmentStatusDto getDoctorAppointmentStatusForDate(Long doctorId, LocalDate date);
}
