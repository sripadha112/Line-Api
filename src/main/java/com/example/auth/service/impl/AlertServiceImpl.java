package com.example.auth.service.impl;

import com.example.auth.dto.AlertRequestDto;
import com.example.auth.dto.AlertResponseDto;
import com.example.auth.service.AlertService;
import com.example.auth.service.MessageTemplateService;
import com.example.auth.service.WhatsAppService;
import com.example.auth.service.SMSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertServiceImpl implements AlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertServiceImpl.class);
    
    @Autowired
    private MessageTemplateService messageTemplateService;
    
    @Autowired
    private WhatsAppService whatsAppService;
    
    @Autowired
    private SMSService smsService;
    
    @Override
    public AlertResponseDto sendAlert(AlertRequestDto alertRequest) {
        logger.info("Processing alert for mobile: {} with status: {}", 
                   alertRequest.getMobileNumber(), alertRequest.getStatus());
        
        try {
            // Generate messages for WhatsApp and SMS
            String whatsAppMessage = messageTemplateService.generateWhatsAppMessage(
                alertRequest.getAppointmentDetails(), alertRequest.getStatus());
            String smsMessage = messageTemplateService.generateSMSMessage(
                alertRequest.getAppointmentDetails(), alertRequest.getStatus());
            
            // Send WhatsApp message
            String whatsAppStatus = "FAILED";
            try {
                boolean whatsAppSent = whatsAppService.sendMessage(
                    alertRequest.getMobileNumber(), whatsAppMessage);
                whatsAppStatus = whatsAppSent ? "SENT" : "FAILED";
                logger.info("WhatsApp status for {}: {}", alertRequest.getMobileNumber(), whatsAppStatus);
            } catch (Exception e) {
                logger.error("WhatsApp sending failed for {}: {}", 
                           alertRequest.getMobileNumber(), e.getMessage());
                whatsAppStatus = "ERROR: " + e.getMessage();
            }
            
            // Send SMS message
            String smsStatus = "FAILED";
            try {
                boolean smsSent = smsService.sendMessage(
                    alertRequest.getMobileNumber(), smsMessage);
                smsStatus = smsSent ? "SENT" : "FAILED";
                logger.info("SMS status for {}: {}", alertRequest.getMobileNumber(), smsStatus);
            } catch (Exception e) {
                logger.error("SMS sending failed for {}: {}", 
                           alertRequest.getMobileNumber(), e.getMessage());
                smsStatus = "ERROR: " + e.getMessage();
            }
            
            // Determine overall success
            boolean overallSuccess = whatsAppStatus.equals("SENT") || smsStatus.equals("SENT");
            String message = overallSuccess ? 
                "Alert sent successfully" : 
                "Failed to send alert via both WhatsApp and SMS";
            
            return new AlertResponseDto(overallSuccess, message, whatsAppStatus, smsStatus);
            
        } catch (Exception e) {
            logger.error("Error processing alert for {}: {}", 
                        alertRequest.getMobileNumber(), e.getMessage(), e);
            return new AlertResponseDto(false, "Failed to process alert: " + e.getMessage());
        }
    }
}