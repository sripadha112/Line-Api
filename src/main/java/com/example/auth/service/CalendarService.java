package com.example.auth.service;

import com.example.auth.dto.CalendarEventDto;

/**
 * Interface for calendar service operations
 * Supports both Google Calendar (Android) and Apple Calendar (iOS)
 */
public interface CalendarService {
    
    /**
     * Create a calendar event
     * @param eventDto Calendar event details
     * @param userAccessToken User's calendar access token
     * @return Event ID from the calendar service
     */
    String createEvent(CalendarEventDto eventDto, String userAccessToken);
    
    /**
     * Update an existing calendar event
     * @param eventId Existing event ID
     * @param eventDto Updated event details
     * @param userAccessToken User's calendar access token
     * @return Updated event ID
     */
    String updateEvent(String eventId, CalendarEventDto eventDto, String userAccessToken);
    
    /**
     * Delete a calendar event
     * @param eventId Event ID to delete
     * @param userAccessToken User's calendar access token
     * @return Success status
     */
    boolean deleteEvent(String eventId, String userAccessToken);
    
    /**
     * Get the calendar service type
     * @return Service type (GOOGLE_CALENDAR, APPLE_CALENDAR)
     */
    CalendarServiceType getServiceType();
    
    enum CalendarServiceType {
        GOOGLE_CALENDAR, APPLE_CALENDAR
    }
}