package com.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for updating doctor profile with flexible field updates
 * All fields are optional - only provided fields will be updated
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorProfileUpdateDto {
    
    private String mobileNumber;
    private String fullName;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String specialization;
    private String designation;
    
    @Valid
    @JsonProperty("workspaces")
    private List<WorkspaceUpdateDto> workspaces;
    
    public DoctorProfileUpdateDto() {}
    
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
    
    public List<WorkspaceUpdateDto> getWorkspaces() {
        return workspaces;
    }
    
    public void setWorkspaces(List<WorkspaceUpdateDto> workspaces) {
        this.workspaces = workspaces;
    }
    
    /**
     * DTO for workspace updates within doctor profile
     */
    public static class WorkspaceUpdateDto {
        private Long id; // For updating existing workspace, null for new workspace
        private String workplaceName;
        private String workplaceType;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String country;
        private LocalTime morningStartTime;
        private LocalTime morningEndTime;
        private LocalTime eveningStartTime;
        private LocalTime eveningEndTime;
        private Integer checkingDurationMinutes;
        private Boolean isPrimary;
        private String contactNumber;
        
        public WorkspaceUpdateDto() {}
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
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
        
        public String getContactNumber() {
            return contactNumber;
        }
        
        public void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }
    }
}
