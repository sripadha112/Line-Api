package com.example.auth.service.impl;

import com.example.auth.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppServiceImpl implements WhatsAppService {
    
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppServiceImpl.class);
    
    @Value("${whatsapp.api.url:https://graph.facebook.com/v18.0}")
    private String whatsappApiUrl;
    
    @Value("${whatsapp.phone.number.id:YOUR_PHONE_NUMBER_ID}")
    private String phoneNumberId;
    
    @Value("${whatsapp.access.token:YOUR_ACCESS_TOKEN}")
    private String accessToken;
    
    @Value("${whatsapp.enabled:false}")
    private boolean whatsappEnabled;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public WhatsAppServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public boolean sendMessage(String mobileNumber, String message) throws Exception {
        if (!whatsappEnabled) {
            logger.warn("WhatsApp service is disabled. Message not sent to: {}", mobileNumber);
            return false;
        }
        
        try {
            // Clean mobile number (remove any formatting)
            String cleanMobileNumber = cleanMobileNumber(mobileNumber);
            
            // Prepare WhatsApp API request
            String url = String.format("%s/%s/messages", whatsappApiUrl, phoneNumberId);
            
            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("to", cleanMobileNumber);
            requestBody.put("type", "text");
            
            Map<String, Object> textObject = new HashMap<>();
            textObject.put("body", message);
            textObject.put("preview_url", false);
            requestBody.put("text", textObject);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            // Create request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Send request
            logger.info("Sending WhatsApp message to: {}", cleanMobileNumber);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("WhatsApp message sent successfully to: {}", cleanMobileNumber);
                return true;
            } else {
                logger.error("WhatsApp API returned status: {} for mobile: {}", 
                           response.getStatusCode(), cleanMobileNumber);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error sending WhatsApp message to {}: {}", mobileNumber, e.getMessage(), e);
            throw new Exception("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clean mobile number by removing any formatting and ensuring proper format
     */
    private String cleanMobileNumber(String mobileNumber) {
        if (mobileNumber == null) {
            return null;
        }
        
        // Remove all non-digit characters except +
        String cleaned = mobileNumber.replaceAll("[^\\d+]", "");
        
        // If number doesn't start with +, assume it needs country code
        if (!cleaned.startsWith("+")) {
            // Add default country code if not present (you may want to make this configurable)
            if (cleaned.length() == 10) {
                cleaned = "+91" + cleaned; // Default to India, adjust as needed
            } else {
                cleaned = "+" + cleaned;
            }
        }
        
        return cleaned;
    }
}