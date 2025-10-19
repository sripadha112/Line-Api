package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "doctor_workplaces")
public class DoctorWorkplace {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workplace_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorDetails doctor;

    @Column(name = "workplace_name", nullable = false, length = 150)
    private String workplaceName;

    @Column(name = "workplace_type", nullable = false, length = 50)
    private String workplaceType; // CLINIC, HOSPITAL, HOME, etc.

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "contact_number", length = 15)
    private String contactNumber;

    // Timing fields for this specific workspace
    @Column(name = "morning_start_time")
    private LocalTime morningStartTime;

    @Column(name = "morning_end_time")
    private LocalTime morningEndTime;

    @Column(name = "evening_start_time")
    private LocalTime eveningStartTime;

    @Column(name = "evening_end_time")
    private LocalTime eveningEndTime;

    @Column(name = "checking_duration_minutes")
    private Integer checkingDurationMinutes = 30;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // Constructors
    public DoctorWorkplace() {}

    // getters & setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DoctorDetails getDoctor() { return doctor; }
    public void setDoctor(DoctorDetails doctor) { this.doctor = doctor; }

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

    public LocalTime getMorningStartTime() { return morningStartTime; }
    public void setMorningStartTime(LocalTime morningStartTime) { this.morningStartTime = morningStartTime; }

    public LocalTime getMorningEndTime() { return morningEndTime; }
    public void setMorningEndTime(LocalTime morningEndTime) { this.morningEndTime = morningEndTime; }

    public LocalTime getEveningStartTime() { return eveningStartTime; }
    public void setEveningStartTime(LocalTime eveningStartTime) { this.eveningStartTime = eveningStartTime; }

    public LocalTime getEveningEndTime() { return eveningEndTime; }
    public void setEveningEndTime(LocalTime eveningEndTime) { this.eveningEndTime = eveningEndTime; }

    public Integer getCheckingDurationMinutes() { return checkingDurationMinutes; }
    public void setCheckingDurationMinutes(Integer checkingDurationMinutes) { this.checkingDurationMinutes = checkingDurationMinutes; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
