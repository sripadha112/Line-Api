package com.example.auth.service.impl;

import com.example.auth.dto.AlertStatus;
import com.example.auth.dto.AppointmentAlertDto;
import com.example.auth.service.MessageTemplateService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class MessageTemplateServiceImpl implements MessageTemplateService {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    
    @Override
    public String generateWhatsAppMessage(AppointmentAlertDto appointmentDetails, AlertStatus status) {
        StringBuilder message = new StringBuilder();
        
        // Header with emoji based on status
        String emoji = getEmojiForStatus(status);
        message.append(emoji).append(" *").append(status.getDisplayName()).append("*\n\n");
        
        // Patient greeting
        message.append("Dear ").append(appointmentDetails.getPatientName()).append(",\n\n");
        
        // Status-specific message
        switch (status) {
            case BOOKED:
                message.append("✅ Your appointment has been successfully booked!\n\n");
                break;
            case RESCHEDULED:
                message.append("🔄 Your appointment has been rescheduled.\n\n");
                break;
            case CANCELLED:
                message.append("❌ Your appointment has been cancelled.\n\n");
                break;
            case COMPLETED:
                message.append("✅ Your appointment has been completed. Thank you for visiting!\n\n");
                break;
        }
        
        // Appointment details (only for non-cancelled appointments)
        if (status != AlertStatus.CANCELLED) {
            message.append("📋 *Appointment Details:*\n");
            message.append("🆔 ID: #").append(appointmentDetails.getAppointmentId()).append("\n");
            message.append("📅 Date: ").append(appointmentDetails.getAppointmentTime().format(DATE_FORMATTER)).append("\n");
            message.append("⏰ Time: ").append(appointmentDetails.getAppointmentTime().format(TIME_FORMATTER)).append("\n");
            message.append("⏱️ Duration: ").append(appointmentDetails.getDurationMinutes()).append(" minutes\n");
            
            if (appointmentDetails.getQueuePosition() != null && appointmentDetails.getQueuePosition() > 0) {
                message.append("📍 Queue Position: #").append(appointmentDetails.getQueuePosition()).append("\n");
            }
            message.append("\n");
        }
        
        // Doctor details
        if (appointmentDetails.getDoctor() != null) {
            message.append("👨‍⚕️ *Doctor Information:*\n");
            message.append("Name: Dr. ").append(appointmentDetails.getDoctor().getFullName()).append("\n");
            message.append("Specialization: ").append(appointmentDetails.getDoctor().getSpecialization()).append("\n");
            if (appointmentDetails.getDoctor().getDesignation() != null) {
                message.append("Designation: ").append(appointmentDetails.getDoctor().getDesignation()).append("\n");
            }
            message.append("\n");
        }
        
        // Workplace details (only for non-cancelled appointments)
        if (status != AlertStatus.CANCELLED && appointmentDetails.getWorkplace() != null) {
            message.append("🏥 *Location:*\n");
            message.append("Hospital: ").append(appointmentDetails.getWorkplace().getWorkplaceName()).append("\n");
            message.append("Address: ").append(appointmentDetails.getWorkplace().getAddress()).append("\n");
            if (appointmentDetails.getWorkplace().getContactNumber() != null) {
                message.append("Contact: ").append(appointmentDetails.getWorkplace().getContactNumber()).append("\n");
            }
            message.append("\n");
        }
        
        // Notes if present
        if (appointmentDetails.getNotes() != null && !appointmentDetails.getNotes().trim().isEmpty()) {
            message.append("📝 *Notes:* ").append(appointmentDetails.getNotes()).append("\n\n");
        }
        
        // Footer message based on status
        switch (status) {
            case BOOKED:
                message.append("Please arrive 15 minutes early. Bring your ID and insurance card.\n\n");
                message.append("Need to reschedule? Reply to this message or call us.\n\n");
                break;
            case RESCHEDULED:
                message.append("Please update your calendar with the new time.\n\n");
                break;
            case CANCELLED:
                message.append("If you need to book a new appointment, please contact us.\n\n");
                break;
            case COMPLETED:
                message.append("We hope you had a great experience. Take care!\n\n");
                break;
        }
        
        message.append("📱 *Healthcare App*\n");
        message.append("_Powered by Line Healthcare Solutions_");
        
        return message.toString();
    }
    
    @Override
    public String generateSMSMessage(AppointmentAlertDto appointmentDetails, AlertStatus status) {
        StringBuilder message = new StringBuilder();
        
        // Shorter format for SMS due to character limits
        message.append(status.getDisplayName().toUpperCase()).append("\n\n");
        
        message.append("Dear ").append(appointmentDetails.getPatientName()).append(",\n");
        
        switch (status) {
            case BOOKED:
                message.append("Appointment booked successfully!\n");
                break;
            case RESCHEDULED:
                message.append("Appointment rescheduled.\n");
                break;
            case CANCELLED:
                message.append("Appointment cancelled.\n");
                break;
            case COMPLETED:
                message.append("Appointment completed. Thank you!\n");
                break;
        }
        
        // Essential details only for SMS
        if (status != AlertStatus.CANCELLED) {
            message.append("\nID: #").append(appointmentDetails.getAppointmentId());
            message.append("\nDate: ").append(appointmentDetails.getAppointmentTime().format(DATE_FORMATTER));
            message.append("\nTime: ").append(appointmentDetails.getAppointmentTime().format(TIME_FORMATTER));
            
            if (appointmentDetails.getDoctor() != null) {
                message.append("\nDoctor: Dr. ").append(appointmentDetails.getDoctor().getFullName());
            }
            
            if (appointmentDetails.getWorkplace() != null) {
                message.append("\nLocation: ").append(appointmentDetails.getWorkplace().getWorkplaceName());
            }
            
            if (appointmentDetails.getQueuePosition() != null && appointmentDetails.getQueuePosition() > 0) {
                message.append("\nQueue: #").append(appointmentDetails.getQueuePosition());
            }
        }
        
        message.append("\n\n- Healthcare App");
        
        return message.toString();
    }
    
    private String getEmojiForStatus(AlertStatus status) {
        switch (status) {
            case BOOKED:
                return "📅";
            case RESCHEDULED:
                return "🔄";
            case CANCELLED:
                return "❌";
            case COMPLETED:
                return "✅";
            default:
                return "📱";
        }
    }
}