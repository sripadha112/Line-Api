package com.app.auth.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class AppointmentSegregatedDto {
    private String workplaceType; // CLINIC, HOSPITAL
    private String workplaceName;
    private Long workplaceId;
    private List<AppointmentDto> appointments;

    // getters & setters
    public String getWorkplaceType() { return workplaceType; }
    public void setWorkplaceType(String workplaceType) { this.workplaceType = workplaceType; }

    public String getWorkplaceName() { return workplaceName; }
    public void setWorkplaceName(String workplaceName) { this.workplaceName = workplaceName; }

    public Long getWorkplaceId() { return workplaceId; }
    public void setWorkplaceId(Long workplaceId) { this.workplaceId = workplaceId; }

    public List<AppointmentDto> getAppointments() { return appointments; }
    public void setAppointments(List<AppointmentDto> appointments) { this.appointments = appointments; }
}

// Response wrapper for segregated appointments
class AppointmentSegregatedResponse {
    private List<AppointmentSegregatedDto> clinicAppointments;
    private List<AppointmentSegregatedDto> hospitalAppointments;

    public List<AppointmentSegregatedDto> getClinicAppointments() { return clinicAppointments; }
    public void setClinicAppointments(List<AppointmentSegregatedDto> clinicAppointments) { this.clinicAppointments = clinicAppointments; }

    public List<AppointmentSegregatedDto> getHospitalAppointments() { return hospitalAppointments; }
    public void setHospitalAppointments(List<AppointmentSegregatedDto> hospitalAppointments) { this.hospitalAppointments = hospitalAppointments; }
}
