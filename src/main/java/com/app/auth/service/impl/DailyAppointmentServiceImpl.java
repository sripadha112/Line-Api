package com.app.auth.service.impl;

import com.app.auth.dto.DailyAppointmentStatusDto;
import com.app.auth.entity.Appointment;
import com.app.auth.repository.AppointmentRepository;
import com.app.auth.service.DailyAppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class DailyAppointmentServiceImpl implements DailyAppointmentService {

    private final AppointmentRepository appointmentRepository;

    public DailyAppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public DailyAppointmentStatusDto getCurrentDateAppointmentStatus() {
        return getAppointmentStatusForDate(LocalDate.now());
    }

    @Override
    public DailyAppointmentStatusDto getAppointmentStatusForDate(LocalDate date) {
        OffsetDateTime dayStart = OffsetDateTime.of(date, java.time.LocalTime.MIDNIGHT, ZoneOffset.UTC);
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> appointments = appointmentRepository.findByAppointmentTimeBetween(dayStart, dayEnd);
        
        long totalCount = appointments.size();
        long completedCount = appointments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long cancelledCount = appointments.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();
        long bookedCount = appointments.stream().filter(a -> "BOOKED".equals(a.getStatus())).count();
        long pendingCount = appointments.stream().filter(a -> "BOOKED".equals(a.getStatus()) || "RESCHEDULED".equals(a.getStatus())).count();

        return new DailyAppointmentStatusDto(date, totalCount, completedCount, cancelledCount, pendingCount, bookedCount);
    }

    @Override
    @Transactional
    public int markPreviousDayAppointmentsAsCompleted() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        OffsetDateTime dayStart = OffsetDateTime.of(yesterday, java.time.LocalTime.MIDNIGHT, ZoneOffset.UTC);
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> pendingAppointments = appointmentRepository.findPendingAppointmentsBetween(dayStart, dayEnd);
        
        int updatedCount = 0;
        for (Appointment appointment : pendingAppointments) {
            if ("BOOKED".equals(appointment.getStatus()) || "RESCHEDULED".equals(appointment.getStatus())) {
                appointment.setStatus("COMPLETED");
                appointment.setUpdatedAt(OffsetDateTime.now());
                appointmentRepository.save(appointment);
                updatedCount++;
            }
        }

        System.out.println("[DEBUG] Marked " + updatedCount + " appointments as COMPLETED for date: " + yesterday);
        return updatedCount;
    }

    @Override
    public DailyAppointmentStatusDto getDoctorCurrentDateAppointmentStatus(Long doctorId) {
        return getDoctorAppointmentStatusForDate(doctorId, LocalDate.now());
    }

    @Override
    public DailyAppointmentStatusDto getDoctorAppointmentStatusForDate(Long doctorId, LocalDate date) {
        OffsetDateTime dayStart = OffsetDateTime.of(date, java.time.LocalTime.MIDNIGHT, ZoneOffset.UTC);
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetweenOrderByAppointmentTime(doctorId, dayStart, dayEnd);
        
        long totalCount = appointments.size();
        long completedCount = appointments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long cancelledCount = appointments.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();
        long bookedCount = appointments.stream().filter(a -> "BOOKED".equals(a.getStatus())).count();
        long pendingCount = appointments.stream().filter(a -> "BOOKED".equals(a.getStatus()) || "RESCHEDULED".equals(a.getStatus())).count();

        return new DailyAppointmentStatusDto(date, totalCount, completedCount, cancelledCount, pendingCount, bookedCount);
    }
}
