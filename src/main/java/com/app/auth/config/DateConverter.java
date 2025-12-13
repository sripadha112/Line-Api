package com.app.auth.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class DateConverter implements Converter<String, LocalDate> {
    
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ISO_LOCAL_DATE
    };
    
    @Override
    public LocalDate convert(String source) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(source, formatter);
            } catch (DateTimeParseException e) {
                // Continue to next formatter
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + source + 
            ". Supported formats: M/d/yyyy, MM/dd/yyyy, yyyy-MM-dd");
    }
}
