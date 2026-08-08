package com.app.auth.service.impl;

import com.app.auth.dto.DoctorSlotDto;
import com.app.auth.dto.DoctorWorkplaceDto;
import com.app.auth.entity.DoctorDetails;
import com.app.auth.entity.DoctorSlot;
import com.app.auth.entity.DoctorWorkplace;
import com.app.auth.repository.DoctorDetailsRepository;
import com.app.auth.repository.DoctorSlotRepository;
import com.app.auth.repository.DoctorWorkplaceRepository;
import com.app.auth.service.DoctorSlotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorSlotServiceImpl implements DoctorSlotService {

    private final DoctorSlotRepository slotRepository;
    private final DoctorDetailsRepository doctorRepository;
    private final DoctorWorkplaceRepository workplaceRepository;

    public DoctorSlotServiceImpl(DoctorSlotRepository slotRepository,
                                DoctorDetailsRepository doctorRepository,
                                DoctorWorkplaceRepository workplaceRepository) {
        this.slotRepository = slotRepository;
        this.doctorRepository = doctorRepository;
        this.workplaceRepository = workplaceRepository;
    }

    @Override
    @Transactional
    public void generateSlotsForDoctorAndDate(Long doctorId, LocalDate date) {
        System.out.println("[DEBUG] Generating slots for doctor " + doctorId + " on " + date);
        
        Optional<DoctorDetails> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            System.out.println("[DEBUG] Doctor not found: " + doctorId);
            return;
        }

        DoctorDetails doctor = doctorOpt.get();
        List<DoctorWorkplace> workplaces = workplaceRepository.findByDoctorId(doctorId);
        
        if (workplaces.isEmpty()) {
            System.out.println("[DEBUG] No workplaces found for doctor: " + doctorId);
            return;
        }

        // Check if slots already exist for this date
        List<DoctorSlot> existingSlots = slotRepository.findSlotsByDoctorAndDate(doctorId, date);
        if (!existingSlots.isEmpty()) {
            System.out.println("[DEBUG] Slots already exist for doctor " + doctorId + " on " + date);
            return;
        }

//        int durationMinutes = doctor.getCheckingDurationMinutes() != null ? doctor.getCheckingDurationMinutes() : 30;

        // Generate morning slots
//        if (doctor.getMorningStartTime() != null && doctor.getMorningEndTime() != null) {
//            generateSlotsForSession(doctorId, workplaces, date,
//                doctor.getMorningStartTime(), doctor.getMorningEndTime(),
//                durationMinutes, "MORNING");
//        }

        // Generate evening slots
//        if (doctor.getEveningStartTime() != null && doctor.getEveningEndTime() != null) {
//            generateSlotsForSession(doctorId, workplaces, date,
//                doctor.getEveningStartTime(), doctor.getEveningEndTime(),
//                durationMinutes, "EVENING");
//        }

        System.out.println("[DEBUG] Slot generation completed for doctor " + doctorId + " on " + date);
    }

    private void generateSlotsForSession(Long doctorId, List<DoctorWorkplace> workplaces, 
                                       LocalDate date, LocalTime startTime, LocalTime endTime, 
                                       int durationMinutes, String sessionType) {
        
        LocalTime currentTime = startTime;
        while (currentTime.plus(durationMinutes, ChronoUnit.MINUTES).isBefore(endTime) || 
               currentTime.plus(durationMinutes, ChronoUnit.MINUTES).equals(endTime)) {
            
            LocalTime slotEndTime = currentTime.plus(durationMinutes, ChronoUnit.MINUTES);
            
            // Create slots for each workplace
            for (DoctorWorkplace workplace : workplaces) {
                DoctorSlot slot = new DoctorSlot();
                slot.setDoctorId(doctorId);
                slot.setWorkplace(workplace);
                slot.setSlotDate(date);
                slot.setStartTime(currentTime);
                slot.setEndTime(slotEndTime);
                slot.setSessionType(sessionType);
                slot.setIsAvailable(true);
                
                slotRepository.save(slot);
            }
            
            currentTime = slotEndTime;
        }
    }

    @Override
    public List<DoctorSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        List<DoctorSlot> slots = slotRepository.findAvailableSlotsByDoctorAndDate(doctorId, date);
        return slots.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<DoctorSlotDto> getAvailableSlots(Long doctorId, LocalDate fromDate, LocalDate toDate) {
        // If no dates provided, default to current date + 2 days
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }
        if (toDate == null) {
            toDate = fromDate.plusDays(2);
        }
        
        List<DoctorSlot> slots = slotRepository.findAvailableSlotsByDoctorAndDateRange(doctorId, fromDate, toDate);
        return slots.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<DoctorSlotDto> getAvailableSlotsByWorkplace(Long doctorId, Long workplaceId, LocalDate date) {
        List<DoctorSlot> slots = slotRepository.findAvailableSlotsByDoctorWorkplaceAndDate(doctorId, workplaceId, date);
        return slots.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markSlotAsBooked(Long slotId) {
        Optional<DoctorSlot> slotOpt = slotRepository.findById(slotId);
        if (slotOpt.isPresent()) {
            DoctorSlot slot = slotOpt.get();
            slot.setIsAvailable(false);
            slotRepository.save(slot);
            System.out.println("[DEBUG] Slot " + slotId + " marked as booked");
        }
    }

    @Override
    @Transactional
    public void generateSlotsForAllDoctors(int daysAhead) {
        List<DoctorDetails> doctors = doctorRepository.findAll();
        LocalDate startDate = LocalDate.now();
        
        for (int i = 0; i <= daysAhead; i++) {
            LocalDate targetDate = startDate.plusDays(i);
            for (DoctorDetails doctor : doctors) {
                generateSlotsForDoctorAndDate(doctor.getId(), targetDate);
            }
        }
        
        System.out.println("[DEBUG] Generated slots for " + doctors.size() + " doctors for " + (daysAhead + 1) + " days");
    }

    private DoctorSlotDto convertToDto(DoctorSlot slot) {
        DoctorSlotDto dto = new DoctorSlotDto();
        dto.setId(slot.getId());
        dto.setDoctorId(slot.getDoctorId());
        dto.setSlotDate(slot.getSlotDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setSessionType(slot.getSessionType());
        dto.setIsAvailable(slot.getIsAvailable());
        
        if (slot.getWorkplace() != null) {
            DoctorWorkplaceDto workplaceDto = new DoctorWorkplaceDto();
            workplaceDto.setId(slot.getWorkplace().getId());
            workplaceDto.setWorkplaceName(slot.getWorkplace().getWorkplaceName());
            workplaceDto.setWorkplaceType(slot.getWorkplace().getWorkplaceType());
            workplaceDto.setAddress(slot.getWorkplace().getAddress());
            workplaceDto.setContactNumber(slot.getWorkplace().getContactNumber());
            workplaceDto.setIsPrimary(slot.getWorkplace().getIsPrimary());
            dto.setWorkplace(workplaceDto);
        }
        
        return dto;
    }
}
