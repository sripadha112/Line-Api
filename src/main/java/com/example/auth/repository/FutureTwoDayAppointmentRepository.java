package com.example.auth.repository;

import com.example.auth.entity.FutureTwoDayAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FutureTwoDayAppointmentRepository extends JpaRepository<FutureTwoDayAppointment, Long> {
    
    List<FutureTwoDayAppointment> findByUserIdOrderByAppointmentTime(@Param("userId") Long userId);
    
    List<FutureTwoDayAppointment> findByDoctorIdOrderByAppointmentTime(@Param("doctorId") Long doctorId);
    
    @Query("SELECT fa FROM FutureTwoDayAppointment fa WHERE fa.doctorId = :doctorId AND fa.workplaceId = :workplaceId")
    List<FutureTwoDayAppointment> findByDoctorIdAndWorkplaceId(@Param("doctorId") Long doctorId, @Param("workplaceId") Long workplaceId);
    
    @Query("SELECT fa FROM FutureTwoDayAppointment fa WHERE fa.appointmentDate = :date")
    List<FutureTwoDayAppointment> findByAppointmentDate(@Param("date") String date);
    
    @Query("SELECT fa FROM FutureTwoDayAppointment fa WHERE fa.doctorId = :doctorId AND fa.workplaceId = :workplaceId AND fa.appointmentDate = :date")
    List<FutureTwoDayAppointment> findByDoctorIdAndWorkplaceIdAndAppointmentDate(@Param("doctorId") Long doctorId, @Param("workplaceId") Long workplaceId, @Param("date") String date);
    
    @Query("SELECT fa FROM FutureTwoDayAppointment fa WHERE fa.doctorId = :doctorId AND fa.workplaceId = :workplaceId AND fa.slot = :slot AND fa.appointmentDate = :date")
    List<FutureTwoDayAppointment> findByDoctorIdAndWorkplaceIdAndSlotAndAppointmentDate(@Param("doctorId") Long doctorId, @Param("workplaceId") Long workplaceId, @Param("slot") String slot, @Param("date") String date);
    
    /**
     * Count future appointments for queue position calculation
     */
    long countByDoctorIdAndWorkplaceIdAndAppointmentDate(@Param("doctorId") Long doctorId, @Param("workplaceId") Long workplaceId, @Param("appointmentDate") String appointmentDate);
    
    /**
     * Find future appointments by workplace and date - for workspace appointments API
     */
    @Query("SELECT fa FROM FutureTwoDayAppointment fa WHERE fa.workplaceId = :workplaceId AND fa.appointmentDate = :appointmentDate ORDER BY fa.appointmentTime ASC")
    List<FutureTwoDayAppointment> findByWorkplaceIdAndAppointmentDate(@Param("workplaceId") Long workplaceId, @Param("appointmentDate") String appointmentDate);
    
    /**
     * Find all future appointments by workplace - for workspace appointments API (all dates)
     */
    @Query("SELECT fa FROM FutureTwoDayAppointment fa WHERE fa.workplaceId = :workplaceId ORDER BY fa.appointmentDate ASC, fa.appointmentTime ASC")
    List<FutureTwoDayAppointment> findByWorkplaceIdOrderByAppointmentDateAndTime(@Param("workplaceId") Long workplaceId);
}
