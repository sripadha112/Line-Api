package com.app.auth.repository;

import com.app.auth.entity.DoctorDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoctorDetailsRepository extends JpaRepository<DoctorDetails, Long> {
    Optional<DoctorDetails> findByMobileNumber(String mobileNumber);
    Optional<DoctorDetails> findByEmail(String email);
    
    @Query("SELECT d FROM DoctorDetails d LEFT JOIN FETCH d.workplaces WHERE d.id = :id")
    Optional<DoctorDetails> findByIdWithWorkplaces(@Param("id") Long id);
}
