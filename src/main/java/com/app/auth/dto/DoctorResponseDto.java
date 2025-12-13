package com.app.auth.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class DoctorResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String specialization;
    private String designation;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private OffsetDateTime createdAt;
    private List<WorkplaceResponseDto> workplaces;

    // Default constructor
    public DoctorResponseDto() {}

    // Constructor for mapping
    public DoctorResponseDto(Long id, String fullName, String email, 
                           String mobileNumber, String specialization, String designation, 
                           String address, String city, String state, String pincode, 
                           String country, OffsetDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.specialization = specialization;
        this.designation = designation;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.country = country;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public List<WorkplaceResponseDto> getWorkplaces() { return workplaces; }
    public void setWorkplaces(List<WorkplaceResponseDto> workplaces) { this.workplaces = workplaces; }
}
