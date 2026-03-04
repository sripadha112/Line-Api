package com.app.auth.dto;

import java.math.BigDecimal;

public class MedicineDto {
    private Integer id;
    private String medicineName;
    private String composition1;
    private String composition2;
    private BigDecimal priceInr;
    private String manufacturer;
    private String packSize;
    private String type;
    private String doctorSpecialization;
    private Integer usageScore;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getComposition1() { return composition1; }
    public void setComposition1(String composition1) { this.composition1 = composition1; }

    public String getComposition2() { return composition2; }
    public void setComposition2(String composition2) { this.composition2 = composition2; }

    public BigDecimal getPriceInr() { return priceInr; }
    public void setPriceInr(BigDecimal priceInr) { this.priceInr = priceInr; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getPackSize() { return packSize; }
    public void setPackSize(String packSize) { this.packSize = packSize; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    public Integer getUsageScore() { return usageScore; }
    public void setUsageScore(Integer usageScore) { this.usageScore = usageScore; }
}
