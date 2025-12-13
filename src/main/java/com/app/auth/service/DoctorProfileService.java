package com.app.auth.service;

import com.app.auth.dto.DoctorProfileUpdateDto;
import com.app.auth.entity.DoctorDetails;

public interface DoctorProfileService {
    
    /**
     * Update doctor profile with flexible field updates
     * 
     * @param doctor The doctor entity to update
     * @param updateRequest The update request containing fields to update
     * @return Response containing update counts
     */
    DoctorProfileUpdateResponse updateDoctorProfile(DoctorDetails doctor, DoctorProfileUpdateDto updateRequest);
    
    /**
     * Response class for doctor profile updates
     */
    public static class DoctorProfileUpdateResponse {
        private String message;
        private int fieldsUpdated;
        private int workspacesProcessed;
        
        public DoctorProfileUpdateResponse(String message, int fieldsUpdated, int workspacesProcessed) {
            this.message = message;
            this.fieldsUpdated = fieldsUpdated;
            this.workspacesProcessed = workspacesProcessed;
        }
        
        // Getters and setters
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getFieldsUpdated() {
            return fieldsUpdated;
        }
        
        public void setFieldsUpdated(int fieldsUpdated) {
            this.fieldsUpdated = fieldsUpdated;
        }
        
        public int getWorkspacesProcessed() {
            return workspacesProcessed;
        }
        
        public void setWorkspacesProcessed(int workspacesProcessed) {
            this.workspacesProcessed = workspacesProcessed;
        }
    }
}
