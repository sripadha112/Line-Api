package com.app.auth.controller;

import com.app.auth.dto.AuthDtos.CheckMobileRequest;
import com.app.auth.dto.AuthDtos.LoginRequest;
import com.app.auth.dto.AuthDtos.RegisterRequest;
import com.app.auth.dto.AuthDtos.AuthResponse;
import com.app.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Step 1: Frontend calls this first to check if the mobile number
     * is already registered → decides whether to show "Set PIN" or "Enter PIN"
     */
    @PostMapping("/check-mobile")
    public ResponseEntity<?> checkMobile(@Valid @RequestBody CheckMobileRequest req) {
        return ResponseEntity.ok(authService.checkMobile(req));
    }

    /**
     * Register new user with mobile number + PIN + role
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            AuthResponse res = authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Login with mobile number + PIN
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            AuthResponse res = authService.login(req);
            return ResponseEntity.ok(res);
        } catch (IllegalStateException e) {
            // USER_NOT_FOUND
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found", "status", "NOT_FOUND"));
        } catch (IllegalArgumentException e) {
            // Wrong PIN
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
