package com.app.auth.dto;

public class AlertResponseDto {
    private boolean success;
    private String message;
    private String whatsappStatus;
    private String smsStatus;
    
    public AlertResponseDto() {}
    
    public AlertResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public AlertResponseDto(boolean success, String message, String whatsappStatus, String smsStatus) {
        this.success = success;
        this.message = message;
        this.whatsappStatus = whatsappStatus;
        this.smsStatus = smsStatus;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getWhatsappStatus() {
        return whatsappStatus;
    }
    
    public void setWhatsappStatus(String whatsappStatus) {
        this.whatsappStatus = whatsappStatus;
    }
    
    public String getSmsStatus() {
        return smsStatus;
    }
    
    public void setSmsStatus(String smsStatus) {
        this.smsStatus = smsStatus;
    }
}