package com.example.auth.service;

import com.example.auth.dto.AlertRequestDto;
import com.example.auth.dto.AlertResponseDto;

public interface AlertService {
    
    /**
     * Sends WhatsApp and SMS alerts based on appointment status
     * @param alertRequest contains mobile number, appointment details, and status
     * @return AlertResponseDto with success status and individual service statuses
     */
    AlertResponseDto sendAlert(AlertRequestDto alertRequest);
}