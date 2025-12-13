package com.app.auth.service;

import com.app.auth.dto.AlertStatus;
import com.app.auth.dto.AppointmentAlertDto;

public interface MessageTemplateService {
    
    /**
     * Generate WhatsApp message based on appointment details and status
     * @param appointmentDetails appointment information
     * @param status alert status (BOOKED, RESCHEDULED, CANCELLED, COMPLETED)
     * @return formatted WhatsApp message
     */
    String generateWhatsAppMessage(AppointmentAlertDto appointmentDetails, AlertStatus status);
    
    /**
     * Generate SMS message based on appointment details and status
     * @param appointmentDetails appointment information
     * @param status alert status (BOOKED, RESCHEDULED, CANCELLED, COMPLETED)
     * @return formatted SMS message
     */
    String generateSMSMessage(AppointmentAlertDto appointmentDetails, AlertStatus status);
}