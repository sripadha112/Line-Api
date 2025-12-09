package com.example.auth.service;

import com.example.auth.dto.CalendarEventDto;
import com.example.auth.entity.Appointment;
import com.example.auth.entity.DoctorDetails;
import com.example.auth.entity.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Service to handle calendar integration for appointments
 */
@Service
public class AppointmentCalendarService {
    
    private static final Logger logger = LoggerFactory.getLogger(AppointmentCalendarService.class);
    
    @Autowired
    private CalendarFactoryService calendarFactoryService;
    
    /**
     * Create a calendar event for an appointment
     * @param appointment The appointment entity
     * @param doctor Doctor details
     * @param user User details  
     * @param userAccessToken User's calendar access token
     * @param request HTTP request to detect device OS
     * @return Calendar event ID or null if creation failed
     */
    public String createCalendarEvent(Appointment appointment, DoctorDetails doctor, UserDetails user, 
                                    String userAccessToken, HttpServletRequest request) {
        try {
            if (!calendarFactoryService.isCalendarSupportedForDevice(request)) {
                logger.warn("Calendar integration not supported for this device");
                return null;
            }
            
            CalendarService calendarService = calendarFactoryService.getCalendarService(request);
            CalendarEventDto eventDto = createEventDto(appointment, doctor, user);
            
            String eventId = calendarService.createEvent(eventDto, userAccessToken);
            
            if (eventId != null) {
                appointment.setCalendarEventId(eventId);
                appointment.setCalendarServiceType(calendarService.getServiceType().name());
                
                logger.info("Calendar event created successfully for appointment ID: {} with event ID: {}", 
                           appointment.getId(), eventId);
            }
            
            return eventId;
            
        } catch (Exception e) {
            logger.error("Failed to create calendar event for appointment ID: {}", appointment.getId(), e);
            return null;
        }
    }
    
    /**
     * Create a calendar event for an appointment with device type header
     * @param appointment The appointment entity
     * @param doctor Doctor details
     * @param user User details
     * @param userAccessToken User's calendar access token
     * @param deviceType Device type from header
     * @return Calendar event ID or null if creation failed
     */
    public String createCalendarEvent(Appointment appointment, DoctorDetails doctor, UserDetails user, 
                                    String userAccessToken, String deviceType) {
        try {
            CalendarService calendarService = calendarFactoryService.getCalendarService(deviceType);
            CalendarEventDto eventDto = createEventDto(appointment, doctor, user);
            
            String eventId = calendarService.createEvent(eventDto, userAccessToken);
            
            if (eventId != null) {
                appointment.setCalendarEventId(eventId);
                appointment.setCalendarServiceType(calendarService.getServiceType().name());
                
                logger.info("Calendar event created successfully for appointment ID: {} with event ID: {}", 
                           appointment.getId(), eventId);
            }
            
            return eventId;
            
        } catch (Exception e) {
            logger.error("Failed to create calendar event for appointment ID: {}", appointment.getId(), e);
            return null;
        }
    }
    
    /**
     * Update a calendar event for an appointment
     * @param appointment The appointment entity
     * @param doctor Doctor details
     * @param user User details
     * @param userAccessToken User's calendar access token
     * @param request HTTP request to detect device OS
     * @return Updated event ID or null if update failed
     */
    public String updateCalendarEvent(Appointment appointment, DoctorDetails doctor, UserDetails user, 
                                    String userAccessToken, HttpServletRequest request) {
        try {
            if (appointment.getCalendarEventId() == null) {
                logger.warn("No calendar event ID found for appointment ID: {}, creating new event", appointment.getId());
                return createCalendarEvent(appointment, doctor, user, userAccessToken, request);
            }
            
            CalendarService calendarService = calendarFactoryService.getCalendarService(request);
            CalendarEventDto eventDto = createEventDto(appointment, doctor, user);
            
            String eventId = calendarService.updateEvent(appointment.getCalendarEventId(), eventDto, userAccessToken);
            
            if (eventId != null) {
                appointment.setCalendarEventId(eventId);
                logger.info("Calendar event updated successfully for appointment ID: {} with event ID: {}", 
                           appointment.getId(), eventId);
            }
            
            return eventId;
            
        } catch (Exception e) {
            logger.error("Failed to update calendar event for appointment ID: {}", appointment.getId(), e);
            return null;
        }
    }
    
    /**
     * Delete a calendar event for an appointment
     * @param appointment The appointment entity
     * @param userAccessToken User's calendar access token
     * @param request HTTP request to detect device OS
     * @return true if deletion was successful
     */
    public boolean deleteCalendarEvent(Appointment appointment, String userAccessToken, HttpServletRequest request) {
        try {
            if (appointment.getCalendarEventId() == null) {
                logger.warn("No calendar event ID found for appointment ID: {}", appointment.getId());
                return true; // Consider it successful if there's nothing to delete
            }
            
            CalendarService calendarService = calendarFactoryService.getCalendarService(request);
            boolean deleted = calendarService.deleteEvent(appointment.getCalendarEventId(), userAccessToken);
            
            if (deleted) {
                logger.info("Calendar event deleted successfully for appointment ID: {} with event ID: {}", 
                           appointment.getId(), appointment.getCalendarEventId());
                
                // Clear calendar event data from appointment
                appointment.setCalendarEventId(null);
                appointment.setCalendarServiceType(null);
            }
            
            return deleted;
            
        } catch (Exception e) {
            logger.error("Failed to delete calendar event for appointment ID: {}", appointment.getId(), e);
            return false;
        }
    }
    
    /**
     * Create CalendarEventDto from appointment details
     */
    private CalendarEventDto createEventDto(Appointment appointment, DoctorDetails doctor, UserDetails user) {
        CalendarEventDto eventDto = new CalendarEventDto();
        
        // Set event title
        eventDto.setTitle(String.format("Appointment with Dr. %s", appointment.getDoctorName()));
        
        // Set event description
        String description = String.format(
            "Medical Appointment\n" +
            "Doctor: Dr. %s\n" +
            "Specialization: %s\n" +
            "Location: %s\n" +
            "Slot: %s\n" +
            "Duration: %d minutes\n" +
            "Status: %s",
            appointment.getDoctorName(),
            appointment.getDoctorSpecialization() != null ? appointment.getDoctorSpecialization() : "General",
            appointment.getWorkplaceAddress() != null ? appointment.getWorkplaceAddress() : appointment.getWorkplaceName(),
            appointment.getSlot(),
            appointment.getDurationMinutes(),
            appointment.getStatus()
        );
        
        if (appointment.getNotes() != null && !appointment.getNotes().trim().isEmpty()) {
            description += "\n\nNotes: " + appointment.getNotes();
        }
        
        eventDto.setDescription(description);
        
        // Set location
        String location = appointment.getWorkplaceAddress() != null 
            ? appointment.getWorkplaceAddress() 
            : appointment.getWorkplaceName();
        eventDto.setLocation(location);
        
        // Set start and end times
        LocalDateTime startDateTime = appointment.getAppointmentTime().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDateTime = startDateTime.plusMinutes(appointment.getDurationMinutes());
        
        eventDto.setStartDateTime(startDateTime);
        eventDto.setEndDateTime(endDateTime);
        
        // Set timezone (you may want to get this from user preferences or doctor's location)
        eventDto.setTimeZone(ZoneId.systemDefault().getId());
        
        // Set attendee email if available
        if (user != null && user.getEmail() != null) {
            eventDto.setAttendeeEmail(user.getEmail());
        }
        
        return eventDto;
    }
}