package com.app.auth.repository;

import com.app.auth.entity.DoctorDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorDetailsRepository extends JpaRepository<DoctorDetails, Long> {
    Optional<DoctorDetails> findByMobileNumber(String mobileNumber);
    Optional<DoctorDetails> findByEmail(String email);
}
