package com.app.auth.repository;

import com.app.auth.entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {
    
    @Query("SELECT ds FROM DoctorSlot ds WHERE ds.doctorId = :doctorId AND ds.slotDate = :date AND ds.isAvailable = true ORDER BY ds.startTime")
    List<DoctorSlot> findAvailableSlotsByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    @Query("SELECT ds FROM DoctorSlot ds WHERE ds.doctorId = :doctorId AND ds.slotDate BETWEEN :fromDate AND :toDate AND ds.isAvailable = true ORDER BY ds.slotDate, ds.startTime")
    List<DoctorSlot> findAvailableSlotsByDoctorAndDateRange(@Param("doctorId") Long doctorId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
    
    @Query("SELECT ds FROM DoctorSlot ds WHERE ds.doctorId = :doctorId AND ds.slotDate = :date ORDER BY ds.startTime")
    List<DoctorSlot> findSlotsByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    @Query("SELECT ds FROM DoctorSlot ds WHERE ds.doctorId = :doctorId AND ds.workplace.id = :workplaceId AND ds.slotDate = :date AND ds.isAvailable = true ORDER BY ds.startTime")
    List<DoctorSlot> findAvailableSlotsByDoctorWorkplaceAndDate(@Param("doctorId") Long doctorId, @Param("workplaceId") Long workplaceId, @Param("date") LocalDate date);
}
