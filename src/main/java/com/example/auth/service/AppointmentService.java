package com.example.auth.service;

import com.example.auth.dto.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface AppointmentService {

    List<AppointmentDto> getDoctorAppointments(Long doctorId, OffsetDateTime from, OffsetDateTime to);

    List<AppointmentDto> getDoctorHistory(Long doctorId, OffsetDateTime from, OffsetDateTime to);

    List<AppointmentDto> rescheduleDoctorAppointments(Long doctorId, RescheduleRequest req);

    List<Long> cancelDoctorDay(Long doctorId, CancelDayRequest req);

    AppointmentDto bookAppointment(Long userId, BookAppointmentRequest req);
    
    BookAppointmentResponse bookAppointmentEnhanced(Long userId, BookAppointmentRequest req);

    AppointmentDto cancelAppointment(Long userId, Long appointmentId);

    AppointmentDto pushToEnd(Long userId, Long appointmentId, String reason);

    List<AppointmentDto> getUserAppointments(Long userId);

    // Enhanced methods for segregated appointments
    Map<String, List<AppointmentSegregatedDto>> getDoctorAppointmentsSegregated(Long doctorId, OffsetDateTime from, OffsetDateTime to);

    // Get only completed or cancelled appointments for history
    List<AppointmentDto> getDoctorCompletedHistory(Long doctorId, OffsetDateTime from, OffsetDateTime to);
}
