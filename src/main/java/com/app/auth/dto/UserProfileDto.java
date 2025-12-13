package com.app.auth.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class UserProfileDto {
    
    // Basic User Details
    private Long id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Medical Profile Fields
    // Basic Vitals
    private Double heightCm;
    private Double weightKg;
    private Integer age;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Double bloodOxygenLevel;
    private Integer heartRate;
    private Double bodyTemperature;

    // Medical History
    private String bloodGroup;
    private Boolean hasDiabetes;
    private Boolean hasHypertension;
    private Boolean hasHeartDisease;
    private Boolean hasKidneyDisease;
    private Boolean hasLiverDisease;

    // Medical Details (as lists for easy use)
    private List<String> currentMedications;
    private List<String> allergies;
    private List<String> chronicDiseases;
    private List<String> previousSurgeries;
    private List<String> vaccinations;

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String emergencyContactRelation;

    // Additional Notes
    private String medicalNotes;
    private String prescription;
    private String familyMedicalHistory;

    // Constructors
    public UserProfileDto() {}

    public UserProfileDto(Long id, String fullName, String email, String mobileNumber) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
    }

    // Basic User Details Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

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

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Medical Profile Getters and Setters
    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getBloodPressureSystolic() { return bloodPressureSystolic; }
    public void setBloodPressureSystolic(Integer bloodPressureSystolic) { this.bloodPressureSystolic = bloodPressureSystolic; }

    public Integer getBloodPressureDiastolic() { return bloodPressureDiastolic; }
    public void setBloodPressureDiastolic(Integer bloodPressureDiastolic) { this.bloodPressureDiastolic = bloodPressureDiastolic; }

    public Double getBloodOxygenLevel() { return bloodOxygenLevel; }
    public void setBloodOxygenLevel(Double bloodOxygenLevel) { this.bloodOxygenLevel = bloodOxygenLevel; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Double getBodyTemperature() { return bodyTemperature; }
    public void setBodyTemperature(Double bodyTemperature) { this.bodyTemperature = bodyTemperature; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public Boolean getHasDiabetes() { return hasDiabetes; }
    public void setHasDiabetes(Boolean hasDiabetes) { this.hasDiabetes = hasDiabetes; }

    public Boolean getHasHypertension() { return hasHypertension; }
    public void setHasHypertension(Boolean hasHypertension) { this.hasHypertension = hasHypertension; }

    public Boolean getHasHeartDisease() { return hasHeartDisease; }
    public void setHasHeartDisease(Boolean hasHeartDisease) { this.hasHeartDisease = hasHeartDisease; }

    public Boolean getHasKidneyDisease() { return hasKidneyDisease; }
    public void setHasKidneyDisease(Boolean hasKidneyDisease) { this.hasKidneyDisease = hasKidneyDisease; }

    public Boolean getHasLiverDisease() { return hasLiverDisease; }
    public void setHasLiverDisease(Boolean hasLiverDisease) { this.hasLiverDisease = hasLiverDisease; }

    public List<String> getCurrentMedications() { return currentMedications; }
    public void setCurrentMedications(List<String> currentMedications) { this.currentMedications = currentMedications; }

    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }

    public List<String> getChronicDiseases() { return chronicDiseases; }
    public void setChronicDiseases(List<String> chronicDiseases) { this.chronicDiseases = chronicDiseases; }

    public List<String> getPreviousSurgeries() { return previousSurgeries; }
    public void setPreviousSurgeries(List<String> previousSurgeries) { this.previousSurgeries = previousSurgeries; }

    public List<String> getVaccinations() { return vaccinations; }
    public void setVaccinations(List<String> vaccinations) { this.vaccinations = vaccinations; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public void setEmergencyContactNumber(String emergencyContactNumber) { this.emergencyContactNumber = emergencyContactNumber; }

    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public void setEmergencyContactRelation(String emergencyContactRelation) { this.emergencyContactRelation = emergencyContactRelation; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public String getFamilyMedicalHistory() { return familyMedicalHistory; }
    public void setFamilyMedicalHistory(String familyMedicalHistory) { this.familyMedicalHistory = familyMedicalHistory; }
}
