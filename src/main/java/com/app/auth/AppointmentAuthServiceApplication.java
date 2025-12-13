package com.app.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppointmentAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppointmentAuthServiceApplication.class, args);
    }
}
