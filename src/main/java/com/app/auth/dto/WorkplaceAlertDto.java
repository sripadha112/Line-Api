package com.app.auth.dto;

public class WorkplaceAlertDto {
    private Long id;
    private String workplaceName;
    private String workplaceType;
    private String address;
    private String contactNumber;
    
    // Getters and Setters
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
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}