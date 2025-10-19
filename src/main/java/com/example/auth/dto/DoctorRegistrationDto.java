package com.example.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;

public class DoctorRegistrationDto {

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp="^\\d{10}$", message = "Mobile number must be 10-15 digits")
    private String mobileNumber;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Pattern(regexp="^\\d{6,10}$", message = "Pincode must be 6-10 digits")
    private String pincode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    private String specialization;

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    // New workspace list - at least one workspace is required
    @NotEmpty(message = "At least one workspace is required")
    @Valid
    private List<WorkspaceDto> workspaces;

    // Constructors
    public DoctorRegistrationDto() {}

    // Getters and Setters
    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public List<WorkspaceDto> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<WorkspaceDto> workspaces) {
        this.workspaces = workspaces;
    }
}
