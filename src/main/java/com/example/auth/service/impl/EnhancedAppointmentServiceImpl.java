package com.example.auth.service.impl;

import com.example.auth.dto.*;
import com.example.auth.entity.*;
import com.example.auth.repository.*;
import com.example.auth.service.EnhancedAppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnhancedAppointmentServiceImpl implements EnhancedAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final FutureTwoDayAppointmentRepository futureAppointmentRepository;
    private final PastAppointmentRepository pastAppointmentRepository;
    private final DoctorDetailsRepository doctorRepository;
    private final DoctorWorkplaceRepository workplaceRepository;
    private final UserDetailsRepository userRepository;

    public EnhancedAppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            FutureTwoDayAppointmentRepository futureAppointmentRepository,
            PastAppointmentRepository pastAppointmentRepository,
            DoctorDetailsRepository doctorRepository,
            DoctorWorkplaceRepository workplaceRepository,
            UserDetailsRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.futureAppointmentRepository = futureAppointmentRepository;
        this.pastAppointmentRepository = pastAppointmentRepository;
        this.doctorRepository = doctorRepository;
        this.workplaceRepository = workplaceRepository;
        this.userRepository = userRepository;
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
                .filter(appointment -> "BOOKED".equals(appointment.getStatus())) // Only booked appointments
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
        
        // Get future appointments from future_2day_appointments table
        List<FutureTwoDayAppointment> allFutureAppointments = futureAppointmentRepository.findByUserIdOrderByAppointmentTime(userId);
        
        // Filter future appointments to only include from today onwards
        Map<String, List<UserAppointmentDto>> futureAppointmentsByDate = allFutureAppointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().compareTo(today) >= 0) // Only today and future
                .filter(appointment -> "BOOKED".equals(appointment.getStatus())) // Only booked appointments
                .map(this::convertFutureToUserAppointmentDto)
                .collect(Collectors.groupingBy(
                    UserAppointmentDto::getAppointmentDate,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        
        appointmentsByDate.putAll(futureAppointmentsByDate);
        
        int totalAppointments = currentAppointmentDtos.size() + 
                               (int) allFutureAppointments.stream()
                                       .filter(appointment -> appointment.getAppointmentDate().compareTo(today) >= 0)
                                       .filter(appointment -> "BOOKED".equals(appointment.getStatus()))
                                       .count();
        
        return new UserAppointmentsResponseDto(appointmentsByDate, totalAppointments);
    }

    @Override
    public AvailableSlotsResponseDto getAvailableSlots(Long doctorId, Long workplaceId) {
        Map<String, List<String>> slotsByDate = new LinkedHashMap<>();
        
        // Get doctor and workplace details
        Optional<DoctorDetails> doctorOpt = doctorRepository.findById(doctorId);
        Optional<DoctorWorkplace> workplaceOpt = workplaceRepository.findById(workplaceId);
        
        if (!doctorOpt.isPresent() || !workplaceOpt.isPresent()) {
            throw new IllegalArgumentException("Doctor or workplace not found");
        }
        
        DoctorDetails doctor = doctorOpt.get();
        DoctorWorkplace workplace = workplaceOpt.get();
        
        // Generate slots for current day + next 2 days (3 days total)
        LocalDate currentDate = LocalDate.now();
        for (int i = 0; i < 3; i++) {
            LocalDate date = currentDate.plusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            List<String> availableSlots = generateAvailableSlots(doctor, workplace, dateStr);
            if (!availableSlots.isEmpty()) {
                slotsByDate.put(dateStr, availableSlots);
            }
        }
        
        return new AvailableSlotsResponseDto(slotsByDate, doctorId, workplaceId, workplace.getWorkplaceName(), doctor.getFullName());
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma");
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
        
        // Check future appointments
        List<FutureTwoDayAppointment> futureAppointments = futureAppointmentRepository.findByDoctorIdAndWorkplaceIdAndAppointmentDate(doctorId, workplaceId, date);
        bookedSlots.addAll(futureAppointments.stream()
                .filter(a -> a.getSlot() != null && !a.getStatus().equals("CANCELLED"))
                .map(FutureTwoDayAppointment::getSlot)
                .collect(Collectors.toSet()));
        
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
        return convertToUserAppointmentDto(saved);
    }

    private UserAppointmentDto bookFutureAppointment(BookAppointmentRequestDto request, DoctorDetails doctor, DoctorWorkplace workplace) {
        FutureTwoDayAppointment appointment = new FutureTwoDayAppointment();
        appointment.setUserId(request.getUserId());
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
        appointment.setQueuePosition(getNextQueuePositionFuture(request.getDoctorId(), request.getWorkplaceId(), request.getAppointmentDate()));
        
        FutureTwoDayAppointment saved = futureAppointmentRepository.save(appointment);
        return convertFutureToUserAppointmentDto(saved);
    }

    private OffsetDateTime parseSlotToDateTime(String slot, String date) {
        // Parse slot like "9:00AM - 9:15AM" and combine with date
        String[] parts = slot.split(" - ");
        String startTimeStr = parts[0];
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mma");
        LocalTime startTime = LocalTime.parse(startTimeStr, timeFormatter);
        LocalDate appointmentDate = LocalDate.parse(date);
        
        return appointmentDate.atTime(startTime).atOffset(OffsetDateTime.now().getOffset());
    }

    private int getNextQueuePosition(Long doctorId, Long workplaceId, String date) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndWorkplaceIdAndAppointmentDate(doctorId, workplaceId, date);
        return appointments.size() + 1;
    }

    private int getNextQueuePositionFuture(Long doctorId, Long workplaceId, String date) {
        List<FutureTwoDayAppointment> appointments = futureAppointmentRepository.findByDoctorIdAndWorkplaceIdAndAppointmentDate(doctorId, workplaceId, date);
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
            return "Appointment cancelled successfully";
        }
        
        // Try to find in future appointments
        Optional<FutureTwoDayAppointment> futureAppointment = futureAppointmentRepository.findById(appointmentId);
        if (futureAppointment.isPresent()) {
            FutureTwoDayAppointment appointment = futureAppointment.get();
            appointment.setStatus("CANCELLED");
            futureAppointmentRepository.save(appointment);
            return "Appointment cancelled successfully";
        }
        
        throw new IllegalArgumentException("Appointment not found");
    }

    @Override
    @Transactional
    public String rescheduleUserAppointment(UserRescheduleRequestDto request) {
        Long appointmentId = request.getAppointmentId();
        
        // Try to find in current appointments
        Optional<Appointment> currentAppointment = appointmentRepository.findById(appointmentId);
        if (currentAppointment.isPresent()) {
            Appointment appointment = currentAppointment.get();
            
            // Mark original appointment as rescheduled
            appointment.setStatus("RESCHEDULED");
            appointment.setNotes(request.getReason());
            appointmentRepository.save(appointment);
            
            // Create new appointment with the new date and time
            createRescheduledAppointmentFromUser(appointment, request);
            
            return "Appointment rescheduled successfully";
        }
        
        // Try to find in future appointments
        Optional<FutureTwoDayAppointment> futureAppointment = futureAppointmentRepository.findById(appointmentId);
        if (futureAppointment.isPresent()) {
            FutureTwoDayAppointment appointment = futureAppointment.get();
            
            // Mark original appointment as rescheduled
            appointment.setStatus("RESCHEDULED");
            appointment.setNotes(request.getReason());
            futureAppointmentRepository.save(appointment);
            
            // Create new appointment with the new date and time
            createRescheduledFutureAppointmentFromUser(appointment, request);
            
            return "Appointment rescheduled successfully";
        }
        
        throw new IllegalArgumentException("Appointment not found");
    }

    @Override
    @Transactional
    public void moveAppointmentsToCurrentDay() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<FutureTwoDayAppointment> appointmentsToMove = futureAppointmentRepository.findByAppointmentDate(today);
        
        System.out.println("[SCHEDULER] Found " + appointmentsToMove.size() + " future appointments to move to current day: " + today);
        
        for (FutureTwoDayAppointment futureAppointment : appointmentsToMove) {
            // Create current appointment
            Appointment currentAppointment = new Appointment();
            currentAppointment.setUserId(futureAppointment.getUserId());
            currentAppointment.setDoctorId(futureAppointment.getDoctorId());
            currentAppointment.setWorkplaceId(futureAppointment.getWorkplaceId());
            currentAppointment.setWorkplaceName(futureAppointment.getWorkplaceName());
            currentAppointment.setWorkplaceType(futureAppointment.getWorkplaceType());
            currentAppointment.setWorkplaceAddress(futureAppointment.getWorkplaceAddress());
            currentAppointment.setAppointmentDate(futureAppointment.getAppointmentDate());
            currentAppointment.setSlot(futureAppointment.getSlot());
            currentAppointment.setAppointmentTime(futureAppointment.getAppointmentTime());
            currentAppointment.setDurationMinutes(futureAppointment.getDurationMinutes());
            currentAppointment.setQueuePosition(futureAppointment.getQueuePosition());
            currentAppointment.setStatus(futureAppointment.getStatus());
            currentAppointment.setNotes(futureAppointment.getNotes());
            currentAppointment.setDoctorName(futureAppointment.getDoctorName());
            currentAppointment.setDoctorSpecialization(futureAppointment.getDoctorSpecialization());
            currentAppointment.setCreatedAt(futureAppointment.getCreatedAt());
            
            appointmentRepository.save(currentAppointment);
            futureAppointmentRepository.delete(futureAppointment);
            
            System.out.println("[SCHEDULER] Moved appointment ID " + futureAppointment.getId() + " for user " + futureAppointment.getUserId());
        }
        
        System.out.println("[SCHEDULER] Successfully moved " + appointmentsToMove.size() + " appointments to current day");
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
        
        return dto;
    }

    private UserAppointmentDto convertFutureToUserAppointmentDto(FutureTwoDayAppointment appointment) {
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
        
        return dto;
    }
    
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
                    updatedCount++;
                    break;
                    
                case "CANCELLED":
                    appointment.setStatus("CANCELLED");
                    appointment.setNotes(request.getNotes());
                    appointmentRepository.save(appointment);
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
            // Create future appointment (within 2 days)
            FutureTwoDayAppointment futureAppointment = new FutureTwoDayAppointment();
            futureAppointment.setUserId(originalAppointment.getUserId());
            futureAppointment.setDoctorId(originalAppointment.getDoctorId());
            
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
            
            futureAppointment.setWorkplaceId(workplaceIdToUse);
            futureAppointment.setWorkplaceName(workplaceNameToUse);
            futureAppointment.setWorkplaceType(workplaceTypeToUse);
            futureAppointment.setWorkplaceAddress(workplaceAddressToUse);
            futureAppointment.setAppointmentDate(request.getNewAppointmentDate());
            futureAppointment.setSlot(request.getNewTimeSlot());
            futureAppointment.setAppointmentTime(parseSlotToDateTime(request.getNewTimeSlot(), request.getNewAppointmentDate()));
            futureAppointment.setDurationMinutes(originalAppointment.getDurationMinutes());
            futureAppointment.setStatus("BOOKED");
            futureAppointment.setNotes("Rescheduled from " + originalAppointment.getAppointmentDate());
            
            // Set doctor details from original appointment
            futureAppointment.setDoctorName(originalAppointment.getDoctorName());
            futureAppointment.setDoctorSpecialization(originalAppointment.getDoctorSpecialization());
            
            futureAppointment.setQueuePosition(getNextQueuePositionFuture(originalAppointment.getDoctorId(), 
                    workplaceIdToUse, request.getNewAppointmentDate()));
            
            futureAppointmentRepository.save(futureAppointment);
        } else {
            throw new RuntimeException("Rescheduling is only allowed for today or within next 2 days");
        }
    }
    
    private void createRescheduledAppointmentFromUser(Appointment originalAppointment, UserRescheduleRequestDto request) {
        LocalDate newDate = LocalDate.parse(request.getNewAppointmentDate());
        LocalDate today = LocalDate.now();
        LocalDate dayAfterTomorrow = today.plusDays(2);
        
        if (newDate.isEqual(today)) {
            // Create current day appointment
            Appointment newAppointment = new Appointment();
            newAppointment.setUserId(originalAppointment.getUserId());
            newAppointment.setDoctorId(originalAppointment.getDoctorId());
            newAppointment.setWorkplaceId(originalAppointment.getWorkplaceId());
            newAppointment.setWorkplaceName(originalAppointment.getWorkplaceName());
            newAppointment.setWorkplaceType(originalAppointment.getWorkplaceType());
            newAppointment.setWorkplaceAddress(originalAppointment.getWorkplaceAddress());
            
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
            
        } else if (newDate.isAfter(today) && !newDate.isAfter(dayAfterTomorrow)) {
            // Create future appointment (within 2 days)
            FutureTwoDayAppointment futureAppointment = new FutureTwoDayAppointment();
            futureAppointment.setUserId(originalAppointment.getUserId());
            futureAppointment.setDoctorId(originalAppointment.getDoctorId());
            futureAppointment.setWorkplaceId(originalAppointment.getWorkplaceId());
            futureAppointment.setWorkplaceName(originalAppointment.getWorkplaceName());
            futureAppointment.setWorkplaceType(originalAppointment.getWorkplaceType());
            futureAppointment.setWorkplaceAddress(originalAppointment.getWorkplaceAddress());
            
            futureAppointment.setAppointmentDate(request.getNewAppointmentDate());
            futureAppointment.setSlot(request.getNewTimeSlot());
            futureAppointment.setAppointmentTime(parseSlotToDateTime(request.getNewTimeSlot(), request.getNewAppointmentDate()));
            futureAppointment.setDurationMinutes(originalAppointment.getDurationMinutes());
            futureAppointment.setStatus("BOOKED");
            futureAppointment.setNotes("Rescheduled by user: " + request.getReason());
            futureAppointment.setDoctorName(originalAppointment.getDoctorName());
            futureAppointment.setDoctorSpecialization(originalAppointment.getDoctorSpecialization());
            futureAppointment.setQueuePosition(getNextQueuePositionFuture(originalAppointment.getDoctorId(), 
                    originalAppointment.getWorkplaceId(), request.getNewAppointmentDate()));
            
            futureAppointmentRepository.save(futureAppointment);
        } else {
            throw new RuntimeException("Rescheduling is only allowed for today or within next 2 days");
        }
    }
    
    private void createRescheduledFutureAppointmentFromUser(FutureTwoDayAppointment originalAppointment, UserRescheduleRequestDto request) {
        LocalDate newDate = LocalDate.parse(request.getNewAppointmentDate());
        LocalDate today = LocalDate.now();
        LocalDate dayAfterTomorrow = today.plusDays(2);
        
        if (newDate.isEqual(today)) {
            // Create current day appointment
            Appointment newAppointment = new Appointment();
            newAppointment.setUserId(originalAppointment.getUserId());
            newAppointment.setDoctorId(originalAppointment.getDoctorId());
            newAppointment.setWorkplaceId(originalAppointment.getWorkplaceId());
            newAppointment.setWorkplaceName(originalAppointment.getWorkplaceName());
            newAppointment.setWorkplaceType(originalAppointment.getWorkplaceType());
            newAppointment.setWorkplaceAddress(originalAppointment.getWorkplaceAddress());
            
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
            
        } else if (newDate.isAfter(today) && !newDate.isAfter(dayAfterTomorrow)) {
            // Create future appointment (within 2 days)
            FutureTwoDayAppointment futureAppointment = new FutureTwoDayAppointment();
            futureAppointment.setUserId(originalAppointment.getUserId());
            futureAppointment.setDoctorId(originalAppointment.getDoctorId());
            futureAppointment.setWorkplaceId(originalAppointment.getWorkplaceId());
            futureAppointment.setWorkplaceName(originalAppointment.getWorkplaceName());
            futureAppointment.setWorkplaceType(originalAppointment.getWorkplaceType());
            futureAppointment.setWorkplaceAddress(originalAppointment.getWorkplaceAddress());
            
            futureAppointment.setAppointmentDate(request.getNewAppointmentDate());
            futureAppointment.setSlot(request.getNewTimeSlot());
            futureAppointment.setAppointmentTime(parseSlotToDateTime(request.getNewTimeSlot(), request.getNewAppointmentDate()));
            futureAppointment.setDurationMinutes(originalAppointment.getDurationMinutes());
            futureAppointment.setStatus("BOOKED");
            futureAppointment.setNotes("Rescheduled by user: " + request.getReason());
            futureAppointment.setDoctorName(originalAppointment.getDoctorName());
            futureAppointment.setDoctorSpecialization(originalAppointment.getDoctorSpecialization());
            futureAppointment.setQueuePosition(getNextQueuePositionFuture(originalAppointment.getDoctorId(), 
                    originalAppointment.getWorkplaceId(), request.getNewAppointmentDate()));
            
            futureAppointmentRepository.save(futureAppointment);
        } else {
            throw new RuntimeException("Rescheduling is only allowed for today or within next 2 days");
        }
    }
}
