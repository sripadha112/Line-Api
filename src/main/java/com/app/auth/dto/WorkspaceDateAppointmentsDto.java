package com.app.auth.dto;

import java.util.List;

/**
 * DTO for workspace appointments grouped by date
 * Contains appointment date and list of appointments for that date
 */
public class WorkspaceDateAppointmentsDto {
    
    private String appointmentDate;
    private List<WorkspaceAppointmentDto> appointments;
    
    public WorkspaceDateAppointmentsDto() {}
    
    public WorkspaceDateAppointmentsDto(String appointmentDate, List<WorkspaceAppointmentDto> appointments) {
        this.appointmentDate = appointmentDate;
        this.appointments = appointments;
    }
    
    public String getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public List<WorkspaceAppointmentDto> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<WorkspaceAppointmentDto> appointments) {
        this.appointments = appointments;
    }
}
