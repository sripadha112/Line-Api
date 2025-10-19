package com.example.auth.dto;

import java.util.List;

/**
 * DTO for doctor's appointment history grouped by workspace
 * Contains workspace information and list of past appointments for that workspace
 */
public class DoctorHistoryWorkspaceDto {
    
    private Long workplaceId;
    private String workplaceName;
    private String workplaceType;
    private String workplaceAddress;
    private List<DoctorHistoryAppointmentDto> appointments;
    
    public DoctorHistoryWorkspaceDto() {}
    
    public DoctorHistoryWorkspaceDto(Long workplaceId, String workplaceName, String workplaceType, 
                                   String workplaceAddress, List<DoctorHistoryAppointmentDto> appointments) {
        this.workplaceId = workplaceId;
        this.workplaceName = workplaceName;
        this.workplaceType = workplaceType;
        this.workplaceAddress = workplaceAddress;
        this.appointments = appointments;
    }
    
    public Long getWorkplaceId() {
        return workplaceId;
    }
    
    public void setWorkplaceId(Long workplaceId) {
        this.workplaceId = workplaceId;
    }
    
    public String getWorkplaceName() {
        return workplaceName;
    }
    
    public void setWorkplaceName(String workplaceName) {
        this.workplaceName = workplaceName;
    }
    
    public String getWorkplaceType() {
        return workplaceType;
    }
    
    public void setWorkplaceType(String workplaceType) {
        this.workplaceType = workplaceType;
    }
    
    public String getWorkplaceAddress() {
        return workplaceAddress;
    }
    
    public void setWorkplaceAddress(String workplaceAddress) {
        this.workplaceAddress = workplaceAddress;
    }
    
    public List<DoctorHistoryAppointmentDto> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<DoctorHistoryAppointmentDto> appointments) {
        this.appointments = appointments;
    }
}
