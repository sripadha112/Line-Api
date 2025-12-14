package com.app.auth.controller;

import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.OtpRequestDto;
import com.app.auth.dto.OtpVerifyRequestDto;
import com.app.auth.dto.OtpVerifySimpleDto;
import com.app.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@Valid @RequestBody OtpRequestDto req) {
        authService.requestOtp(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-mobile")
    public ResponseEntity<AuthResponse> verifyMobile(@Valid @RequestBody String mobileNumber) {
        AuthResponse response = authService.verifyMobile(mobileNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifySimpleDto otpVerifySimpleDto) {
        AuthResponse response = authService.verifyOtpSimple(otpVerifySimpleDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp-with-registration")
    public ResponseEntity<AuthResponse> verifyOtpWithRegistration(@Valid @RequestBody OtpVerifyRequestDto otpVerifyRequestDto) {
        AuthResponse response = authService.verifyOtp(otpVerifyRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract JWT token from Authorization header (remove "Bearer " prefix)
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid Authorization header format"
            ));
        }
        
        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
        
        authService.logout(token);
        
        return ResponseEntity.ok(Map.of(
            "message", "Logout successful"
        ));
    }
}
