package com.example.auth.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalTime;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final DateConverter dateConverter;
    
    public WebConfig(DateConverter dateConverter) {
        this.dateConverter = dateConverter;
    }
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(dateConverter);
    }
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // Configure LocalTime to handle multiple formats using our custom deserializer
        javaTimeModule.addDeserializer(LocalTime.class, new FlexibleLocalTimeDeserializer());
        
        mapper.registerModule(javaTimeModule);
        return mapper;
    }
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
    }
}
