package com.app.auth.service;

import com.app.auth.config.JwtUtil;
import com.app.auth.dto.AuthDtos.CheckMobileRequest;
import com.app.auth.dto.AuthDtos.CheckMobileResponse;
import com.app.auth.dto.AuthDtos.AuthResponse;
import com.app.auth.dto.AuthDtos.LoginRequest;
import com.app.auth.dto.AuthDtos.RegisterRequest;
import com.app.auth.entity.UserDetails;
import com.app.auth.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserDetailsRepository userRepo;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Check if a mobile number is already registered.
     * Frontend uses this to decide: show "Set PIN" (register) vs "Enter PIN" (login)
     */
    public CheckMobileResponse checkMobile(CheckMobileRequest req) {
        boolean exists = userRepo.findByMobileNumber(req.getMobileNumber()).isPresent();
        return new CheckMobileResponse(exists);
    }

    /**
     * Register a new user with mobile number + PIN.
     * Creates a minimal row in user_details — full profile (name, medical info, etc.)
     * gets filled in later via the RoleSelection / profile-completion screen.
     */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.findByMobileNumber(req.getMobileNumber()).isPresent()) {
            throw new IllegalStateException("Mobile number already registered. Please login instead.");
        }

        UserDetails user = new UserDetails();
        user.setMobileNumber(req.getMobileNumber());
        user.setPinHash(passwordEncoder.encode(req.getPin()));
        user.setRole(req.getRole() != null ? req.getRole().toUpperCase() : "USER");

        // full_name is NOT NULL in your table — set a placeholder until
        // the user completes their profile in RoleSelection
        user.setFullName(req.getFullName() != null && !req.getFullName().isBlank()
                ? req.getFullName()
                : "Pending");

        user.setProfileCompleted(req.getFullName() != null && !req.getFullName().isBlank());

        userRepo.save(user);

        String token = jwtUtil.generateToken(user.getMobileNumber(), user.getId(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getRole(), user.getFullName(), user.getMobileNumber());
    }

    /**
     * Login with mobile number + PIN
     */
    @Transactional
    public AuthResponse login(LoginRequest req) {
        UserDetails user = userRepo.findByMobileNumber(req.getMobileNumber())
                .orElseThrow(() -> new IllegalStateException("USER_NOT_FOUND"));

        if (user.getPinHash() == null) {
            // Edge case: user exists from old flow without a PIN set
            throw new IllegalStateException("PIN_NOT_SET");
        }

        if (!passwordEncoder.matches(req.getPin(), user.getPinHash())) {
            throw new IllegalArgumentException("Incorrect PIN");
        }

        String token = jwtUtil.generateToken(user.getMobileNumber(), user.getId(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getRole(), user.getFullName(), user.getMobileNumber());
    }
}