package com.app.auth.service.impl;

import com.app.auth.config.JwtUtil;
import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.OtpRequestDto;
import com.app.auth.dto.OtpVerifyRequestDto;
import com.app.auth.dto.OtpVerifySimpleDto;
import com.app.auth.dto.Role;
import com.app.auth.entity.DoctorDetails;
import com.app.auth.entity.OtpLogin;
import com.app.auth.entity.UserDetails;
import com.app.auth.repository.DoctorDetailsRepository;
import com.app.auth.repository.OtpLoginRepository;
import com.app.auth.repository.UserDetailsRepository;
import com.app.auth.service.AuthService;
import com.app.auth.service.OtpService;
import com.app.auth.service.TokenBlacklistService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {

    private final OtpLoginRepository otpRepo;
    private final UserDetailsRepository userRepo;
    private final DoctorDetailsRepository doctorRepo;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final OtpService otpService;

    public AuthServiceImpl(OtpLoginRepository otpRepo, UserDetailsRepository userRepo,
                           DoctorDetailsRepository doctorRepo, JwtUtil jwtUtil,
                           TokenBlacklistService tokenBlacklistService, OtpService otpService) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.doctorRepo = doctorRepo;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.otpService = otpService;
    }

    @Override
    @Transactional
    public void requestOtp(OtpRequestDto request) {
        System.out.println("[DEBUG] Starting OTP request for mobile: " + request.getMobileNumber());
        
        // generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        System.out.println("[DEBUG] Generated OTP: " + otp);
        
        OtpLogin o = new OtpLogin();
//        o.setMobileNumber(request.getMobileNumber());
        o.setMobileNumber(request.getMobileNumber());
        o.setOtpCode(otp);
        o.setUsed(false);
        o.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        
        System.out.println("[DEBUG] About to save OTP to database");
        otpRepo.save(o);
        System.out.println("[DEBUG] OTP saved to database successfully");

        // Send OTP via SMS using OtpService
        try {
            // TESTING: Redirect all OTP messages to specific test number
            // boolean sentSuccessfully = otpService.sendOtp(request.getMobileNumber(), otp);
            String testMobileNumber = "8790672731"; // TODO: Remove after testing
            boolean sentSuccessfully = otpService.sendOtp(testMobileNumber, otp);
            if (sentSuccessfully) {
                System.out.println("[DEBUG] OTP sent successfully via SMS to test number: " + testMobileNumber + " (Original: " + request.getMobileNumber() + ")");
            } else {
                System.out.println("[DEBUG] OTP SMS sending failed, but OTP was logged to console");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to send OTP via SMS: " + e.getMessage());
            // OTP is already saved to database, so user can still proceed with verification
            // The OtpService will handle console fallback in case of SMS failure
        }
        
        System.out.println("[DEBUG] OTP request completed successfully");
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequestDto request) {
        Optional<OtpLogin> maybe = otpRepo.findTopByMobileNumberAndOtpCodeOrderByCreatedAtDesc(request.getMobileNumber(), request.getOtpCode());
        if (!maybe.isPresent()) throw new IllegalArgumentException("Invalid OTP");
        OtpLogin otp = maybe.get();
        if (otp.isUsed() || otp.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("OTP expired or already used");
        }
        otp.setUsed(true);
        otpRepo.save(otp);

        // check existing user or doctor
        Optional<UserDetails> maybeUser = userRepo.findByMobileNumber(request.getMobileNumber());
        if (maybeUser.isPresent()) {
            UserDetails u = maybeUser.get();
            String token = jwtUtil.generateToken("USER:" + u.getId());
            return new AuthResponse("LOGGED_IN", Role.USER, u.getId(), u.getFullName(), u.getEmail(), u.getMobileNumber(), token);
        }

        Optional<DoctorDetails> maybeDoc = doctorRepo.findByMobileNumber(request.getMobileNumber());
        if (maybeDoc.isPresent()) {
            DoctorDetails d = maybeDoc.get();
            String token = jwtUtil.generateToken("DOCTOR:" + d.getId());
            return new AuthResponse("LOGGED_IN", Role.DOCTOR, d.getId(), d.getFullName(), d.getEmail(), d.getMobileNumber(), token);
        }

        // not found -> register based on role in request
        if (request.getRole() == null) throw new IllegalArgumentException("Role is required for new registration");

        if (request.getRole() == Role.USER) {
            UserDetails u = new UserDetails();
            u.setFullName(request.getFullName());
            u.setEmail(request.getEmail());
            u.setMobileNumber(request.getMobileNumber());
            u.setAddress(request.getAddress());
            UserDetails saved = userRepo.save(u);
            String token = jwtUtil.generateToken("USER:" + saved.getId());
            return new AuthResponse("REGISTERED_AND_LOGGED_IN", Role.USER, saved.getId(), saved.getFullName(), saved.getEmail(), saved.getMobileNumber(), token);
        } else {
            DoctorDetails d = new DoctorDetails();
            d.setFullName(request.getFullName());
            d.setEmail(request.getEmail());
            d.setMobileNumber(request.getMobileNumber());
            d.setAddress(request.getAddress());
            d.setDesignation(request.getDesignation());
            d.setSpecialization(request.getSpecialization());
            d.setPincode(request.getPincode());
            DoctorDetails saved = doctorRepo.save(d);
            String token = jwtUtil.generateToken("DOCTOR:" + saved.getId());
            return new AuthResponse("REGISTERED_AND_LOGGED_IN", Role.DOCTOR, saved.getId(), saved.getFullName(), saved.getEmail(), saved.getMobileNumber(), token);
        }
    }

    @Override
    @Transactional
    public AuthResponse verifyOtpSimple(OtpVerifySimpleDto request) {
        // Verify OTP
        Optional<OtpLogin> maybe = otpRepo.findTopByMobileNumberAndOtpCodeOrderByCreatedAtDesc(request.getMobileNumber(), request.getOtpCode());
        if (!maybe.isPresent()) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        
        OtpLogin otp = maybe.get();
        if (otp.isUsed() || otp.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("OTP expired or already used");
        }
        
        // Mark OTP as used
        otp.setUsed(true);
        otpRepo.save(otp);

        // Check existing user
        Optional<UserDetails> maybeUser = userRepo.findByMobileNumber(request.getMobileNumber());
        if (maybeUser.isPresent()) {
            UserDetails u = maybeUser.get();
            String token = jwtUtil.generateToken("USER:" + u.getId());
            return new AuthResponse("LOGGED_IN", Role.USER, u.getId(), u.getFullName(), u.getEmail(), u.getMobileNumber(), token);
        }

        // Check existing doctor
        Optional<DoctorDetails> maybeDoc = doctorRepo.findByMobileNumber(request.getMobileNumber());
        if (maybeDoc.isPresent()) {
            DoctorDetails d = maybeDoc.get();
            String token = jwtUtil.generateToken("DOCTOR:" + d.getId());
            return new AuthResponse("LOGGED_IN", Role.DOCTOR, d.getId(), d.getFullName(), d.getEmail(), d.getMobileNumber(), token);
        }

        // User not found - they need to register first
        throw new IllegalArgumentException("User not found. Please register first.");
    }

    @Override
    public void logout(String token) {
        System.out.println("[DEBUG] Logout requested for token");
        
        // Validate token before blacklisting
        try {
            jwtUtil.parseToken(token);
            tokenBlacklistService.blacklistToken(token);
            System.out.println("[DEBUG] Token successfully blacklisted");
        } catch (Exception e) {
            System.out.println("[DEBUG] Invalid token provided for logout: " + e.getMessage());
            throw new IllegalArgumentException("Invalid token");
        }
    }

    @Override
    public AuthResponse verifyMobile(String mobileNumber) {
        // Check existing user
        Optional<UserDetails> maybeUser = userRepo.findByMobileNumber(mobileNumber);
        if (maybeUser.isPresent()) {
            UserDetails u = maybeUser.get();
            String token = jwtUtil.generateToken("USER:" + u.getId());
            return new AuthResponse("LOGGED_IN", Role.USER, u.getId(), u.getFullName(), u.getEmail(), u.getMobileNumber(), token);
        }

        // Check existing doctor
        Optional<DoctorDetails> maybeDoc = doctorRepo.findByMobileNumber(mobileNumber);
        if (maybeDoc.isPresent()) {
            DoctorDetails d = maybeDoc.get();
            String token = jwtUtil.generateToken("DOCTOR:" + d.getId());
            return new AuthResponse("LOGGED_IN", Role.DOCTOR, d.getId(), d.getFullName(), d.getEmail(), d.getMobileNumber(), token);
        }

        throw new IllegalArgumentException("User not found. Please register first.");
    }
}
