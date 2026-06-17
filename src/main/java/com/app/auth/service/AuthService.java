package com.app.auth.service;

import com.app.auth.config.JwtUtil;
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

//    public CheckMobileResponse checkMobile(CheckMobileRequest req) {
//        boolean exists = userRepo.findByMobileNumber(req.getMobileNumber()).isPresent();
//        if (!exists) {
//            exists = doctorRepo.findByMobileNumber(req.getMobileNumber()).isPresent();
//        }
//        return new CheckMobileResponse(exists);
//    }

    public CheckMobileResponse checkMobile(CheckMobileRequest req) {

        boolean mobileExists = false;
        boolean pinExists = false;

        Optional<UserDetails> user =
                userRepo.findByMobileNumber(req.getMobileNumber());

        if (user.isPresent()) {

            mobileExists = true;

            pinExists =
                    user.get().getPinHash() != null &&
                            !user.get().getPinHash().isBlank();

            return new CheckMobileResponse(mobileExists, pinExists, user.get().getId());
        }

        Optional<DoctorDetails> doctor =
                doctorRepo.findByMobileNumber(req.getMobileNumber());

        if (doctor.isPresent()) {

            mobileExists = true;

            pinExists =
                    doctor.get().getPinHash() != null &&
                            !doctor.get().getPinHash().isBlank();

            return new CheckMobileResponse(mobileExists, pinExists, user.get().getId());
        }

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

        // ── Try USER table first ─────────────────────────────────────────
        Optional<UserDetails> userOpt = userRepo.findByMobileNumber(req.getMobileNumber());
        if (userOpt.isPresent()) {
            UserDetails user = userOpt.get();

            if (user.getPinHash() == null) {
                throw new IllegalStateException("PIN_NOT_SET");
            }
            if (!passwordEncoder.matches(req.getPin(), user.getPinHash())) {
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
            if (!passwordEncoder.matches(req.getPin(), doctor.getPinHash())) {
                throw new IllegalArgumentException("Incorrect PIN");
            }

            String token = jwtUtil.generateToken(doctor.getMobileNumber(), doctor.getId(), doctor.getRole());
            return new AuthResponse(token, doctor.getId(), doctor.getRole(), doctor.getFullName(), doctor.getMobileNumber());
        }

        throw new IllegalStateException("USER_NOT_FOUND");
    }

    @Transactional
    public String setPin(String pin, Long id) {
        UserDetails user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPinHash() != null && !user.getPinHash().isEmpty()) {
            return "PIN already exists";
        }
        user.setPinHash(passwordEncoder.encode(pin));
        userRepo.save(user);

        return "Success";
    }
}