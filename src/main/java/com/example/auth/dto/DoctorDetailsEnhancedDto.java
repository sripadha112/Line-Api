package com.example.auth.dto;

import java.time.LocalTime;
import java.util.List;

public class DoctorDetailsEnhancedDto {
    private Long id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String specialization;
    private String designation;
    private String address;
    private String pincode;
    
    // Enhanced fields
    private LocalTime morningStartTime;
    private LocalTime morningEndTime;
    private LocalTime eveningStartTime;
    private LocalTime eveningEndTime;
    private Integer checkingDurationMinutes;
    
    // Workplaces
    private List<DoctorWorkplaceDto> workplaces;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public LocalTime getMorningStartTime() { return morningStartTime; }
    public void setMorningStartTime(LocalTime morningStartTime) { this.morningStartTime = morningStartTime; }

    public LocalTime getMorningEndTime() { return morningEndTime; }
    public void setMorningEndTime(LocalTime morningEndTime) { this.morningEndTime = morningEndTime; }

    public LocalTime getEveningStartTime() { return eveningStartTime; }
    public void setEveningStartTime(LocalTime eveningStartTime) { this.eveningStartTime = eveningStartTime; }

    public LocalTime getEveningEndTime() { return eveningEndTime; }
    public void setEveningEndTime(LocalTime eveningEndTime) { this.eveningEndTime = eveningEndTime; }

    public Integer getCheckingDurationMinutes() { return checkingDurationMinutes; }
    public void setCheckingDurationMinutes(Integer checkingDurationMinutes) { this.checkingDurationMinutes = checkingDurationMinutes; }

    public List<DoctorWorkplaceDto> getWorkplaces() { return workplaces; }
    public void setWorkplaces(List<DoctorWorkplaceDto> workplaces) { this.workplaces = workplaces; }
}
