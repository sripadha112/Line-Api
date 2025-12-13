package com.app.auth.service;

public interface WhatsAppService {
    
    /**
     * Send WhatsApp message to the specified mobile number
     * @param mobileNumber recipient's mobile number (with country code)
     * @param message message content to send
     * @return true if message was sent successfully, false otherwise
     * @throws Exception if there's an error in sending the message
     */
    boolean sendMessage(String mobileNumber, String message) throws Exception;
}