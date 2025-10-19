package com.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * DTO for creating a new doctor workplace
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorWorkplaceCreateDto {
    
    @NotBlank(message = "Workplace name is required")
    private String workplaceName;
    
    private String workplaceType;
    
    @NotBlank(message = "Address is required")
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
    
    @NotNull(message = "Checking duration is required")
    private Integer checkingDurationMinutes;
    
    private Boolean isPrimary;
    
    public DoctorWorkplaceCreateDto() {}
    
    // Getters and setters
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPincode() {
        return pincode;
    }
    
    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public LocalTime getMorningStartTime() {
        return morningStartTime;
    }
    
    public void setMorningStartTime(LocalTime morningStartTime) {
        this.morningStartTime = morningStartTime;
    }
    
    public LocalTime getMorningEndTime() {
        return morningEndTime;
    }
    
    public void setMorningEndTime(LocalTime morningEndTime) {
        this.morningEndTime = morningEndTime;
    }
    
    public LocalTime getEveningStartTime() {
        return eveningStartTime;
    }
    
    public void setEveningStartTime(LocalTime eveningStartTime) {
        this.eveningStartTime = eveningStartTime;
    }
    
    public LocalTime getEveningEndTime() {
        return eveningEndTime;
    }
    
    public void setEveningEndTime(LocalTime eveningEndTime) {
        this.eveningEndTime = eveningEndTime;
    }
    
    public Integer getCheckingDurationMinutes() {
        return checkingDurationMinutes;
    }
    
    public void setCheckingDurationMinutes(Integer checkingDurationMinutes) {
        this.checkingDurationMinutes = checkingDurationMinutes;
    }
    
    public Boolean getIsPrimary() {
        return isPrimary;
    }
    
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}
