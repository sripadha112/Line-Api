package com.example.auth.controller;

import com.example.auth.dto.AlertRequestDto;
import com.example.auth.dto.AlertResponseDto;
import com.example.auth.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alert Service", description = "API for sending WhatsApp and SMS alerts for appointments")
@CrossOrigin(origins = "*")
public class AlertController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);
    
    @Autowired
    private AlertService alertService;
    
    @PostMapping("/send")
    @Operation(summary = "Send appointment alert", 
               description = "Sends WhatsApp and SMS alerts to users based on appointment status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alert sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AlertResponseDto> sendAlert(@Valid @RequestBody AlertRequestDto alertRequest) {
        try {
            logger.info("Received alert request for mobile: {} with status: {}", 
                       alertRequest.getMobileNumber(), alertRequest.getStatus());
            
            AlertResponseDto response = alertService.sendAlert(alertRequest);
            
            if (response.isSuccess()) {
                logger.info("Alert sent successfully for mobile: {}", alertRequest.getMobileNumber());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Alert sending failed for mobile: {} - {}", 
                           alertRequest.getMobileNumber(), response.getMessage());
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new AlertResponseDto(false, "Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error sending alert for mobile: {} - {}", 
                        alertRequest.getMobileNumber(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AlertResponseDto(false, "Failed to send alert: " + e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if alert service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Alert Service is running");
    }
}