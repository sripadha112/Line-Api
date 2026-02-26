package com.app.auth.service.impl;

import com.app.auth.dto.BlockedSlotDto;
import com.app.auth.dto.BlockSlotRequest;
import com.app.auth.entity.Appointment;
import com.app.auth.entity.BlockedSlot;
import com.app.auth.entity.DoctorWorkplace;
import com.app.auth.entity.UserDetails;
import com.app.auth.repository.AppointmentRepository;
import com.app.auth.repository.BlockedSlotRepository;
import com.app.auth.repository.DoctorWorkplaceRepository;
import com.app.auth.repository.UserDetailsRepository;
import com.app.auth.service.BlockedSlotService;
import com.app.auth.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BlockedSlotServiceImpl implements BlockedSlotService {

    private final BlockedSlotRepository blockedSlotRepository;
    private final DoctorWorkplaceRepository workplaceRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final NotificationService notificationService;
    
    private int lastCancelledCount = 0;

    public BlockedSlotServiceImpl(BlockedSlotRepository blockedSlotRepository, 
                                  DoctorWorkplaceRepository workplaceRepository,
                                  AppointmentRepository appointmentRepository,
                                  UserDetailsRepository userDetailsRepository,
                                  NotificationService notificationService) {
        this.blockedSlotRepository = blockedSlotRepository;
        this.workplaceRepository = workplaceRepository;
        this.appointmentRepository = appointmentRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.notificationService = notificationService;
    }
    
    public int getLastCancelledCount() {
        return lastCancelledCount;
    }

    @Override
    public BlockedSlotDto createBlockedSlot(Long doctorId, BlockSlotRequest request) {
        LocalDate blockDate = LocalDate.parse(request.getDate());
        LocalTime startTime = null;
        LocalTime endTime = null;
        
        Boolean isFullDay = request.getIsFullDay() != null && request.getIsFullDay();
        
        // If not full day, parse start and end times
        if (!isFullDay && request.getStartTime() != null && request.getEndTime() != null) {
            startTime = LocalTime.parse(request.getStartTime());
            endTime = LocalTime.parse(request.getEndTime());
        } else {
            isFullDay = true; // If no times provided, treat as full day block
        }
        
        // Cancel existing appointments if requested
        lastCancelledCount = 0;
        if (request.getCancelExistingAppointments() != null && request.getCancelExistingAppointments()) {
            lastCancelledCount = cancelAppointmentsInBlockedTime(
                doctorId, request.getWorkplaceId(), blockDate, startTime, endTime, isFullDay, request.getReason());
        }
        
        BlockedSlot blockedSlot = new BlockedSlot(
            doctorId,
            request.getWorkplaceId(),
            blockDate,
            startTime,
            endTime,
            isFullDay,
            request.getReason()
        );
        
        BlockedSlot saved = blockedSlotRepository.save(blockedSlot);
        System.out.println("[BlockedSlotService] Created blocked slot: " + saved.getId() + 
                          " for doctor " + doctorId + " on " + blockDate + 
                          (isFullDay ? " (full day)" : " from " + startTime + " to " + endTime) +
                          ". Cancelled " + lastCancelledCount + " appointments.");
        
        return convertToDto(saved);
    }
    
    /**
     * Cancel appointments that fall within the blocked time
     */
    private int cancelAppointmentsInBlockedTime(Long doctorId, Long workplaceId, LocalDate date, 
                                                 LocalTime startTime, LocalTime endTime, 
                                                 Boolean isFullDay, String reason) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<Appointment> appointmentsToCancel = new ArrayList<>();
        
        if (workplaceId != null) {
            // Get appointments for specific workplace on this date
            List<Appointment> appointments = appointmentRepository.findByDoctorIdAndWorkplaceIdAndAppointmentDate(
                doctorId, workplaceId, dateStr);
            appointmentsToCancel.addAll(appointments);
        } else {
            // Get all appointments for doctor on this date (all workplaces)
            List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(
                doctorId, dateStr);
            appointmentsToCancel.addAll(appointments);
        }
        
        int cancelledCount = 0;
        List<Long> cancelledUserIds = new ArrayList<>();
        List<Appointment> cancelledAppointments = new ArrayList<>();
        
        for (Appointment appointment : appointmentsToCancel) {
            // Skip already cancelled appointments
            if ("CANCELLED".equals(appointment.getStatus()) || "COMPLETED".equals(appointment.getStatus())) {
                continue;
            }
            
            boolean shouldCancel = false;
            
            if (isFullDay) {
                // Full day block - cancel all appointments
                shouldCancel = true;
            } else if (startTime != null && endTime != null && appointment.getSlot() != null) {
                // Time range block - check if appointment falls within blocked time
                try {
                    LocalTime appointmentTime = parseSlotTime(appointment.getSlot());
                    if (appointmentTime != null) {
                        // Check if appointment time is within blocked range
                        shouldCancel = !appointmentTime.isBefore(startTime) && appointmentTime.isBefore(endTime);
                    }
                } catch (Exception e) {
                    System.out.println("[BlockedSlotService] Error parsing slot time: " + appointment.getSlot());
                }
            }
            
            if (shouldCancel) {
                appointment.setStatus("CANCELLED");
                appointmentRepository.save(appointment);
                cancelledCount++;
                cancelledUserIds.add(appointment.getUserId());
                cancelledAppointments.add(appointment);
            }
        }
        
        // Send notifications to all affected users in batch
        if (!cancelledAppointments.isEmpty()) {
            try {
                sendBatchCancellationNotifications(cancelledAppointments, reason);
            } catch (Exception e) {
                System.out.println("[BlockedSlotService] Error sending batch notifications: " + e.getMessage());
            }
        }
        
        System.out.println("[BlockedSlotService] Cancelled " + cancelledCount + " appointments for blocked time");
        return cancelledCount;
    }
    
    /**
     * Send notifications to multiple users about cancellation
     */
    private void sendBatchCancellationNotifications(List<Appointment> cancelledAppointments, String reason) {
        // Collect device tokens and prepare notifications
        List<String> deviceTokens = new ArrayList<>();
        
        for (Appointment appointment : cancelledAppointments) {
            try {
                Optional<UserDetails> userOpt = userDetailsRepository.findById(appointment.getUserId());
                if (userOpt.isPresent()) {
                    UserDetails user = userOpt.get();
                    String fcmToken = user.getFcmToken();
                    
                    if (fcmToken != null && !fcmToken.isEmpty()) {
                        deviceTokens.add(fcmToken);
                    }
                }
            } catch (Exception e) {
                System.out.println("[BlockedSlotService] Error getting user token for userId " + 
                                  appointment.getUserId() + ": " + e.getMessage());
            }
        }
        
        if (!deviceTokens.isEmpty()) {
            String title = "Appointment Cancelled by Doctor";
            String body = String.format(
                "Your appointment has been cancelled by the doctor.\n\nReason: %s\n\nPlease reschedule at your convenience.",
                reason != null ? reason : "Doctor unavailable"
            );
            
            try {
                String[] tokenArray = deviceTokens.toArray(new String[0]);
                notificationService.sendNotificationToMultipleDevices(tokenArray, title, body);
                System.out.println("[BlockedSlotService] Sent cancellation notifications to " + 
                                  deviceTokens.size() + " users");
            } catch (Exception e) {
                System.out.println("[BlockedSlotService] Failed to send batch notifications: " + e.getMessage());
            }
        }
    }
    
    /**
     * Parse slot time string like "9:00AM - 9:30AM" to get start time
     */
    private LocalTime parseSlotTime(String slot) {
        try {
            String[] parts = slot.split(" - ");
            if (parts.length >= 1) {
                String timeStr = parts[0].trim().toUpperCase();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
                return LocalTime.parse(timeStr, formatter);
            }
        } catch (Exception e) {
            // Try alternate format
            try {
                return LocalTime.parse(slot.split(" ")[0]);
            } catch (Exception ex) {
                // Ignore
            }
        }
        return null;
    }

    @Override
    public List<BlockedSlotDto> getBlockedSlotsByDoctor(Long doctorId) {
        List<BlockedSlot> blockedSlots = blockedSlotRepository.findAllActiveBlocksByDoctor(doctorId);
        return blockedSlots.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<BlockedSlotDto> getBlockedSlotsByWorkplace(Long workplaceId) {
        List<BlockedSlot> blockedSlots = blockedSlotRepository.findAllActiveBlocksByWorkplace(workplaceId);
        return blockedSlots.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<BlockedSlotDto> getBlockedSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        List<BlockedSlot> blockedSlots = blockedSlotRepository.findActiveBlockedSlotsByDoctorAndDate(doctorId, date);
        return blockedSlots.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<BlockedSlotDto> getBlockedSlotsByWorkplaceAndDate(Long doctorId, Long workplaceId, LocalDate date) {
        List<BlockedSlot> blockedSlots = blockedSlotRepository.findActiveBlockedSlotsByDoctorWorkplaceAndDate(
            doctorId, workplaceId, date);
        return blockedSlots.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public boolean isTimeBlocked(Long doctorId, Long workplaceId, LocalDate date, LocalTime time) {
        List<BlockedSlot> blockedSlots = blockedSlotRepository.findBlockedSlotsForTime(
            doctorId, workplaceId, date, time);
        return !blockedSlots.isEmpty();
    }

    @Override
    public BlockedSlotDto getFullDayBlock(Long doctorId, Long workplaceId, LocalDate date) {
        List<BlockedSlot> fullDayBlocks = blockedSlotRepository.findFullDayBlocksForWorkplace(
            doctorId, workplaceId, date);
        if (fullDayBlocks.isEmpty()) {
            return null;
        }
        return convertToDto(fullDayBlocks.get(0));
    }

    @Override
    public void removeBlockedSlot(Long blockedSlotId) {
        Optional<BlockedSlot> blockedSlotOpt = blockedSlotRepository.findById(blockedSlotId);
        if (blockedSlotOpt.isPresent()) {
            BlockedSlot blockedSlot = blockedSlotOpt.get();
            blockedSlot.setIsActive(false);
            blockedSlotRepository.save(blockedSlot);
            System.out.println("[BlockedSlotService] Deactivated blocked slot: " + blockedSlotId);
        }
    }

    @Override
    public List<BlockedSlotDto> getBlockedSlotsForDateRange(Long doctorId, Long workplaceId, 
                                                            LocalDate fromDate, LocalDate toDate) {
        List<BlockedSlot> blockedSlots = blockedSlotRepository.findActiveBlockedSlotsByWorkplaceAndDateRange(
            doctorId, workplaceId, fromDate, toDate);
        return blockedSlots.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private BlockedSlotDto convertToDto(BlockedSlot blockedSlot) {
        BlockedSlotDto dto = new BlockedSlotDto();
        dto.setId(blockedSlot.getId());
        dto.setDoctorId(blockedSlot.getDoctorId());
        dto.setWorkplaceId(blockedSlot.getWorkplaceId());
        dto.setBlockDate(blockedSlot.getBlockDate());
        dto.setStartTime(blockedSlot.getStartTime());
        dto.setEndTime(blockedSlot.getEndTime());
        dto.setIsFullDay(blockedSlot.getIsFullDay());
        dto.setReason(blockedSlot.getReason());
        dto.setCreatedAt(blockedSlot.getCreatedAt());
        dto.setIsActive(blockedSlot.getIsActive());
        
        // Get workplace name if workplaceId is set
        if (blockedSlot.getWorkplaceId() != null) {
            Optional<DoctorWorkplace> workplace = workplaceRepository.findById(blockedSlot.getWorkplaceId());
            workplace.ifPresent(w -> dto.setWorkplaceName(w.getWorkplaceName()));
        } else {
            dto.setWorkplaceName("All Workplaces");
        }
        
        return dto;
    }
}
