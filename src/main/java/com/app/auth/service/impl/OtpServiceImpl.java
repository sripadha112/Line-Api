package com.app.auth.service.impl;

import com.app.auth.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;

@Service
public class OtpServiceImpl implements OtpService {
    
    private static final Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);
    
    @Value("${otp.sms.enabled:false}")
    private boolean otpSmsEnabled;
    
    // Use existing Twilio configuration from SMS service
    @Value("${sms.twilio.account.sid:YOUR_TWILIO_ACCOUNT_SID}")
    private String twilioAccountSid;
    
    @Value("${sms.twilio.auth.token:YOUR_TWILIO_AUTH_TOKEN}")
    private String twilioAuthToken;
    
    @Value("${sms.twilio.from.number:YOUR_TWILIO_PHONE_NUMBER}")
    private String twilioFromNumber;
    
    @Value("${sms.twilio.api.url:https://api.twilio.com/2010-04-01}")
    private String twilioApiUrl;
    
    @Value("${otp.country.code:+91}")
    private String defaultCountryCode;
    
    private final RestTemplate restTemplate;
    
    public OtpServiceImpl() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public boolean sendOtp(String mobileNumber, String otpCode) throws Exception {
        if (!otpSmsEnabled) {
            logger.warn("OTP SMS is disabled. OTP not sent via SMS to: {}", mobileNumber);
            // Print to console as fallback when SMS is disabled
            logger.info("[OTP CONSOLE FALLBACK] Mobile: {} | OTP: {}", mobileNumber, otpCode);
            System.out.println("[OTP] mobile=" + mobileNumber + " otp=" + otpCode);
            return false;
        }
        
        try {
            String fullMobileNumber = formatMobileNumber(mobileNumber);
            String message = generateOtpMessage(otpCode);
            
            // Use Twilio for OTP SMS (reusing existing Twilio configuration)
            return sendViaTwilio(fullMobileNumber, message);
            
        } catch (Exception e) {
            logger.error("Error sending OTP to {}: {}", mobileNumber, e.getMessage(), e);
            // Fallback to console logging on error
            logger.info("[OTP CONSOLE FALLBACK] Mobile: {} | OTP: {}", mobileNumber, otpCode);
            System.out.println("[OTP FALLBACK] mobile=" + mobileNumber + " otp=" + otpCode);
            throw e;
        }
    }
    
    private boolean sendViaTwilio(String mobileNumber, String message) throws Exception {
        try {
            // Twilio API endpoint
            String url = String.format("%s/Accounts/%s/Messages.json", twilioApiUrl, twilioAccountSid);
            
            // Create request body for Twilio
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("From", twilioFromNumber);
            requestBody.add("To", mobileNumber);
            requestBody.add("Body", message);
            
            // Create Basic Auth header
            String auth = twilioAccountSid + ":" + twilioAuthToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            logger.info("Sending OTP via Twilio to: {}", mobileNumber);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                logger.info("OTP sent successfully via Twilio to: {}", mobileNumber);
                return true;
            } else {
                logger.error("Twilio API returned status: {} for mobile: {}", 
                           response.getStatusCode(), mobileNumber);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error sending OTP via Twilio to {}: {}", mobileNumber, e.getMessage(), e);
            throw new Exception("Failed to send OTP via Twilio: " + e.getMessage(), e);
        }
    }
    
    /**
     * Format mobile number to include country code
     */
    private String formatMobileNumber(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            return mobileNumber;
        }
        
        // Remove any non-digit characters except +
        String cleaned = mobileNumber.replaceAll("[^\\d+]", "");
        
        // If number doesn't start with +, add default country code
        if (!cleaned.startsWith("+")) {
            if (cleaned.length() == 10) {
                cleaned = defaultCountryCode + cleaned;
            } else {
                cleaned = "+" + cleaned;
            }
        }
        
        return cleaned;
    }
    
    /**
     * Generate OTP message template
     */
    private String generateOtpMessage(String otpCode) {
        return String.format(
            "Your OTP for Healthcare App login is: %s\n\n" +
            "This OTP will expire in 5 minutes.\n" +
            "Please do not share this code with anyone.\n\n" +
            "- Healthcare App",
            otpCode
        );
    }
}