package com.example.auth.service.impl;

import com.example.auth.dto.CalendarEventDto;
import com.example.auth.service.CalendarService;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

/**
 * Google Calendar Service implementation for Android devices
 */
@Service
public class GoogleCalendarServiceImpl implements CalendarService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarServiceImpl.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CALENDAR_ID = "primary";
    
    @Value("${app.calendar.google.application-name:Line App Appointment Booking}")
    private String applicationName;
    
    @Override
    public String createEvent(CalendarEventDto eventDto, String userAccessToken) {
        try {
            Calendar service = getCalendarService(userAccessToken);
            
            Event event = new Event()
                .setSummary(eventDto.getTitle())
                .setDescription(eventDto.getDescription())
                .setLocation(eventDto.getLocation());
            
            // Set start time
            ZonedDateTime startZonedDateTime = eventDto.getStartDateTime()
                .atZone(ZoneId.of(eventDto.getTimeZone() != null ? eventDto.getTimeZone() : "UTC"));
            DateTime startDateTime = new DateTime(startZonedDateTime.toInstant().toEpochMilli());
            EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(eventDto.getTimeZone() != null ? eventDto.getTimeZone() : "UTC");
            event.setStart(start);
            
            // Set end time
            ZonedDateTime endZonedDateTime = eventDto.getEndDateTime()
                .atZone(ZoneId.of(eventDto.getTimeZone() != null ? eventDto.getTimeZone() : "UTC"));
            DateTime endDateTime = new DateTime(endZonedDateTime.toInstant().toEpochMilli());
            EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(eventDto.getTimeZone() != null ? eventDto.getTimeZone() : "UTC");
            event.setEnd(end);
            
            // Add attendee if provided
            if (eventDto.getAttendeeEmail() != null && !eventDto.getAttendeeEmail().isEmpty()) {
                EventAttendee attendee = new EventAttendee()
                    .setEmail(eventDto.getAttendeeEmail());
                event.setAttendees(Collections.singletonList(attendee));
            }
            
            // Insert the event
            event = service.events().insert(CALENDAR_ID, event).execute();
            
            logger.info("Google Calendar event created successfully with ID: {}", event.getId());
            return event.getId();
            
        } catch (Exception e) {
            logger.error("Error creating Google Calendar event: ", e);
            throw new RuntimeException("Failed to create Google Calendar event", e);
        }
    }
    
    @Override
    public String updateEvent(String eventId, CalendarEventDto eventDto, String userAccessToken) {
        try {
            Calendar service = getCalendarService(userAccessToken);
            
            // Get the existing event
            Event event = service.events().get(CALENDAR_ID, eventId).execute();
            
            // Update event details
            event.setSummary(eventDto.getTitle())
                .setDescription(eventDto.getDescription())
                .setLocation(eventDto.getLocation());
            
            // Update start time
            ZonedDateTime startZonedDateTime = eventDto.getStartDateTime()
                .atZone(ZoneId.of(eventDto.getTimeZone() != null ? eventDto.getTimeZone() : "UTC"));
            DateTime startDateTime = new DateTime(startZonedDateTime.toInstant().toEpochMilli());
            EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(eventDto.getTimeZone() != null ? eventDto.getTimeZone() : "UTC");
            event.setStart(start);
            
            // Update end time
            ZonedDateTime endZonedDateTime = eventDto.getEndDateTime()
                .atZone(ZoneId.of(eventDto.getTimeZone() != null ? eventDto.getTimeZone() : "UTC"));
            DateTime endDateTime = new DateTime(endZonedDateTime.toInstant().toEpochMilli());
            EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(eventDto.getTimeZone() != null ? eventDto.getTimeZone() : "UTC");
            event.setEnd(end);
            
            // Update attendee if provided
            if (eventDto.getAttendeeEmail() != null && !eventDto.getAttendeeEmail().isEmpty()) {
                EventAttendee attendee = new EventAttendee()
                    .setEmail(eventDto.getAttendeeEmail());
                event.setAttendees(Collections.singletonList(attendee));
            }
            
            // Update the event
            Event updatedEvent = service.events().update(CALENDAR_ID, eventId, event).execute();
            
            logger.info("Google Calendar event updated successfully with ID: {}", updatedEvent.getId());
            return updatedEvent.getId();
            
        } catch (Exception e) {
            logger.error("Error updating Google Calendar event: ", e);
            throw new RuntimeException("Failed to update Google Calendar event", e);
        }
    }
    
    @Override
    public boolean deleteEvent(String eventId, String userAccessToken) {
        try {
            Calendar service = getCalendarService(userAccessToken);
            
            service.events().delete(CALENDAR_ID, eventId).execute();
            
            logger.info("Google Calendar event deleted successfully with ID: {}", eventId);
            return true;
            
        } catch (Exception e) {
            logger.error("Error deleting Google Calendar event: ", e);
            return false;
        }
    }
    
    @Override
    public CalendarServiceType getServiceType() {
        return CalendarServiceType.GOOGLE_CALENDAR;
    }
    
    private Calendar getCalendarService(String accessToken) throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
            .setAccessToken(accessToken);
        
        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(applicationName)
            .build();
    }
}