package com.app.auth.service.impl;

import com.app.auth.dto.*;
import com.app.auth.entity.*;
import com.app.auth.repository.*;
import com.app.auth.service.BlockedSlotService;
import com.app.auth.service.EnhancedAppointmentService;
import com.app.auth.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;

@Service
public class EnhancedAppointmentServiceImpl implements EnhancedAppointmentService {

    private final AppointmentRepository appointmentRepository;
    // FutureTwoDayAppointmentRepository kept for safety but not used anymore
    // private final FutureTwoDayAppointmentRepository futureAppointmentRepository;
    private final PastAppointmentRepository pastAppointmentRepository;
    private final DoctorDetailsRepository doctorRepository;
    private final DoctorWorkplaceRepository workplaceRepository;
    private final UserDetailsRepository userRepository;
    private final com.app.auth.repository.FamilyMemberRepository familyMemberRepository;
    private final NotificationService notificationService;
    private final BlockedSlotService blockedSlotService;

    public EnhancedAppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            /* FutureTwoDayAppointmentRepository futureAppointmentRepository, */
            PastAppointmentRepository pastAppointmentRepository,
            DoctorDetailsRepository doctorRepository,
            DoctorWorkplaceRepository workplaceRepository,
            UserDetailsRepository userRepository,
            com.app.auth.repository.FamilyMemberRepository familyMemberRepository,
            NotificationService notificationService,
            BlockedSlotService blockedSlotService) {
        this.appointmentRepository = appointmentRepository;
    // this.futureAppointmentRepository = futureAppointmentRepository;
        this.pastAppointmentRepository = pastAppointmentRepository;
        this.doctorRepository = doctorRepository;
        this.workplaceRepository = workplaceRepository;
        this.userRepository = userRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.notificationService = notificationService;
        this.blockedSlotService = blockedSlotService;
    }

    @Override
    public UserAppointmentsResponseDto getUserAppointments(Long userId) {
        Map<String, List<UserAppointmentDto>> appointmentsByDate = new LinkedHashMap<>();
        
        // Get today's date for filtering
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Get all appointments from appointments table for this user
        List<Appointment> allCurrentAppointments = appointmentRepository.findByUserIdOrderByAppointmentTimeDesc(userId);
        
        // Filter to only include appointments from today onwards
        List<UserAppointmentDto> currentAppointmentDtos = allCurrentAppointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().compareTo(today) >= 0) // Only today and future
