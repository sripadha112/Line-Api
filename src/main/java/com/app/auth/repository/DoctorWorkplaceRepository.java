package com.app.auth.repository;

import com.app.auth.entity.DoctorWorkplace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorWorkplaceRepository extends JpaRepository<DoctorWorkplace, Long> {
    
    @Query("SELECT dw FROM DoctorWorkplace dw WHERE dw.doctor.id = :doctorId")
    List<DoctorWorkplace> findByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT dw FROM DoctorWorkplace dw WHERE dw.doctor.id = :doctorId AND dw.workplaceType = :type")
    List<DoctorWorkplace> findByDoctorIdAndWorkplaceType(@Param("doctorId") Long doctorId, @Param("type") String type);
    
    @Query("SELECT dw FROM DoctorWorkplace dw WHERE dw.doctor.id = :doctorId AND dw.isPrimary = true")
    DoctorWorkplace findPrimaryWorkplaceByDoctorId(@Param("doctorId") Long doctorId);
    
    // Enhanced search methods
    @Query("SELECT DISTINCT dw FROM DoctorWorkplace dw JOIN dw.doctor d WHERE " +
           "LOWER(dw.workplaceName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DoctorWorkplace> findByWorkplaceNameContaining(@Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT dw FROM DoctorWorkplace dw JOIN dw.doctor d WHERE " +
           "LOWER(dw.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dw.state) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "dw.pincode = :keyword")
    List<DoctorWorkplace> findByAreaOrPincode(@Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT dw FROM DoctorWorkplace dw WHERE dw.pincode = :pincode")
    List<DoctorWorkplace> findByPincode(@Param("pincode") String pincode);
}
