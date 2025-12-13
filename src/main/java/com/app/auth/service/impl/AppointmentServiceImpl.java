package com.app.auth.service.impl;

import com.app.auth.dto.*;
import com.app.auth.entity.Appointment;
import com.app.auth.entity.DoctorDetails;
import com.app.auth.entity.DoctorWorkplace;
import com.app.auth.repository.*;
import com.app.auth.service.AppointmentService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorDetailsRepository doctorRepo;
    private final DoctorWorkplaceRepository workplaceRepo;
    private final UserDetailsRepository userRepo;

    // keep future repo bean for compatibility but avoid using it at runtime
    // private final FutureTwoDayAppointmentRepository futureAppointmentRepo;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepo,
                                  /* FutureTwoDayAppointmentRepository futureAppointmentRepo, */
                                  DoctorDetailsRepository doctorRepo,
                                  DoctorWorkplaceRepository workplaceRepo,
                                  UserDetailsRepository userRepo) {
        this.appointmentRepo = appointmentRepo;
        // this.futureAppointmentRepo = futureAppointmentRepo;
        this.doctorRepo = doctorRepo;
        this.workplaceRepo = workplaceRepo;
        this.userRepo = userRepo;
    }

    private AppointmentDto toDto(Appointment a) {
        AppointmentDto d = new AppointmentDto();
        d.setId(a.getId());
        d.setDoctorId(a.getDoctorId());
        d.setUserId(a.getUserId());
        d.setAppointmentTime(a.getAppointmentTime());
        d.setDurationMinutes(a.getDurationMinutes());
        d.setQueuePosition(a.getQueuePosition());
        d.setStatus(a.getStatus());
        d.setNotes(a.getNotes());
        return d;
    }

    @Override
    public List<AppointmentDto> getDoctorAppointments(Long doctorId, OffsetDateTime from, OffsetDateTime to) {
        List<Appointment> list = appointmentRepo.findByDoctorIdAndAppointmentTimeBetweenOrderByAppointmentTime(doctorId, from, to);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDto> getDoctorHistory(Long doctorId, OffsetDateTime from, OffsetDateTime to) {
        List<Appointment> list = appointmentRepo.findByDoctorIdAndAppointmentTimeBetweenOrderByAppointmentTimeDesc(doctorId, from, to);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Reschedule: shift appointments at/after startFrom (or from now) by shiftMinutes.
     * Uses DB locking for doctor's day to avoid races.
     */
    @Override
    @Transactional
    public List<AppointmentDto> rescheduleDoctorAppointments(Long doctorId, RescheduleRequest req) {
        int shift = req.getShiftMinutes();
        int maxShift = Optional.ofNullable(req.getMaxShiftMinutes()).orElse(1440);
        if (Math.abs(shift) > maxShift) throw new IllegalArgumentException("shiftMinutes exceeds maxShiftMinutes");

        OffsetDateTime startFrom = Optional.ofNullable(req.getStartFrom()).orElse(OffsetDateTime.now(ZoneOffset.UTC));

        // we'll reschedule all appointments from startFrom up to startFrom + 7 days (or configurable)
        OffsetDateTime from = startFrom;
        OffsetDateTime to = startFrom.plusDays(7); // limit window to avoid accidental global shifts

        // lock appointments for the window
        List<Appointment> locked = appointmentRepo.lockAppointmentsForDoctorBetween(doctorId, from, to);

        if (locked.isEmpty()) return Collections.emptyList();

        List<AppointmentDto> updated = new ArrayList<>();
        for (Appointment a : locked) {
            OffsetDateTime old = a.getAppointmentTime();
            OffsetDateTime nu = old.plusMinutes(shift);
            a.setAppointmentTime(nu);
            a.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            a.setStatus("RESCHEDULED");
            appointmentRepo.save(a);
            AppointmentDto dto = toDto(a);
            updated.add(dto);
        }
        return updated;
    }

    /**
     * Cancel all appointments for a doctor's date
     */
    @Override
    @Transactional
    public List<Long> cancelDoctorDay(Long doctorId, CancelDayRequest req) {
        LocalDate date = LocalDate.parse(req.getDate());
        OffsetDateTime dayStart = OffsetDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> booked = appointmentRepo.findByDoctorIdAndAppointmentTimeBetweenAndStatusOrderByAppointmentTime(doctorId, dayStart, dayEnd, "BOOKED");
        List<Long> ids = new ArrayList<>();
        for (Appointment a : booked) {
            a.setStatus("CANCELLED");
            a.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            appointmentRepo.save(a);
            ids.add(a.getId());
            // TODO: enqueue notification to user
        }
        return ids;
    }

    /**
     * Book an appointment for user.
     * - If requestedTime provided and free -> book at requestedTime
     * - If requestedTime provided but collides -> append to end of day
     * - If no requestedTime -> append to end of day
     *
     * concurrency safe: we lock doctor's appointments for that day while computing the slot (pessimistic).
     */
    @Override
    @Transactional
    public AppointmentDto bookAppointment(Long userId, BookAppointmentRequest req) {
        Long doctorId = req.getDoctorId();
        if (!doctorRepo.existsById(doctorId)) throw new IllegalArgumentException("Doctor not found");
        if (!userRepo.existsById(userId)) throw new IllegalArgumentException("User not found");

        OffsetDateTime requested = req.getRequestedTime();
        int duration = 30; // Default duration in minutes

        // determine day window (based on requested time or today)
        OffsetDateTime dayStart;
        if (requested != null) dayStart = OffsetDateTime.of(requested.toLocalDate(), LocalTime.MIDNIGHT, ZoneOffset.UTC);
        else dayStart = OffsetDateTime.of(OffsetDateTime.now(ZoneOffset.UTC).toLocalDate(), LocalTime.MIDNIGHT, ZoneOffset.UTC);

        OffsetDateTime dayEnd = dayStart.plusDays(1);

        // lock appointments for the day
        List<Appointment> locked = appointmentRepo.lockAppointmentsForDoctorBetween(doctorId, dayStart, dayEnd);

        // find last end time and last queue position
        OffsetDateTime lastEnd = dayStart;
        int lastQueue = 0;
        for (Appointment a : locked) {
            OffsetDateTime aEnd = a.getAppointmentTime().plusMinutes(a.getDurationMinutes());
            if (aEnd.isAfter(lastEnd)) lastEnd = aEnd;
            if (a.getQueuePosition() != null && a.getQueuePosition() > lastQueue) lastQueue = a.getQueuePosition();
        }

        OffsetDateTime scheduled;
        int queuePosition;

        if (requested != null) {
            // check for overlap with any existing appointment
            boolean overlaps = locked.stream().anyMatch(a -> {
                OffsetDateTime s = a.getAppointmentTime();
                OffsetDateTime e = s.plusMinutes(a.getDurationMinutes());
                OffsetDateTime rStart = requested;
                OffsetDateTime rEnd = requested.plusMinutes(duration);
                return !(rEnd.isEqual(s) || rEnd.isBefore(s) || rStart.isAfter(e) || rStart.isEqual(e));
            });

            if (!overlaps) {
                scheduled = requested;
                // queue position = count of appointments strictly before +1
                long before = locked.stream().filter(a -> a.getAppointmentTime().isBefore(scheduled)).count();
                queuePosition = (int) before + 1;
            } else {
                // append to end
                scheduled = lastEnd;
                queuePosition = lastQueue + 1;
            }
        } else {
            // no requested time: append to end
            scheduled = lastEnd;
            queuePosition = lastQueue + 1;
        }

        Appointment a = new Appointment();
        a.setDoctorId(doctorId);
        a.setUserId(userId);
        a.setAppointmentTime(scheduled);
        a.setDurationMinutes(duration);
        a.setQueuePosition(queuePosition);
        a.setNotes(req.getNotes());
        a.setStatus("BOOKED");
        a.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        a.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Appointment saved = appointmentRepo.save(a);
        return toDto(saved);
    }

    @Override
    @Transactional
    public BookAppointmentResponse bookAppointmentEnhanced(Long userId, BookAppointmentRequest req) {
        // Validate required fields
        if (req.getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor ID is required");
        }
        if (req.getWorkplaceId() == null) {
            throw new IllegalArgumentException("Workspace ID is required");
        }
        if (req.getSlot() == null || req.getSlot().trim().isEmpty()) {
            throw new IllegalArgumentException("Slot is required");
        }
        if (req.getRequestedTime() == null) {
            throw new IllegalArgumentException("Requested time is required");
        }

        // Validate doctor and user exist
        DoctorDetails doctor = doctorRepo.findById(req.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        DoctorWorkplace workspace = workplaceRepo.findById(req.getWorkplaceId())
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        
        if (!userRepo.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        // Determine appointment date from requested time
        LocalDate appointmentDate = req.getRequestedTime().toLocalDate();
        LocalDate today = LocalDate.now();
        String appointmentDateStr = appointmentDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Calculate duration based on slot (default 30 minutes if can't parse)
        int durationMinutes = calculateDurationFromSlot(req.getSlot());

        if (appointmentDate.equals(today)) {
            // Book in current appointments table
            Appointment appointment = new Appointment();
            appointment.setUserId(userId);
            appointment.setDoctorId(req.getDoctorId());
            appointment.setDoctorName(doctor.getFullName());
            appointment.setWorkplaceId(req.getWorkplaceId());
            appointment.setWorkplaceName(workspace.getWorkplaceName());
            appointment.setWorkplaceType(workspace.getWorkplaceType());
            appointment.setWorkplaceAddress(workspace.getAddress());
            appointment.setAppointmentDate(appointmentDateStr);
            appointment.setSlot(req.getSlot());
            appointment.setAppointmentTime(req.getRequestedTime());
            appointment.setDurationMinutes(durationMinutes);
            appointment.setStatus("BOOKED");
            appointment.setNotes(req.getNotes());
            appointment.setDoctorSpecialization(doctor.getSpecialization());
            appointment.setQueuePosition(getNextQueuePosition(req.getDoctorId(), req.getWorkplaceId(), appointmentDateStr));
            appointment.setCreatedAt(OffsetDateTime.now());
            appointment.setUpdatedAt(OffsetDateTime.now());

            appointmentRepo.save(appointment);
            
        } else {
            // Book in future appointments: persist into appointments table instead of separate future table
            Appointment futureAppt = new Appointment();
            futureAppt.setUserId(userId);
            futureAppt.setDoctorId(req.getDoctorId());
            futureAppt.setDoctorName(doctor.getFullName());
            futureAppt.setDoctorSpecialization(doctor.getSpecialization());
            futureAppt.setWorkplaceId(req.getWorkplaceId());
            futureAppt.setWorkplaceName(workspace.getWorkplaceName());
            futureAppt.setWorkplaceType(workspace.getWorkplaceType());
            futureAppt.setWorkplaceAddress(workspace.getAddress());
            futureAppt.setAppointmentDate(appointmentDateStr);
            futureAppt.setSlot(req.getSlot());
            futureAppt.setAppointmentTime(req.getRequestedTime());
            futureAppt.setDurationMinutes(durationMinutes);
            futureAppt.setStatus("BOOKED");
            futureAppt.setNotes(req.getNotes());
            futureAppt.setQueuePosition(getNextQueuePositionFuture(req.getDoctorId(), req.getWorkplaceId(), appointmentDateStr));
            futureAppt.setCreatedAt(OffsetDateTime.now());

            appointmentRepo.save(futureAppt);
        }

        // Return simple success response
        String workplaceName = workspace.getWorkplaceName();
        return new BookAppointmentResponse("Appointment booked successfully", workplaceName, req.getSlot());
    }

    private int calculateDurationFromSlot(String slot) {
        try {
            // Parse slot like "02:30PM - 02:50PM"
            String[] parts = slot.split(" - ");
            if (parts.length == 2) {
                String startStr = parts[0].trim();
                String endStr = parts[1].trim();
                
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("h:mma");
                LocalTime startTime = LocalTime.parse(startStr, formatter);
                LocalTime endTime = LocalTime.parse(endStr, formatter);
                
                return (int) java.time.Duration.between(startTime, endTime).toMinutes();
            }
        } catch (Exception e) {
            // If parsing fails, return default
        }
        return 30; // Default 30 minutes
    }

    private int getNextQueuePosition(Long doctorId, Long workspaceId, String appointmentDate) {
        // Count existing appointments for the doctor on this date at this workspace
        // Using a custom query since Appointment uses workplace relationship
        long count = appointmentRepo.countByDoctorIdAndAppointmentDate(doctorId, appointmentDate);
        return (int) count + 1;
    }

    private int getNextQueuePositionFuture(Long doctorId, Long workspaceId, String appointmentDate) {
        // Use appointments table counting for future dates as well
        long count = appointmentRepo.countByDoctorIdAndWorkplaceIdAndAppointmentDate(doctorId, workspaceId, appointmentDate);
        return (int) count + 1;
    }

    @Override
    @Transactional
    public AppointmentDto cancelAppointment(Long userId, Long appointmentId) {
        Optional<Appointment> opt = appointmentRepo.lockById(appointmentId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Appointment not found");
        Appointment ap = opt.get();
        if (!ap.getUserId().equals(userId)) throw new IllegalArgumentException("Not your appointment");
        if (!"BOOKED".equals(ap.getStatus())) throw new IllegalArgumentException("Cannot cancel, status=" + ap.getStatus());
        ap.setStatus("CANCELLED");
        ap.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        appointmentRepo.save(ap);
        return toDto(ap);
    }

    @Override
    @Transactional
    public AppointmentDto pushToEnd(Long userId, Long appointmentId, String reason) {
        Optional<Appointment> opt = appointmentRepo.lockById(appointmentId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Appointment not found");
        Appointment ap = opt.get();
        if (!ap.getUserId().equals(userId)) throw new IllegalArgumentException("Not your appointment");
        if (!"BOOKED".equals(ap.getStatus())) throw new IllegalArgumentException("Cannot push, status=" + ap.getStatus());

        // compute day boundaries for this appointment
        LocalDate day = ap.getAppointmentTime().toLocalDate();
        OffsetDateTime dayStart = OffsetDateTime.of(day, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        // lock all appointments for the day
        List<Appointment> locked = appointmentRepo.lockAppointmentsForDoctorBetween(ap.getDoctorId(), dayStart, dayEnd);

        // compute last end time and last queue position
        OffsetDateTime lastEnd = dayStart;
        int lastQueue = 0;
        for (Appointment a : locked) {
            OffsetDateTime e = a.getAppointmentTime().plusMinutes(a.getDurationMinutes());
            if (e.isAfter(lastEnd)) lastEnd = e;
            if (a.getQueuePosition() != null && a.getQueuePosition() > lastQueue) lastQueue = a.getQueuePosition();
        }

        ap.setAppointmentTime(lastEnd);
        ap.setQueuePosition(lastQueue + 1);
        ap.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        ap.setNotes(Optional.ofNullable(ap.getNotes()).orElse("") + " | pushed: " + reason);
        appointmentRepo.save(ap);

        return toDto(ap);
    }

    @Override
    public List<AppointmentDto> getUserAppointments(Long userId) {
        List<Appointment> list = appointmentRepo.findByUserIdOrderByAppointmentTimeDesc(userId);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<AppointmentSegregatedDto>> getDoctorAppointmentsSegregated(Long doctorId, OffsetDateTime from, OffsetDateTime to) {
        Map<String, List<AppointmentSegregatedDto>> result = new HashMap<>();
        
        // Get clinic appointments
        List<Appointment> clinicAppointments = appointmentRepo.findByDoctorIdAndWorkplaceTypeAndAppointmentTimeBetween(doctorId, "CLINIC", from, to);
        List<AppointmentSegregatedDto> clinicSegregated = segregateByWorkplace(clinicAppointments, "CLINIC");
        result.put("clinics", clinicSegregated);
        
        // Get hospital appointments
        List<Appointment> hospitalAppointments = appointmentRepo.findByDoctorIdAndWorkplaceTypeAndAppointmentTimeBetween(doctorId, "HOSPITAL", from, to);
        List<AppointmentSegregatedDto> hospitalSegregated = segregateByWorkplace(hospitalAppointments, "HOSPITAL");
        result.put("hospitals", hospitalSegregated);
        
        return result;
    }

    @Override
    public List<AppointmentDto> getDoctorCompletedHistory(Long doctorId, OffsetDateTime from, OffsetDateTime to) {
        List<Appointment> list = appointmentRepo.findHistoryByDoctorIdAndAppointmentTimeBetween(doctorId, from, to);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    private List<AppointmentSegregatedDto> segregateByWorkplace(List<Appointment> appointments, String workplaceType) {
        Map<Long, AppointmentSegregatedDto> workplaceMap = new HashMap<>();
        
        for (Appointment appointment : appointments) {
            if (appointment.getWorkplaceId() != null) {
                Long workplaceId = appointment.getWorkplaceId();
                
                AppointmentSegregatedDto segregated = workplaceMap.computeIfAbsent(workplaceId, k -> {
                    AppointmentSegregatedDto dto = new AppointmentSegregatedDto();
                    dto.setWorkplaceId(workplaceId);
                    dto.setWorkplaceName(appointment.getWorkplaceName());
                    dto.setWorkplaceType(appointment.getWorkplaceType());
                    dto.setAppointments(new ArrayList<>());
                    return dto;
                });
                
                segregated.getAppointments().add(toDto(appointment));
            }
        }
        
        return new ArrayList<>(workplaceMap.values());
    }
}