//                .filter(appointment -> "BOOKED".equals(appointment.getStatus())) // Only booked appointments
                .map(this::convertToUserAppointmentDto)
                .collect(Collectors.toList());
        
        // Group current appointments by date
        Map<String, List<UserAppointmentDto>> currentAppointmentsByDate = currentAppointmentDtos.stream()
                .collect(Collectors.groupingBy(
                    UserAppointmentDto::getAppointmentDate,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        
        appointmentsByDate.putAll(currentAppointmentsByDate);
        
    // All appointments (current + future) are now stored in appointments table
    int totalAppointments = currentAppointmentDtos.size();
        
        return new UserAppointmentsResponseDto(appointmentsByDate, totalAppointments);
    }

    @Override
    public AvailableSlotsResponseDto getAvailableSlots(Long doctorId, Long workplaceId, String date) {
        Map<String, List<String>> slotsByDate = new LinkedHashMap<>();
        
        // Get doctor and workplace details
        Optional<DoctorDetails> doctorOpt = doctorRepository.findById(doctorId);
        Optional<DoctorWorkplace> workplaceOpt = workplaceRepository.findById(workplaceId);
        
        if (!doctorOpt.isPresent() || !workplaceOpt.isPresent()) {
            throw new IllegalArgumentException("Doctor or workplace not found");
        }
        
        DoctorDetails doctor = doctorOpt.get();
        DoctorWorkplace workplace = workplaceOpt.get();
        
        AvailableSlotsResponseDto response;
        
        if (date != null && !date.trim().isEmpty()) {
            // Generate slots for specific date
            try {
                LocalDate targetDate = LocalDate.parse(date);
                String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                List<String> availableSlots = generateAvailableSlots(doctor, workplace, dateStr);
                // Always include the date in response, even if no slots available
                slotsByDate.put(dateStr, availableSlots);
                
                response = new AvailableSlotsResponseDto(slotsByDate, doctorId, workplaceId, workplace.getWorkplaceName(), doctor.getFullName());
                
                // Check for blocked slots and add to response
                addBlockedSlotsInfo(response, doctorId, workplaceId, targetDate, targetDate);
                
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd format");
            }
        } else {
            // Generate slots for current day + next 2 days (3 days total) - existing behavior
            LocalDate currentDate = LocalDate.now();
            LocalDate endDate = currentDate.plusDays(2);
            
            for (int i = 0; i < 3; i++) {
                LocalDate targetDate = currentDate.plusDays(i);
                String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                List<String> availableSlots = generateAvailableSlots(doctor, workplace, dateStr);
                // Always include the date in response, even if no slots available
                slotsByDate.put(dateStr, availableSlots);
            }
            
            response = new AvailableSlotsResponseDto(slotsByDate, doctorId, workplaceId, workplace.getWorkplaceName(), doctor.getFullName());
            
            // Check for blocked slots and add to response
            addBlockedSlotsInfo(response, doctorId, workplaceId, currentDate, endDate);
        }
        
        return response;
    }
    
    /**
     * Add blocked slots information to response and filter out blocked time slots
     */
    private void addBlockedSlotsInfo(AvailableSlotsResponseDto response, Long doctorId, Long workplaceId, 
                                     LocalDate fromDate, LocalDate toDate) {
        // Get all blocked slots for the date range
        List<BlockedSlotDto> blockedSlots = blockedSlotService.getBlockedSlotsForDateRange(
            doctorId, workplaceId, fromDate, toDate);
        
        for (BlockedSlotDto blocked : blockedSlots) {
            String dateStr = blocked.getBlockDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            if (blocked.getIsFullDay()) {
                // Full day is blocked - clear all slots for this date and add blocked info
                response.getSlotsByDate().put(dateStr, new ArrayList<>()); // Empty slots
                response.addBlockedDate(dateStr, new AvailableSlotsResponseDto.BlockedDateInfo(
                    true,
                    true,
                    blocked.getReason() != null ? blocked.getReason() : "Doctor unavailable for this day",
                    null,
                    null
                ));
            } else {
                // Partial block - filter out slots that fall within blocked time
                List<String> slots = response.getSlotsByDate().get(dateStr);
                if (slots != null && blocked.getStartTime() != null && blocked.getEndTime() != null) {
                    List<String> filteredSlots = filterBlockedTimeSlots(slots, blocked.getStartTime(), blocked.getEndTime());
                    response.getSlotsByDate().put(dateStr, filteredSlots);
                    
                    // Add partial block info
                    response.addBlockedDate(dateStr, new AvailableSlotsResponseDto.BlockedDateInfo(
                        true,
                        false,
                        blocked.getReason() != null ? blocked.getReason() : "Doctor unavailable during this time",
                        blocked.getStartTime().format(DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH)),
                        blocked.getEndTime().format(DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH))
                    ));
                }
            }
        }
    }
    
    /**
     * Filter out slots that fall within a blocked time range
     */
    private List<String> filterBlockedTimeSlots(List<String> slots, LocalTime blockStart, LocalTime blockEnd) {
        List<String> filtered = new ArrayList<>();
        
        for (String slot : slots) {
            try {
                // Parse slot time (e.g., "9:00AM - 9:30AM")
                String[] parts = slot.split(" - ");
                if (parts.length == 2) {
                    LocalTime slotStart = parseTime(parts[0].trim());
                    LocalTime slotEnd = parseTime(parts[1].trim());
                    
                    // Check if slot overlaps with blocked time
                    boolean overlaps = !(slotEnd.isBefore(blockStart) || slotEnd.equals(blockStart) ||
                                        slotStart.isAfter(blockEnd) || slotStart.equals(blockEnd));
                    
                    if (!overlaps) {
                        filtered.add(slot);
                    }
                } else {
                    filtered.add(slot); // Keep slot if format is unexpected
                }
            } catch (Exception e) {
                filtered.add(slot); // Keep slot if parsing fails
            }
        }
        
        return filtered;
    }
    
    /**
     * Parse time string like "9:00AM" or "2:30PM" to LocalTime
     */
    private LocalTime parseTime(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
        return LocalTime.parse(timeStr.toUpperCase(), formatter);
    }

    private List<String> generateAvailableSlots(DoctorDetails doctor, DoctorWorkplace workplace, String date) {
        List<String> allSlots = new ArrayList<>();
        
        // Get checking duration (from workplace first, then doctor, then default)
        int durationMinutes = workplace.getCheckingDurationMinutes() != null ?
            workplace.getCheckingDurationMinutes() :
            (workplace.getCheckingDurationMinutes() != null ? workplace.getCheckingDurationMinutes() : 30);
        
        // Generate morning slots
        if (workplace.getMorningStartTime() != null && workplace.getMorningEndTime() != null) {
            allSlots.addAll(generateSlotsForPeriod(workplace.getMorningStartTime(), workplace.getMorningEndTime(), durationMinutes));
        }
        
        // Generate evening slots
        if (workplace.getEveningStartTime() != null && workplace.getEveningEndTime() != null) {
            allSlots.addAll(generateSlotsForPeriod(workplace.getEveningStartTime(), workplace.getEveningEndTime(), durationMinutes));
        }
        
        // Remove booked slots
        Set<String> bookedSlots = getBookedSlots(doctor.getId(), workplace.getId(), date);
        allSlots.removeAll(bookedSlots);
        
        // If requested date is today, remove slots that already started (cannot book past slots)
        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (date != null && date.equals(todayStr)) {
            OffsetDateTime now = OffsetDateTime.now();
            List<String> filtered = new ArrayList<>();
            for (String slot : allSlots) {
                try {
                    OffsetDateTime slotStart = parseSlotToDateTime(slot, date);
                    if (!slotStart.isBefore(now)) { // keep slots that start at or after now
                        filtered.add(slot);
                    }
                } catch (Exception e) {
                    // If parsing fails, skip the slot
                }
            }
            allSlots = filtered;
        }

        return allSlots;
    }

    private List<String> generateSlotsForPeriod(LocalTime startTime, LocalTime endTime, int durationMinutes) {
        List<String> slots = new ArrayList<>();
        LocalTime current = startTime;
        
        while (current.plusMinutes(durationMinutes).isBefore(endTime) || current.plusMinutes(durationMinutes).equals(endTime)) {
            LocalTime slotEnd = current.plusMinutes(durationMinutes);
            String slot = formatSlot(current, slotEnd);
            slots.add(slot);
            current = slotEnd;
        }
        
        return slots;
    }

    private String formatSlot(LocalTime start, LocalTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
        return start.format(formatter) + " - " + end.format(formatter);
    }

    private Set<String> getBookedSlots(Long doctorId, Long workplaceId, String date) {
        Set<String> bookedSlots = new HashSet<>();
        
        // Check current day appointments
        List<Appointment> currentAppointments = appointmentRepository.findByDoctorIdAndWorkplaceIdAndAppointmentDate(doctorId, workplaceId, date);
        bookedSlots.addAll(currentAppointments.stream()
                .filter(a -> a.getSlot() != null && !a.getStatus().equals("CANCELLED"))
                .map(Appointment::getSlot)
                .collect(Collectors.toSet()));
        
    // Appointments table contains both current and future entries; no separate future table lookup needed
        
        return bookedSlots;
    }

    @Override
    @Transactional
    public UserAppointmentDto bookAppointment(BookAppointmentRequestDto request) {
        // Validate that slot is available
        Set<String> bookedSlots = getBookedSlots(request.getDoctorId(), request.getWorkplaceId(), request.getAppointmentDate());
        if (bookedSlots.contains(request.getSlot())) {
            throw new IllegalArgumentException("Slot is already booked");
        }
        
        // Get doctor and workplace details
        DoctorDetails doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        DoctorWorkplace workplace = workplaceRepository.findById(request.getWorkplaceId())
                .orElseThrow(() -> new IllegalArgumentException("Workplace not found"));
        
        LocalDate appointmentDate = LocalDate.parse(request.getAppointmentDate());
        LocalDate today = LocalDate.now();
        
        if (appointmentDate.equals(today)) {
            // Book in current appointments table
            return bookCurrentAppointment(request, doctor, workplace);
        } else {
            // Book in future appointments table
            return bookFutureAppointment(request, doctor, workplace);
        }
    }

    private UserAppointmentDto bookCurrentAppointment(BookAppointmentRequestDto request, DoctorDetails doctor, DoctorWorkplace workplace) {
        Appointment appointment = new Appointment();
        appointment.setUserId(request.getUserId());
        // If booking for a family member, record the member id and name
        appointment.setPatientMemberId(request.getFamilyMemberId());
        if (request.getFamilyMemberId() != null) {
            try {
                com.app.auth.entity.FamilyMember fm = familyMemberRepository.findById(request.getFamilyMemberId()).orElse(null);
                if (fm != null) appointment.setPatientName(fm.getName());
            } catch (Exception ignored) {}
        } else {
            // default to owner's name
            try {
                com.app.auth.entity.UserDetails user = userRepository.findById(request.getUserId()).orElse(null);
                if (user != null) appointment.setPatientName(user.getFullName());
            } catch (Exception ignored) {}
        }
        appointment.setDoctorId(request.getDoctorId());
        appointment.setWorkplaceId(request.getWorkplaceId());
        appointment.setWorkplaceName(workplace.getWorkplaceName());
        appointment.setWorkplaceType(workplace.getWorkplaceType());
        appointment.setWorkplaceAddress(workplace.getAddress());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setSlot(request.getSlot());
        appointment.setAppointmentTime(parseSlotToDateTime(request.getSlot(), request.getAppointmentDate()));
        appointment.setDurationMinutes(workplace.getCheckingDurationMinutes() != null ? workplace.getCheckingDurationMinutes() : 30);
        appointment.setStatus(request.getStatus());
        appointment.setNotes(request.getNotes());
        appointment.setDoctorName(doctor.getFullName());
        appointment.setDoctorSpecialization(doctor.getSpecialization());
        appointment.setQueuePosition(getNextQueuePosition(request.getDoctorId(), request.getWorkplaceId(), request.getAppointmentDate()));
        
        Appointment saved = appointmentRepository.save(appointment);
        
        // Send booking confirmation notification
        sendAppointmentNotification(request.getUserId(),
            "Appointment Confirmed",
            String.format("Your appointment with %s is confirmed for %s at %s.",
                doctor.getFullName(), request.getAppointmentDate(), request.getSlot()),
            "APPOINTMENT_BOOKED"
        );
        
        return convertToUserAppointmentDto(saved);
    }

    private UserAppointmentDto bookFutureAppointment(BookAppointmentRequestDto request, DoctorDetails doctor, DoctorWorkplace workplace) {
        // Save future appointment directly into appointments table (no separate future table)
        Appointment appointment = new Appointment();
        appointment.setUserId(request.getUserId());
        // If booking for a family member, record the member id and name
        appointment.setPatientMemberId(request.getFamilyMemberId());
        if (request.getFamilyMemberId() != null) {
            try {
                com.app.auth.entity.FamilyMember fm = familyMemberRepository.findById(request.getFamilyMemberId()).orElse(null);
                if (fm != null) appointment.setPatientName(fm.getName());
            } catch (Exception ignored) {}
        } else {
            try {
                com.app.auth.entity.UserDetails user = userRepository.findById(request.getUserId()).orElse(null);
                if (user != null) appointment.setPatientName(user.getFullName());
            } catch (Exception ignored) {}
        }
        appointment.setDoctorId(request.getDoctorId());
        appointment.setWorkplaceId(request.getWorkplaceId());
        appointment.setWorkplaceName(workplace.getWorkplaceName());
        appointment.setWorkplaceType(workplace.getWorkplaceType());
        appointment.setWorkplaceAddress(workplace.getAddress());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setSlot(request.getSlot());
        appointment.setAppointmentTime(parseSlotToDateTime(request.getSlot(), request.getAppointmentDate()));
        appointment.setDurationMinutes(workplace.getCheckingDurationMinutes() != null ? workplace.getCheckingDurationMinutes() : 30);
        appointment.setStatus(request.getStatus());
        appointment.setNotes(request.getNotes());
        appointment.setDoctorName(doctor.getFullName());
        appointment.setDoctorSpecialization(doctor.getSpecialization());
        appointment.setQueuePosition(getNextQueuePosition(request.getDoctorId(), request.getWorkplaceId(), request.getAppointmentDate()));

        Appointment saved = appointmentRepository.save(appointment);
        
        // Send booking confirmation notification
        sendAppointmentNotification(request.getUserId(),
            "Appointment Confirmed",
            String.format("Your appointment with %s is confirmed for %s at %s.",
                doctor.getFullName(), request.getAppointmentDate(), request.getSlot()),
            "APPOINTMENT_BOOKED"
        );
        
        return convertToUserAppointmentDto(saved);
    }

    private OffsetDateTime parseSlotToDateTime(String slot, String date) {
        try {
            // Parse slot like "9:30AM - 9:45AM" and combine with date
            String[] parts = slot.split(" - ");
            String startTimeStr = parts[0].trim();
            
            // Try multiple formatters to handle different time formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("h:mma", Locale.US)
            };
            
            LocalTime startTime = null;
            for (DateTimeFormatter formatter : formatters) {
                try {
                    startTime = LocalTime.parse(startTimeStr, formatter);
                    break;
                } catch (Exception e) {
                    // Try next formatter
                    continue;
                }
            }
            
            if (startTime == null) {
                throw new IllegalArgumentException("Unable to parse time slot: " + startTimeStr);
            }
            
            LocalDate appointmentDate = LocalDate.parse(date);
            return appointmentDate.atTime(startTime).atOffset(OffsetDateTime.now().getOffset());
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time slot format: " + slot + ". Expected format: '9:30AM - 10:00AM'", e);
        }
    }

    private int getNextQueuePosition(Long doctorId, Long workplaceId, String date) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndWorkplaceIdAndAppointmentDate(doctorId, workplaceId, date);
        return appointments.size() + 1;
    }

    private int getNextQueuePositionFuture(Long doctorId, Long workplaceId, String date) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndWorkplaceIdAndAppointmentDate(doctorId, workplaceId, date);
        return appointments.size() + 1;
    }

    @Override
    @Transactional
    public String cancelAppointment(Long appointmentId) {
        // Try to find in current appointments
        Optional<Appointment> currentAppointment = appointmentRepository.findById(appointmentId);
        if (currentAppointment.isPresent()) {
            Appointment appointment = currentAppointment.get();
            appointment.setStatus("CANCELLED");
            appointmentRepository.save(appointment);
            
            // Send automatic push notification to user
            String notificationTitle = "Appointment Cancelled";
            String notificationBody = String.format("Your appointment with %s on %s at %s has been cancelled.",
                appointment.getDoctorName(),
                appointment.getAppointmentDate(),
                appointment.getSlot()
            );
            
            sendAppointmentNotification(appointment.getUserId(), 
                notificationTitle, 
                notificationBody, 
                "APPOINTMENT_CANCELLED"
            );
            
            return "Appointment cancelled successfully";
        }
        
        // Appointment not found in appointments table
        throw new IllegalArgumentException("Appointment not found");
    }

    @Override
    @Transactional
    public String rescheduleUserAppointment(UserRescheduleRequestDto request) {
        Long appointmentId = request.getAppointmentId();
        
        // Validate new appointment date
        try {
            LocalDate newDate = LocalDate.parse(request.getNewAppointmentDate());
            LocalDate today = LocalDate.now();
            
            if (newDate.isBefore(today)) {
                throw new IllegalArgumentException("Cannot reschedule to a past date");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd format");
        }
        
        // Try to find in current appointments table
        Optional<Appointment> currentAppointment = appointmentRepository.findById(appointmentId);
        if (currentAppointment.isPresent()) {
            Appointment appointment = currentAppointment.get();
            
            // Mark original appointment as rescheduled
            appointment.setStatus("RESCHEDULED");
            appointment.setNotes("Original appointment rescheduled: " + request.getReason());
            appointmentRepository.save(appointment);
            
            // Create new appointment with the new date and time - always in appointments table
            createRescheduledAppointment(appointment, request);
            
            // Send automatic push notification to user
            String notificationTitle = "Appointment Rescheduled";
            String notificationBody = String.format("Your appointment with %s has been rescheduled to %s at %s.",
                appointment.getDoctorName(),
                request.getNewAppointmentDate(),
                request.getNewTimeSlot()
            );
            
            sendAppointmentNotification(appointment.getUserId(), 
                notificationTitle, 
                notificationBody, 
                "APPOINTMENT_RESCHEDULED"
            );
            
            return "Appointment rescheduled successfully";
        }
        
        // No fallback to separate future table — if not in appointments table, treat as not found
        
        throw new IllegalArgumentException("Appointment not found");
    }

    @Override
    @Transactional
    public void moveAppointmentsToCurrentDay() {
        // No-op: appointments table now contains both current and future appointments; no moving required
        System.out.println("[SCHEDULER] moveAppointmentsToCurrentDay is a no-op when all appointments are stored in the appointments table");
    }

    @Override
    @Transactional
    public void movePastAppointments() {
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Find all appointments from yesterday and before that are still in the appointments table
        List<Appointment> appointmentsToMove = appointmentRepository.findByAppointmentDate(yesterday);
        
        // Also get any appointments that are older than yesterday
        LocalDate currentDate = LocalDate.now();
        List<Appointment> allCurrentAppointments = appointmentRepository.findAll();
        List<Appointment> olderAppointments = allCurrentAppointments.stream()
                .filter(a -> {
                    try {
                        LocalDate appointmentDate = LocalDate.parse(a.getAppointmentDate());
                        return appointmentDate.isBefore(currentDate);
                    } catch (Exception e) {
                        System.err.println("[SCHEDULER] Error parsing date: " + a.getAppointmentDate());
                        return false;
                    }
                })
                .collect(Collectors.toList());
        
        // Combine both lists and remove duplicates
        Set<Long> processedIds = new HashSet<>();
        List<Appointment> allAppointmentsToMove = new ArrayList<>();
        
        for (Appointment apt : appointmentsToMove) {
            if (!processedIds.contains(apt.getId())) {
                allAppointmentsToMove.add(apt);
                processedIds.add(apt.getId());
            }
        }
        
        for (Appointment apt : olderAppointments) {
            if (!processedIds.contains(apt.getId())) {
                allAppointmentsToMove.add(apt);
                processedIds.add(apt.getId());
            }
        }
        
        System.out.println("[SCHEDULER] Found " + allAppointmentsToMove.size() + " past appointments to move");
        
        for (Appointment appointment : allAppointmentsToMove) {
            // Create past appointment
            PastAppointment pastAppointment = new PastAppointment();
            pastAppointment.setUserId(appointment.getUserId());
            pastAppointment.setDoctorId(appointment.getDoctorId());
            pastAppointment.setWorkplaceId(appointment.getWorkplaceId());
            pastAppointment.setWorkplaceName(appointment.getWorkplaceName());
            pastAppointment.setWorkplaceType(appointment.getWorkplaceType());
            pastAppointment.setWorkplaceAddress(appointment.getWorkplaceAddress());
            pastAppointment.setAppointmentDate(appointment.getAppointmentDate());
            pastAppointment.setSlot(appointment.getSlot());
            pastAppointment.setAppointmentTime(appointment.getAppointmentTime());
            pastAppointment.setDurationMinutes(appointment.getDurationMinutes());
            pastAppointment.setQueuePosition(appointment.getQueuePosition());
            pastAppointment.setStatus(appointment.getStatus());
            pastAppointment.setNotes(appointment.getNotes());
            pastAppointment.setDoctorName(appointment.getDoctorName());
            pastAppointment.setDoctorSpecialization(appointment.getDoctorSpecialization());
            pastAppointment.setCreatedAt(appointment.getCreatedAt());
            
            pastAppointmentRepository.save(pastAppointment);
            appointmentRepository.delete(appointment);
            
            System.out.println("[SCHEDULER] Moved past appointment ID " + appointment.getId() + " for date " + appointment.getAppointmentDate());
        }
        
        System.out.println("[SCHEDULER] Successfully moved " + allAppointmentsToMove.size() + " appointments to past table");
    }

    private UserAppointmentDto convertToUserAppointmentDto(Appointment appointment) {
        UserAppointmentDto dto = new UserAppointmentDto();
        dto.setId(appointment.getId());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setDoctorName(appointment.getDoctorName());
        dto.setDoctorSpecialization(appointment.getDoctorSpecialization());
        dto.setWorkplaceId(appointment.getWorkplaceId());
        dto.setWorkplaceName(appointment.getWorkplaceName());
        dto.setWorkplaceType(appointment.getWorkplaceType());
        dto.setWorkplaceAddress(appointment.getWorkplaceAddress());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setSlot(appointment.getSlot());
        dto.setDurationMinutes(appointment.getDurationMinutes());
        dto.setQueuePosition(appointment.getQueuePosition());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        dto.setPatientMemberId(appointment.getPatientMemberId());
        dto.setPatientName(appointment.getPatientName());
        
        return dto;
    }

    // convertFutureToUserAppointmentDto removed - use convertToUserAppointmentDto(Appointment) instead
    
    // ==================== NEW DOCTOR MANAGEMENT APIS ====================
    
    @Override
    public List<DoctorAppointmentViewDto> getDoctorAppointmentsWithUserDetails(Long doctorId, String appointmentDate) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctorId, appointmentDate);
        
        return appointments.stream()
                .map(this::convertToDoctorAppointmentViewDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public String bulkUpdateAppointmentStatus(Long doctorId, String appointmentDate, BulkAppointmentStatusUpdateDto request) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDateAndUserIdIn(
                doctorId, appointmentDate, request.getUserIds());
        
        if (appointments.isEmpty()) {
            return "No appointments found for the specified users and date";
        }
        
        int updatedCount = 0;
        String status = request.getStatus();
        
        for (Appointment appointment : appointments) {
            switch (status) {
                case "COMPLETED":
                    appointment.setStatus("COMPLETED");
                    appointment.setNotes(request.getNotes());
                    appointmentRepository.save(appointment);
                    
                    // Move to past appointments for historical tracking
                    moveToPastAppointment(appointment);
                    
                    // Send notification to user
                    sendAppointmentNotification(appointment.getUserId(),
                        "Appointment Completed",
                        String.format("Your appointment with %s on %s has been marked as completed.",
                            appointment.getDoctorName(), appointment.getAppointmentDate()),
                        "APPOINTMENT_COMPLETED"
                    );
                    
                    updatedCount++;
                    break;
                    
                case "CANCELLED":
                    appointment.setStatus("CANCELLED");
                    appointment.setNotes(request.getNotes());
                    appointmentRepository.save(appointment);
                    
                    // Send notification to user
                    sendAppointmentNotification(appointment.getUserId(),
                        "Appointment Cancelled",
                        String.format("Your appointment with %s on %s at %s has been cancelled by the doctor.",
                            appointment.getDoctorName(), appointment.getAppointmentDate(), appointment.getSlot()),
                        "APPOINTMENT_CANCELLED_BY_DOCTOR"
                    );
                    
                    updatedCount++;
                    break;
                    
                case "RESCHEDULED":
                    if (request.getNewAppointmentDate() != null && request.getNewTimeSlot() != null) {
                        // Cancel current appointment
                        appointment.setStatus("RESCHEDULED");
                        appointment.setNotes(request.getNotes());
                        appointmentRepository.save(appointment);
                        
                        // Create new appointment
                        createRescheduledAppointment(appointment, request);
                        
                        // Send notification to user
                        sendAppointmentNotification(appointment.getUserId(),
                            "Appointment Rescheduled",
                            String.format("Your appointment with %s has been rescheduled to %s at %s.",
                                appointment.getDoctorName(), request.getNewAppointmentDate(), request.getNewTimeSlot()),
                            "APPOINTMENT_RESCHEDULED_BY_DOCTOR"
                        );
                        
                        updatedCount++;
                    } else {
                        throw new RuntimeException("New appointment date and time slot are required for rescheduling");
                    }
                    break;
                    
                default:
                    throw new RuntimeException("Invalid status: " + status);
            }
        }
        
        return String.format("Successfully updated %d appointments to %s status", updatedCount, status);
    }
    
    private DoctorAppointmentViewDto convertToDoctorAppointmentViewDto(Appointment appointment) {
        DoctorAppointmentViewDto dto = new DoctorAppointmentViewDto();
        
        dto.setAppointmentId(appointment.getId());
        dto.setUserId(appointment.getUserId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setTimeSlot(appointment.getSlot());
        dto.setDurationMinutes(appointment.getDurationMinutes());
        dto.setQueuePosition(appointment.getQueuePosition());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        
        // Set workplace details from appointment entity directly
        dto.setWorkplaceId(appointment.getWorkplaceId());
        dto.setWorkplaceName(appointment.getWorkplaceName());
        dto.setWorkplaceType(appointment.getWorkplaceType());
        dto.setWorkplaceAddress(appointment.getWorkplaceAddress());
        
        // Set doctor details from appointment entity directly
        dto.setDoctorName(appointment.getDoctorName());
        dto.setDoctorSpecialization(appointment.getDoctorSpecialization());
        
        // Get user details
        Optional<UserDetails> userOpt = userRepository.findById(appointment.getUserId());
        if (userOpt.isPresent()) {
            UserDetails user = userOpt.get();
            dto.setUserName(user.getFullName());
            dto.setUserPhoneNumber(user.getMobileNumber());
            dto.setUserEmail(user.getEmail());
        }
        
        return dto;
    }
    
    private void moveToPastAppointment(Appointment appointment) {
        // This method already exists in the service, we can reuse the logic
        PastAppointment pastAppointment = new PastAppointment();
        pastAppointment.setUserId(appointment.getUserId());
        pastAppointment.setDoctorId(appointment.getDoctorId());
        pastAppointment.setWorkplaceId(appointment.getWorkplaceId());
        pastAppointment.setWorkplaceName(appointment.getWorkplaceName());
        pastAppointment.setWorkplaceType(appointment.getWorkplaceType());
        pastAppointment.setWorkplaceAddress(appointment.getWorkplaceAddress());
        pastAppointment.setAppointmentDate(appointment.getAppointmentDate());
        pastAppointment.setSlot(appointment.getSlot());
        pastAppointment.setAppointmentTime(appointment.getAppointmentTime());
        pastAppointment.setDurationMinutes(appointment.getDurationMinutes());
        pastAppointment.setQueuePosition(appointment.getQueuePosition());
        pastAppointment.setStatus(appointment.getStatus());
        pastAppointment.setNotes(appointment.getNotes());
        pastAppointment.setDoctorName(appointment.getDoctorName());
        pastAppointment.setDoctorSpecialization(appointment.getDoctorSpecialization());
        pastAppointment.setCreatedAt(appointment.getCreatedAt());
        pastAppointment.setUpdatedAt(appointment.getUpdatedAt());
        
        pastAppointmentRepository.save(pastAppointment);
        appointmentRepository.delete(appointment);
    }
    
    private void createRescheduledAppointment(Appointment originalAppointment, BulkAppointmentStatusUpdateDto request) {
        LocalDate newDate = LocalDate.parse(request.getNewAppointmentDate());
        LocalDate today = LocalDate.now();
        LocalDate dayAfterTomorrow = today.plusDays(2);
        
        if (newDate.isEqual(today)) {
            // Create current day appointment
            Appointment newAppointment = new Appointment();
            newAppointment.setUserId(originalAppointment.getUserId());
            newAppointment.setDoctorId(originalAppointment.getDoctorId());
            
            // Preserve family member ID and name if this is a family appointment
            newAppointment.setPatientMemberId(originalAppointment.getPatientMemberId());
            newAppointment.setPatientName(originalAppointment.getPatientName());
            
            // Set workplace details
            if (request.getNewWorkplaceId() != null) {
                Optional<DoctorWorkplace> workplaceOpt = workplaceRepository.findById(request.getNewWorkplaceId());
                if (workplaceOpt.isPresent()) {
                    DoctorWorkplace workplace = workplaceOpt.get();
                    newAppointment.setWorkplaceId(workplace.getId());
                    newAppointment.setWorkplaceName(workplace.getWorkplaceName());
                    newAppointment.setWorkplaceType(workplace.getWorkplaceType());
                    newAppointment.setWorkplaceAddress(workplace.getAddress());
                }
            } else {
                newAppointment.setWorkplaceId(originalAppointment.getWorkplaceId());
                newAppointment.setWorkplaceName(originalAppointment.getWorkplaceName());
                newAppointment.setWorkplaceType(originalAppointment.getWorkplaceType());
                newAppointment.setWorkplaceAddress(originalAppointment.getWorkplaceAddress());
            }
            
            newAppointment.setAppointmentDate(request.getNewAppointmentDate());
            newAppointment.setSlot(request.getNewTimeSlot());
            newAppointment.setAppointmentTime(parseSlotToDateTime(request.getNewTimeSlot(), request.getNewAppointmentDate()));
            newAppointment.setDurationMinutes(originalAppointment.getDurationMinutes());
            newAppointment.setStatus("BOOKED");
            newAppointment.setNotes("Rescheduled from " + originalAppointment.getAppointmentDate());
            newAppointment.setDoctorName(originalAppointment.getDoctorName());
            newAppointment.setDoctorSpecialization(originalAppointment.getDoctorSpecialization());
            newAppointment.setQueuePosition(getNextQueuePosition(originalAppointment.getDoctorId(), 
                    newAppointment.getWorkplaceId(), request.getNewAppointmentDate()));
            
            appointmentRepository.save(newAppointment);
            
        } else if (newDate.isAfter(today) && !newDate.isAfter(dayAfterTomorrow)) {
            // Create future appointment (within 2 days) but persist into appointments table
            Appointment futureAppt = new Appointment();
            futureAppt.setUserId(originalAppointment.getUserId());
            futureAppt.setDoctorId(originalAppointment.getDoctorId());
            
            // Preserve family member ID and name if this is a family appointment
            futureAppt.setPatientMemberId(originalAppointment.getPatientMemberId());
            futureAppt.setPatientName(originalAppointment.getPatientName());

            // Set workplace details
            Long workplaceIdToUse = originalAppointment.getWorkplaceId();
            String workplaceNameToUse = originalAppointment.getWorkplaceName();
            String workplaceTypeToUse = originalAppointment.getWorkplaceType();
            String workplaceAddressToUse = originalAppointment.getWorkplaceAddress();

            if (request.getNewWorkplaceId() != null) {
                Optional<DoctorWorkplace> workplaceOpt = workplaceRepository.findById(request.getNewWorkplaceId());
                if (workplaceOpt.isPresent()) {
                    DoctorWorkplace workplace = workplaceOpt.get();
                    workplaceIdToUse = workplace.getId();
                    workplaceNameToUse = workplace.getWorkplaceName();
                    workplaceTypeToUse = workplace.getWorkplaceType();
                    workplaceAddressToUse = workplace.getAddress();
                }
            }

            futureAppt.setWorkplaceId(workplaceIdToUse);
            futureAppt.setWorkplaceName(workplaceNameToUse);
            futureAppt.setWorkplaceType(workplaceTypeToUse);
            futureAppt.setWorkplaceAddress(workplaceAddressToUse);
            futureAppt.setAppointmentDate(request.getNewAppointmentDate());
            futureAppt.setSlot(request.getNewTimeSlot());
            futureAppt.setAppointmentTime(parseSlotToDateTime(request.getNewTimeSlot(), request.getNewAppointmentDate()));
            futureAppt.setDurationMinutes(originalAppointment.getDurationMinutes());
            futureAppt.setStatus("BOOKED");
            futureAppt.setNotes("Rescheduled from " + originalAppointment.getAppointmentDate());

            // Set doctor details from original appointment
            futureAppt.setDoctorName(originalAppointment.getDoctorName());
            futureAppt.setDoctorSpecialization(originalAppointment.getDoctorSpecialization());

            futureAppt.setQueuePosition(getNextQueuePositionFuture(originalAppointment.getDoctorId(), 
                    workplaceIdToUse, request.getNewAppointmentDate()));

            appointmentRepository.save(futureAppt);
        } else {
            throw new RuntimeException("Rescheduling is only allowed for today or within next 2 days");
        }
    }
    
    private void createRescheduledAppointment(Appointment originalAppointment, UserRescheduleRequestDto request) {
        // Create new appointment in the appointments table (no date restrictions)
        Appointment newAppointment = new Appointment();
        newAppointment.setUserId(originalAppointment.getUserId());
        newAppointment.setDoctorId(originalAppointment.getDoctorId());
        newAppointment.setWorkplaceId(originalAppointment.getWorkplaceId());
        newAppointment.setWorkplaceName(originalAppointment.getWorkplaceName());
        newAppointment.setWorkplaceType(originalAppointment.getWorkplaceType());
        newAppointment.setWorkplaceAddress(originalAppointment.getWorkplaceAddress());
        
        // Preserve family member ID and name if this is a family appointment
        newAppointment.setPatientMemberId(originalAppointment.getPatientMemberId());
        newAppointment.setPatientName(originalAppointment.getPatientName());
        
        newAppointment.setAppointmentDate(request.getNewAppointmentDate());
        newAppointment.setSlot(request.getNewTimeSlot());
        newAppointment.setAppointmentTime(parseSlotToDateTime(request.getNewTimeSlot(), request.getNewAppointmentDate()));
        newAppointment.setDurationMinutes(originalAppointment.getDurationMinutes());
        newAppointment.setStatus("BOOKED");
        newAppointment.setNotes("Rescheduled by user: " + request.getReason());
        newAppointment.setDoctorName(originalAppointment.getDoctorName());
        newAppointment.setDoctorSpecialization(originalAppointment.getDoctorSpecialization());
        newAppointment.setQueuePosition(getNextQueuePosition(originalAppointment.getDoctorId(), 
                originalAppointment.getWorkplaceId(), request.getNewAppointmentDate()));
        
        appointmentRepository.save(newAppointment);
    }
    
    private void createRescheduledAppointmentFromFuture(Appointment originalAppointment, UserRescheduleRequestDto request) {
        // Create new appointment in the appointments table (no date restrictions)
        Appointment newAppointment = new Appointment();
        newAppointment.setUserId(originalAppointment.getUserId());
        newAppointment.setDoctorId(originalAppointment.getDoctorId());
        newAppointment.setWorkplaceId(originalAppointment.getWorkplaceId());
        newAppointment.setWorkplaceName(originalAppointment.getWorkplaceName());
        newAppointment.setWorkplaceType(originalAppointment.getWorkplaceType());
        newAppointment.setWorkplaceAddress(originalAppointment.getWorkplaceAddress());
        
        // Preserve family member ID and name if this is a family appointment
        newAppointment.setPatientMemberId(originalAppointment.getPatientMemberId());
        newAppointment.setPatientName(originalAppointment.getPatientName());
        
        newAppointment.setAppointmentDate(request.getNewAppointmentDate());
        newAppointment.setSlot(request.getNewTimeSlot());
        newAppointment.setAppointmentTime(parseSlotToDateTime(request.getNewTimeSlot(), request.getNewAppointmentDate()));
        newAppointment.setDurationMinutes(originalAppointment.getDurationMinutes());
        newAppointment.setStatus("BOOKED");
        newAppointment.setNotes("Rescheduled by user: " + request.getReason());
        newAppointment.setDoctorName(originalAppointment.getDoctorName());
        newAppointment.setDoctorSpecialization(originalAppointment.getDoctorSpecialization());
        newAppointment.setQueuePosition(getNextQueuePosition(originalAppointment.getDoctorId(), 
                originalAppointment.getWorkplaceId(), request.getNewAppointmentDate()));
        
        appointmentRepository.save(newAppointment);
    }
    
    // ==================== FCM TOKEN MANAGEMENT ====================
    
    @Override
    @Transactional
    public boolean updateFcmToken(Long userId, String fcmToken, String deviceType) {
        try {
            Optional<UserDetails> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                UserDetails user = userOpt.get();
                user.setFcmToken(fcmToken);
                user.setDeviceType(deviceType.toLowerCase());
                // lastTokenUpdate is set automatically in the setter
                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean toggleNotifications(Long userId, Boolean enabled) {
        try {
            Optional<UserDetails> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                UserDetails user = userOpt.get();
                user.setNotificationsEnabled(enabled);
                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void sendAppointmentNotification(Long userId, String title, String body, String notificationType) {
        try {
            System.out.println("[NOTIFICATION] Attempting to send notification to user " + userId);
            System.out.println("[NOTIFICATION] Title: " + title);
            System.out.println("[NOTIFICATION] Body: " + body);
            System.out.println("[NOTIFICATION] Type: " + notificationType);
            
            Optional<UserDetails> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                UserDetails user = userOpt.get();
                
                System.out.println("[NOTIFICATION] User found: " + user.getFullName());
                System.out.println("[NOTIFICATION] Notifications enabled: " + user.getNotificationsEnabled());
                System.out.println("[NOTIFICATION] FCM Token present: " + (user.getFcmToken() != null && !user.getFcmToken().trim().isEmpty()));
                System.out.println("[NOTIFICATION] Device type: " + user.getDeviceType());
                
                if (user.getFcmToken() != null && !user.getFcmToken().trim().isEmpty()) {
                    System.out.println("[NOTIFICATION] FCM Token preview: " + user.getFcmToken().substring(0, Math.min(30, user.getFcmToken().length())) + "...");
                }
                
                // Check if user has notifications enabled and has FCM token
                if (user.getNotificationsEnabled() != null && user.getNotificationsEnabled() 
                    && user.getFcmToken() != null && !user.getFcmToken().trim().isEmpty()) {
                    
                    // Create notification with appointment-specific data
                    NotificationRequestDto notificationRequest = new NotificationRequestDto();
                    notificationRequest.setDeviceToken(user.getFcmToken());
                    notificationRequest.setTitle(title);
                    notificationRequest.setBody(body);
                    
                    // Add platform-specific configuration for proper system notifications
                    if ("android".equalsIgnoreCase(user.getDeviceType())) {
                        AndroidConfigDto androidConfig = new AndroidConfigDto();
                        androidConfig.setChannelId("appointment_updates");
                        androidConfig.setPriority("high");
                        androidConfig.setSound("default");
                        notificationRequest.setAndroidConfig(androidConfig);
                    } else if ("ios".equalsIgnoreCase(user.getDeviceType())) {
                        IOSConfigDto iosConfig = new IOSConfigDto();
                        iosConfig.setSound("default");
                        iosConfig.setBadge(1);
                        iosConfig.setContentAvailable(true);
                        notificationRequest.setIosConfig(iosConfig);
                    }
                    
                    // Add data payload for app-specific handling
                    Map<String, String> data = new HashMap<>();
                    data.put("type", notificationType);
                    data.put("userId", userId.toString());
                    data.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    notificationRequest.setData(data);
                    
                    // Send notification
                    System.out.println("[NOTIFICATION] Sending notification to FCM...");
                    NotificationResponseDto response = notificationService.sendNotificationToDevice(notificationRequest);
                    
                    // Log result and handle invalid tokens
                    if (response.isSuccess()) {
                        System.out.println("[NOTIFICATION] SUCCESS - Message ID: " + response.getMessageId());
                    } else {
                        System.out.println("[NOTIFICATION] FAILED - Error: " + response.getErrorMessage());
                        
                        // If token is invalid/unregistered, clear it from database
                        // Common error messages for invalid tokens: "UNREGISTERED", "not found", "invalid"
                        String errorMsg = response.getErrorMessage() != null ? response.getErrorMessage().toLowerCase() : "";
                        if (errorMsg.contains("unregistered") || errorMsg.contains("not found") || 
                            errorMsg.contains("invalid") || response.getStatusCode() == 404 || response.getStatusCode() == 400) {
                            System.out.println("[NOTIFICATION] Clearing invalid FCM token for user " + userId);
                            user.setFcmToken(null);
                            user.setDeviceType(null);
                            userRepository.save(user);
                            System.out.println("[NOTIFICATION] FCM token cleared. User will get new token on next app open.");
                        }
                    }
                } else {
                    System.out.println("[NOTIFICATION] Skipped - Notifications disabled or no FCM token");
                }
            } else {
                System.out.println("[NOTIFICATION] User not found with ID: " + userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[NOTIFICATION] Exception while sending notification to user " + userId + ": " + e.getMessage());
        }
    }
}
