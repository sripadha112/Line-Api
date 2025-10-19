package com.example.auth.dto;

import java.time.LocalTime;
import java.time.OffsetDateTime;

public class WorkplaceResponseDto {
    private Long id;
    private String doctor; // Just the doctor name, not the full object
    private String workplaceName;
    private String workplaceType;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String contactNumber;
    private LocalTime morningStartTime;
    private LocalTime morningEndTime;
    private LocalTime eveningStartTime;
    private LocalTime eveningEndTime;
    private Integer checkingDurationMinutes;
    private Boolean isPrimary;
    private OffsetDateTime createdAt;

    // Default constructor
    public WorkplaceResponseDto() {}

    // Constructor for mapping
    public WorkplaceResponseDto(Long id, String doctor, String workplaceName, String workplaceType, 
                              String address, String city, String state, String pincode, 
                              String country, String contactNumber, LocalTime morningStartTime, 
                              LocalTime morningEndTime, LocalTime eveningStartTime, 
                              LocalTime eveningEndTime, Integer checkingDurationMinutes, 
                              Boolean isPrimary, OffsetDateTime createdAt) {
        this.id = id;
        this.doctor = doctor;
        this.workplaceName = workplaceName;
        this.workplaceType = workplaceType;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.country = country;
        this.contactNumber = contactNumber;
        this.morningStartTime = morningStartTime;
        this.morningEndTime = morningEndTime;
        this.eveningStartTime = eveningStartTime;
        this.eveningEndTime = eveningEndTime;
        this.checkingDurationMinutes = checkingDurationMinutes;
        this.isPrimary = isPrimary;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }

    public String getWorkplaceName() { return workplaceName; }
    public void setWorkplaceName(String workplaceName) { this.workplaceName = workplaceName; }

    public String getWorkplaceType() { return workplaceType; }
    public void setWorkplaceType(String workplaceType) { this.workplaceType = workplaceType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

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

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
