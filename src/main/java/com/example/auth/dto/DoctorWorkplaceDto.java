package com.example.auth.dto;

public class DoctorWorkplaceDto {
    private Long id;
    private String workplaceName;
    private String workplaceType;
    private String address;
    private String contactNumber;
    private Boolean isPrimary;
    private Long activeAppointmentsCount; // Count of BOOKED appointments (total)
    private Long todayAppointmentsCount; // Count of today's BOOKED appointments
    private Long futureAppointmentsCount; // Count of future BOOKED appointments

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWorkplaceName() { return workplaceName; }
    public void setWorkplaceName(String workplaceName) { this.workplaceName = workplaceName; }

    public String getWorkplaceType() { return workplaceType; }
    public void setWorkplaceType(String workplaceType) { this.workplaceType = workplaceType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public Long getActiveAppointmentsCount() { return activeAppointmentsCount; }
    public void setActiveAppointmentsCount(Long activeAppointmentsCount) { this.activeAppointmentsCount = activeAppointmentsCount; }

    public Long getTodayAppointmentsCount() { return todayAppointmentsCount; }
    public void setTodayAppointmentsCount(Long todayAppointmentsCount) { this.todayAppointmentsCount = todayAppointmentsCount; }

    public Long getFutureAppointmentsCount() { return futureAppointmentsCount; }
    public void setFutureAppointmentsCount(Long futureAppointmentsCount) { this.futureAppointmentsCount = futureAppointmentsCount; }
}
