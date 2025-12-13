package com.app.auth.repository;

import com.app.auth.entity.DoctorDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<DoctorDetails, Long> {

    @Query("SELECT d FROM DoctorDetails d WHERE " +
            "LOWER(TRIM(d.fullName)) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%')) OR " +
            "LOWER(TRIM(d.specialization)) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%')) OR " +
            "LOWER(TRIM(d.designation)) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%')) OR " +
            "LOWER(TRIM(d.address)) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%')) OR " +
            "LOWER(TRIM(d.city)) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%'))")
    List<DoctorDetails> searchDoctors(@Param("keyword") String keyword);
    
    // Method to get all doctors for debugging
    @Query("SELECT d FROM DoctorDetails d")
    List<DoctorDetails> findAllDoctors();
    
    // Enhanced search methods - name search
    @Query("SELECT d FROM DoctorDetails d WHERE " +
           "LOWER(TRIM(d.fullName)) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%'))")
    List<DoctorDetails> findByNameContaining(@Param("keyword") String keyword);
    
    @Query("SELECT d FROM DoctorDetails d WHERE d.id = :doctorId")
    DoctorDetails findByDoctorId(@Param("doctorId") Long doctorId);
}
