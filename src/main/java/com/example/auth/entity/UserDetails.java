package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_details",
       uniqueConstraints = {
         @UniqueConstraint(name="uk_user_email", columnNames = "email"),
         @UniqueConstraint(name="uk_user_mobile", columnNames = "mobile_number")
       })
public class UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "mobile_number", nullable = false, length = 15)
    private String mobileNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // FCM Token for Push Notifications
    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Column(name = "device_type", length = 20)
    private String deviceType; // "android" or "ios"

    @Column(name = "last_token_update")
    private OffsetDateTime lastTokenUpdate;

    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled = true;

    // Medical Profile Fields
    // Basic Vitals
    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "age")
    private Integer age;

    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;

    @Column(name = "blood_oxygen_level")
    private Double bloodOxygenLevel;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "body_temperature")
    private Double bodyTemperature;

    // Medical History
    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "has_diabetes")
    private Boolean hasDiabetes = false;

    @Column(name = "has_hypertension")
    private Boolean hasHypertension = false;

    @Column(name = "has_heart_disease")
    private Boolean hasHeartDisease = false;

    @Column(name = "has_kidney_disease")
    private Boolean hasKidneyDisease = false;

    @Column(name = "has_liver_disease")
    private Boolean hasLiverDisease = false;

    // Medical Details (stored as comma-separated strings)
    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedicationsStr; // comma-separated values

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergiesStr; // comma-separated values

    @Column(name = "chronic_diseases", columnDefinition = "TEXT")
    private String chronicDiseasesStr; // comma-separated values

    @Column(name = "previous_surgeries", columnDefinition = "TEXT")
    private String previousSurgeriesStr; // comma-separated values

    @Column(name = "vaccinations", columnDefinition = "TEXT")
    private String vaccinationsStr; // comma-separated values

    // Emergency Contact
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_number", length = 15)
    private String emergencyContactNumber;

    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;

    // Additional Notes
    @Column(name = "medical_notes", columnDefinition = "TEXT")
    private String medicalNotes;

    @Column(name = "prescription", columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "family_medical_history", columnDefinition = "TEXT")
    private String familyMedicalHistory;

    // getters & setters

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

    public String getCurrentMedicationsStr() { return currentMedicationsStr; }
    public void setCurrentMedicationsStr(String currentMedicationsStr) { this.currentMedicationsStr = currentMedicationsStr; }

    public String getAllergiesStr() { return allergiesStr; }
    public void setAllergiesStr(String allergiesStr) { this.allergiesStr = allergiesStr; }

    public String getChronicDiseasesStr() { return chronicDiseasesStr; }
    public void setChronicDiseasesStr(String chronicDiseasesStr) { this.chronicDiseasesStr = chronicDiseasesStr; }

    public String getPreviousSurgeriesStr() { return previousSurgeriesStr; }
    public void setPreviousSurgeriesStr(String previousSurgeriesStr) { this.previousSurgeriesStr = previousSurgeriesStr; }

    public String getVaccinationsStr() { return vaccinationsStr; }
    public void setVaccinationsStr(String vaccinationsStr) { this.vaccinationsStr = vaccinationsStr; }

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

    // FCM Token Getters and Setters
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { 
        this.fcmToken = fcmToken; 
        this.lastTokenUpdate = OffsetDateTime.now();
    }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public OffsetDateTime getLastTokenUpdate() { return lastTokenUpdate; }
    public void setLastTokenUpdate(OffsetDateTime lastTokenUpdate) { this.lastTokenUpdate = lastTokenUpdate; }

    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
