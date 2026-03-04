package com.app.auth.repository;

import com.app.auth.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {
    
    List<Prescription> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    List<Prescription> findByDoctorIdOrderByCreatedAtDesc(Integer doctorId);
    
    List<Prescription> findByAppointmentId(Integer appointmentId);
}
