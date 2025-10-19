package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class CancelDayRequest {
    @NotBlank
    private String date; // ISO date e.g. 2025-09-01

    private String reason;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
