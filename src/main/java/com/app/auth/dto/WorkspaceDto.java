package com.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public class WorkspaceDto {

    @NotBlank(message = "Workplace name is required")
    @Size(min = 2, max = 150, message = "Workplace name must be between 2 and 150 characters")
    private String workplaceName;

    @NotBlank(message = "Workplace type is required")
    @Size(max = 50, message = "Workplace type must not exceed 50 characters")
    private String workplaceType; // CLINIC, HOSPITAL, HOME, etc.

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp="^\\d{6,10}$", message = "Pincode must be 6-10 digits")
    private String pincode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    // Timing fields for this workspace
    private LocalTime morningStartTime;
    private LocalTime morningEndTime;
    private LocalTime eveningStartTime;
    private LocalTime eveningEndTime;

    private Integer checkingDurationMinutes;

    private Boolean isPrimary = false; // Mark if this is the primary workplace

    // Constructors
    public WorkspaceDto() {}

    // Getters and Setters
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
