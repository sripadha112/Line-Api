package com.app.auth.service;

import com.app.auth.dto.AlertRequestDto;
import com.app.auth.dto.AlertResponseDto;

public interface AlertService {
    
    /**
     * Sends WhatsApp and SMS alerts based on appointment status
     * @param alertRequest contains mobile number, appointment details, and status
     * @return AlertResponseDto with success status and individual service statuses
     */
    AlertResponseDto sendAlert(AlertRequestDto alertRequest);
}