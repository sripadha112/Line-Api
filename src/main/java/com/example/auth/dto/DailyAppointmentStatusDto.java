package com.example.auth.dto;

import java.time.LocalDate;

public class DailyAppointmentStatusDto {
    private LocalDate date;
    private Long totalAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Long pendingAppointments;
    private Long bookedAppointments;

    public DailyAppointmentStatusDto() {}

    public DailyAppointmentStatusDto(LocalDate date, Long totalAppointments, Long completedAppointments, 
                                   Long cancelledAppointments, Long pendingAppointments, Long bookedAppointments) {
        this.date = date;
        this.totalAppointments = totalAppointments;
        this.completedAppointments = completedAppointments;
        this.cancelledAppointments = cancelledAppointments;
        this.pendingAppointments = pendingAppointments;
        this.bookedAppointments = bookedAppointments;
    }

    // getters & setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(Long totalAppointments) { this.totalAppointments = totalAppointments; }

    public Long getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(Long completedAppointments) { this.completedAppointments = completedAppointments; }

    public Long getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(Long cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }

    public Long getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(Long pendingAppointments) { this.pendingAppointments = pendingAppointments; }

    public Long getBookedAppointments() { return bookedAppointments; }
    public void setBookedAppointments(Long bookedAppointments) { this.bookedAppointments = bookedAppointments; }
}
