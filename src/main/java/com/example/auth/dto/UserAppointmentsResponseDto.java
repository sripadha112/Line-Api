package com.example.auth.dto;

import java.util.List;
import java.util.Map;

public class UserAppointmentsResponseDto {
    private Map<String, List<UserAppointmentDto>> appointmentsByDate;
    private int totalAppointments;

    // Constructors
    public UserAppointmentsResponseDto() {}

    public UserAppointmentsResponseDto(Map<String, List<UserAppointmentDto>> appointmentsByDate, int totalAppointments) {
        this.appointmentsByDate = appointmentsByDate;
        this.totalAppointments = totalAppointments;
    }

    // Getters and Setters
    public Map<String, List<UserAppointmentDto>> getAppointmentsByDate() {
        return appointmentsByDate;
    }

    public void setAppointmentsByDate(Map<String, List<UserAppointmentDto>> appointmentsByDate) {
        this.appointmentsByDate = appointmentsByDate;
    }

    public int getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(int totalAppointments) {
        this.totalAppointments = totalAppointments;
    }
}
