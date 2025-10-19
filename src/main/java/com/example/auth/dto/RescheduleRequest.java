package com.example.auth.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class RescheduleRequest {
    private OffsetDateTime startFrom;   // optional, shift appointments at/after this time
    @NotNull
    private Integer shiftMinutes;      // positive pushes forward
    private Integer maxShiftMinutes = 1440; // default cap of 24 hours

    public OffsetDateTime getStartFrom() { return startFrom; }
    public void setStartFrom(OffsetDateTime startFrom) { this.startFrom = startFrom; }
    public Integer getShiftMinutes() { return shiftMinutes; }
    public void setShiftMinutes(Integer shiftMinutes) { this.shiftMinutes = shiftMinutes; }
    public Integer getMaxShiftMinutes() { return maxShiftMinutes; }
    public void setMaxShiftMinutes(Integer maxShiftMinutes) { this.maxShiftMinutes = maxShiftMinutes; }
}
