package com.app.auth.dto;

import java.util.List;

public class DoctorSearchResponseDto {
    private Long doctorId;
    private String doctorName;
    private Boolean verified;
    private String specialization;
    private String designation;
    private String profileImage;
    private Integer experience;
    private List<WorkplaceDto> workplaces;
    
    // Constructors
    public DoctorSearchResponseDto() {}
    
    public DoctorSearchResponseDto(Long doctorId, String doctorName, String specialization, String designation) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.designation = designation;
    }
    
    // Getters and Setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    
    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }
    
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public List<WorkplaceDto> getWorkplaces() { return workplaces; }
    public void setWorkplaces(List<WorkplaceDto> workplaces) { this.workplaces = workplaces; }
    
    // Inner class for workplace details
    public static class WorkplaceDto {
        private Long workplaceId;
        private String workplaceName;
        private String workplaceType;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String country;
        private String contactNumber;
        private String morningStartTime;
        private String morningEndTime;
        private String eveningStartTime;
        private String eveningEndTime;
        private Integer checkingDurationMinutes;
        private Boolean isPrimary;
        
        // Constructors
        public WorkplaceDto() {}
        
        // Getters and Setters
        public Long getWorkplaceId() { return workplaceId; }
        public void setWorkplaceId(Long workplaceId) { this.workplaceId = workplaceId; }
        
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
        
        public String getMorningStartTime() { return morningStartTime; }
        public void setMorningStartTime(String morningStartTime) { this.morningStartTime = morningStartTime; }
        
        public String getMorningEndTime() { return morningEndTime; }
        public void setMorningEndTime(String morningEndTime) { this.morningEndTime = morningEndTime; }
        
        public String getEveningStartTime() { return eveningStartTime; }
        public void setEveningStartTime(String eveningStartTime) { this.eveningStartTime = eveningStartTime; }
        
        public String getEveningEndTime() { return eveningEndTime; }
        public void setEveningEndTime(String eveningEndTime) { this.eveningEndTime = eveningEndTime; }
        
        public Integer getCheckingDurationMinutes() { return checkingDurationMinutes; }
        public void setCheckingDurationMinutes(Integer checkingDurationMinutes) { this.checkingDurationMinutes = checkingDurationMinutes; }
        
        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    }
}
