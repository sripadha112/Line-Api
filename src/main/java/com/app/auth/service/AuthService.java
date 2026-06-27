package com.app.auth.service;

import com.app.auth.config.JwtUtil;
import com.app.auth.config.QueryParamIdCrypto;
import com.app.auth.dto.AuthDtos.*;
import com.app.auth.entity.DoctorDetails;
import com.app.auth.entity.UserDetails;
import com.app.auth.repository.DoctorDetailsRepository;
import com.app.auth.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserDetailsRepository userRepo;

    @Autowired
    private DoctorDetailsRepository doctorRepo;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CheckMobileResponse checkMobile(CheckMobileRequest req) {

        // Check if user exists
        Optional<UserDetails> user = userRepo.findByMobileNumber(req.getMobileNumber());

        if (user.isPresent()) {
            UserDetails userDetails = user.get();
            boolean pinExists = userDetails.getPinHash() != null &&
                    !userDetails.getPinHash().isBlank();
            return new CheckMobileResponse(true, pinExists, userDetails.getId());
        }

        // Check if doctor exists
        Optional<DoctorDetails> doctor = doctorRepo.findByMobileNumber(req.getMobileNumber());

        if (doctor.isPresent()) {
            DoctorDetails doctorDetails = doctor.get();
            boolean pinExists = doctorDetails.getPinHash() != null &&
                    !doctorDetails.getPinHash().isBlank();
            // Return doctor's ID, not user's ID
            return new CheckMobileResponse(true, pinExists, doctorDetails.getId());
        }

        // Neither user nor doctor found
        return new CheckMobileResponse(false, false, null);
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.findByMobileNumber(req.getMobileNumber()).isPresent()) {
            throw new IllegalStateException("Mobile number already registered. Please login instead.");
        }

        UserDetails user = new UserDetails();
        user.setMobileNumber(req.getMobileNumber());
        user.setPinHash(passwordEncoder.encode(req.getPin()));
        user.setRole(req.getRole() != null ? req.getRole().toUpperCase() : "USER");
        user.setFullName(req.getFullName() != null && !req.getFullName().isBlank()
                ? req.getFullName()
                : "Pending");
        user.setProfileCompleted(req.getFullName() != null && !req.getFullName().isBlank());

        userRepo.save(user);

        String token = jwtUtil.generateToken(user.getMobileNumber(), user.getId(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getRole(), user.getFullName(), user.getMobileNumber());
    }

    /**
     * Login with mobile number + PIN.
     * Checks user_details first; if not found there, checks doctor_details.
     * Each branch is self-contained — no cross-referencing of `user`/`doctor`.
     */
    @Transactional
    public AuthResponse login(LoginRequest req) {
        String pin = QueryParamIdCrypto.decodeString(req.getPin(), "pin");
        if (!pin.matches("^[0-9]{4,6}$")) {
            throw new IllegalArgumentException("PIN must be 4-6 digits");
        }

        // ── Try USER table first ─────────────────────────────────────────
        Optional<UserDetails> userOpt = userRepo.findByMobileNumber(req.getMobileNumber());
        if (userOpt.isPresent()) {
            UserDetails user = userOpt.get();

            if (user.getPinHash() == null) {
                throw new IllegalStateException("PIN_NOT_SET");
            }
            if (!passwordEncoder.matches(pin, user.getPinHash())) {
                throw new IllegalArgumentException("Incorrect PIN");
            }

            String token = jwtUtil.generateToken(user.getMobileNumber(), user.getId(), user.getRole());
            return new AuthResponse(token, user.getId(), user.getRole(), user.getFullName(), user.getMobileNumber());
        }

        // ── Try DOCTOR table ─────────────────────────────────────────────
        Optional<DoctorDetails> doctorOpt = doctorRepo.findByMobileNumber(req.getMobileNumber());
        if (doctorOpt.isPresent()) {
            DoctorDetails doctor = doctorOpt.get();

            if (doctor.getPinHash() == null) {
                throw new IllegalStateException("PIN_NOT_SET");
            }
            if (!passwordEncoder.matches(pin, doctor.getPinHash())) {
                throw new IllegalArgumentException("Incorrect PIN");
            }

            String token = jwtUtil.generateToken(doctor.getMobileNumber(), doctor.getId(), doctor.getRole());
            return new AuthResponse(token, doctor.getId(), doctor.getRole(), doctor.getFullName(), doctor.getMobileNumber());
        }

        throw new IllegalStateException("USER_NOT_FOUND");
    }

    @Transactional
    public String setPin(String pin, Long id) {
        // Validate input
        if (pin == null || pin.isEmpty()) {
            return "PIN cannot be empty";
        }
        if (id == null || id <= 0) {
            return "Invalid ID";
        }
        if (!pin.matches("^[0-9]{4,6}$")) {
            return "PIN must be 4-6 digits";
        }

        String encodedPin = passwordEncoder.encode(pin);

        // Check User
        var user = userRepo.findById(id);
        if (user.isPresent()) {
            UserDetails userDetails = user.get();
            if (userDetails.getPinHash() != null && !userDetails.getPinHash().isEmpty()) {
                return "PIN already exists";
            }
            userDetails.setPinHash(encodedPin);
            userDetails.setUpdatedAt(java.time.OffsetDateTime.now());
            userRepo.save(userDetails);
            return "success";
        }

        // Check Doctor (NEW!)
        var doctor = doctorRepo.findById(id);
        if (doctor.isPresent()) {
            DoctorDetails doctorDetails = doctor.get();
            if (doctorDetails.getPinHash() != null && !doctorDetails.getPinHash().isEmpty()) {
                return "PIN already exists";
            }
            doctorDetails.setPinHash(encodedPin);
            doctorRepo.save(doctorDetails);
            return "success";
        }

        return "User/Doctor not found";
    }
}
