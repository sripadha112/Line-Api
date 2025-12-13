package com.app.auth.service.impl;

import com.app.auth.service.SMSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SMSServiceImpl implements SMSService {
    
    private static final Logger logger = LoggerFactory.getLogger(SMSServiceImpl.class);
    
    // Twilio Configuration (popular SMS provider)
    @Value("${sms.twilio.account.sid:YOUR_TWILIO_ACCOUNT_SID}")
    private String twilioAccountSid;
    
    @Value("${sms.twilio.auth.token:YOUR_TWILIO_AUTH_TOKEN}")
    private String twilioAuthToken;
    
    @Value("${sms.twilio.from.number:YOUR_TWILIO_PHONE_NUMBER}")
    private String twilioFromNumber;
    
    @Value("${sms.twilio.api.url:https://api.twilio.com/2010-04-01}")
    private String twilioApiUrl;
    
    // AWS SNS Configuration (alternative)
    @Value("${sms.aws.region:us-east-1}")
    private String awsRegion;
    
    @Value("${sms.aws.access.key:YOUR_AWS_ACCESS_KEY}")
    private String awsAccessKey;
    
    @Value("${sms.aws.secret.key:YOUR_AWS_SECRET_KEY}")
    private String awsSecretKey;
    
    // General SMS Configuration
    @Value("${sms.provider:twilio}")
    private String smsProvider;
    
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;
    
    private final RestTemplate restTemplate;
    
    public SMSServiceImpl() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public boolean sendMessage(String mobileNumber, String message) throws Exception {
        if (!smsEnabled) {
            logger.warn("SMS service is disabled. Message not sent to: {}", mobileNumber);
            return false;
        }
        
        switch (smsProvider.toLowerCase()) {
            case "twilio":
                return sendViaTwilio(mobileNumber, message);
            case "aws":
                return sendViaAWSSNS(mobileNumber, message);
            default:
                logger.error("Unknown SMS provider: {}", smsProvider);
                return false;
        }
    }
    
    private boolean sendViaTwilio(String mobileNumber, String message) throws Exception {
        try {
            String cleanMobileNumber = cleanMobileNumber(mobileNumber);
            
            // Twilio API endpoint
            String url = String.format("%s/Accounts/%s/Messages.json", twilioApiUrl, twilioAccountSid);
            
            // Create request body for Twilio
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("From", twilioFromNumber);
            requestBody.add("To", cleanMobileNumber);
            requestBody.add("Body", message);
            
            // Create Basic Auth header
            String auth = twilioAccountSid + ":" + twilioAuthToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            logger.info("Sending SMS via Twilio to: {}", cleanMobileNumber);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                logger.info("SMS sent successfully via Twilio to: {}", cleanMobileNumber);
                return true;
            } else {
                logger.error("Twilio API returned status: {} for mobile: {}", 
                           response.getStatusCode(), cleanMobileNumber);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error sending SMS via Twilio to {}: {}", mobileNumber, e.getMessage(), e);
            throw new Exception("Failed to send SMS via Twilio: " + e.getMessage(), e);
        }
    }
    
    private boolean sendViaAWSSNS(String mobileNumber, String message) throws Exception {
        try {
            // AWS SNS implementation would go here
            // This is a placeholder for AWS SNS integration
            logger.info("AWS SNS SMS sending not implemented yet for: {}", mobileNumber);
            
            // For now, simulate success for demo purposes
            logger.info("SMS would be sent via AWS SNS to: {} with message length: {}", 
                       mobileNumber, message.length());
            return true;
            
        } catch (Exception e) {
            logger.error("Error sending SMS via AWS SNS to {}: {}", mobileNumber, e.getMessage(), e);
            throw new Exception("Failed to send SMS via AWS SNS: " + e.getMessage(), e);
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
            // Add default country code if not present
            if (cleaned.length() == 10) {
                cleaned = "+91" + cleaned; // Default to India, adjust as needed
            } else {
                cleaned = "+" + cleaned;
            }
        }
        
        return cleaned;
    }
}