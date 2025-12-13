package com.app.auth.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Custom LocalTime deserializer that handles multiple time formats
 * Supports both string formats (HH:mm, HH:mm:ss) and object formats {hour, minute, second, nano}
 */
@Component
public class FlexibleLocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("HH:mm:ss"),
        DateTimeFormatter.ofPattern("HH:mm"),
        DateTimeFormatter.ofPattern("H:mm"),
        DateTimeFormatter.ofPattern("H:mm:ss")
    };
    
    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.getCurrentToken();
        
        if (token == JsonToken.VALUE_STRING) {
            // Handle string format
            String timeString = p.getText();
            
            if (timeString == null || timeString.trim().isEmpty()) {
                return null;
            }
            
            // Try different string formats
            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    return LocalTime.parse(timeString, formatter);
                } catch (DateTimeParseException e) {
                    // Continue to next format
                }
            }
            
            throw new IOException("Unable to parse LocalTime from string: " + timeString + 
                                 ". Expected formats: HH:mm:ss, HH:mm, H:mm, H:mm:ss");
                                 
        } else if (token == JsonToken.START_OBJECT) {
            // Handle object format {hour, minute, second, nano}
            JsonNode node = p.getCodec().readTree(p);
            
            int hour = node.has("hour") ? node.get("hour").asInt() : 0;
            int minute = node.has("minute") ? node.get("minute").asInt() : 0;
            int second = node.has("second") ? node.get("second").asInt() : 0;
            int nano = node.has("nano") ? node.get("nano").asInt() : 0;
            
            try {
                return LocalTime.of(hour, minute, second, nano);
            } catch (Exception e) {
                throw new IOException("Unable to parse LocalTime from object. Hour: " + hour + 
                                     ", Minute: " + minute + ", Second: " + second + ", Nano: " + nano, e);
            }
            
        } else if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        
        throw new IOException("Unable to parse LocalTime from token: " + token + 
                             ". Expected string format or object with hour/minute/second/nano properties");
    }
}
