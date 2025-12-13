package com.example.auth.service;

import com.example.auth.dto.AvailableSlotsResponseDto;
import com.example.auth.dto.BookAppointmentRequestDto;
import com.example.auth.dto.BulkAppointmentStatusUpdateDto;
import com.example.auth.dto.DoctorAppointmentViewDto;
import com.example.auth.dto.UserAppointmentDto;
import com.example.auth.dto.UserAppointmentsResponseDto;
import com.example.auth.dto.UserRescheduleRequestDto;

import java.util.List;

public interface EnhancedAppointmentService {
    
    UserAppointmentsResponseDto getUserAppointments(Long userId);
    
    AvailableSlotsResponseDto getAvailableSlots(Long doctorId, Long workplaceId, String date);
    
    UserAppointmentDto bookAppointment(BookAppointmentRequestDto request);
    
    String cancelAppointment(Long appointmentId);
    
    String rescheduleUserAppointment(UserRescheduleRequestDto request);
    
    void moveAppointmentsToCurrentDay();
    
    void movePastAppointments();
    
    // ==================== NEW DOCTOR MANAGEMENT APIS ====================
    
    /**
     * Get all appointments booked for a doctor, sorted by appointment time
     * Returns user details along with appointment information
     */
    List<DoctorAppointmentViewDto> getDoctorAppointmentsWithUserDetails(Long doctorId, String appointmentDate);
    
    /**
     * Bulk update appointment statuses for multiple users
     * Supports COMPLETED, RESCHEDULED, and CANCELLED statuses
     */
    String bulkUpdateAppointmentStatus(Long doctorId, String appointmentDate, BulkAppointmentStatusUpdateDto request);
    
    // ==================== FCM TOKEN MANAGEMENT ====================
    
    /**
     * Update FCM token for a user
     */
    boolean updateFcmToken(Long userId, String fcmToken, String deviceType);
    
    /**
     * Toggle notification settings for a user
     */
    boolean toggleNotifications(Long userId, Boolean enabled);
    
    /**
     * Send automatic push notification for appointment changes
     */
    void sendAppointmentNotification(Long userId, String title, String body, String notificationType);
}
