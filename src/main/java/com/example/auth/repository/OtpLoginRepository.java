package com.example.auth.repository;

import com.example.auth.entity.OtpLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpLoginRepository extends JpaRepository<OtpLogin, Long> {
    Optional<OtpLogin> findTopByMobileNumberAndOtpCodeOrderByCreatedAtDesc(String mobileNumber, String otpCode);
    Optional<OtpLogin> findTopByMobileNumberOrderByCreatedAtDesc(String mobileNumber);
}
