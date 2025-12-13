package com.app.auth.repository;

import com.app.auth.entity.PastAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PastAppointmentRepository extends JpaRepository<PastAppointment, Long> {
    
    List<PastAppointment> findByUserIdOrderByAppointmentTimeDesc(@Param("userId") Long userId);
    
    List<PastAppointment> findByDoctorIdOrderByAppointmentTimeDesc(@Param("doctorId") Long doctorId);
    
    @Query("SELECT pa FROM PastAppointment pa WHERE pa.userId = :userId AND pa.appointmentDate >= :fromDate ORDER BY pa.appointmentTime DESC")
    List<PastAppointment> findByUserIdAndAppointmentDateAfter(@Param("userId") Long userId, @Param("fromDate") String fromDate);
}
