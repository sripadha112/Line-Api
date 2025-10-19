package com.example.auth.repository;

import com.example.auth.entity.Appointment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // For calendar: find appointments between dates for a doctor
    List<Appointment> findByDoctorIdAndAppointmentTimeBetweenOrderByAppointmentTime(@Param("doctorId") Long doctorId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    // For history (past)
    List<Appointment> findByDoctorIdAndAppointmentTimeBetweenOrderByAppointmentTimeDesc(@Param("doctorId") Long doctorId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    // Count for queue position
    long countByDoctorIdAndAppointmentTimeBetweenAndStatus(@Param("doctorId") Long doctorId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to, @Param("status") String status);

    // Last appointment of the day
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentTime >= :dayStart AND a.appointmentTime < :dayEnd ORDER BY a.appointmentTime DESC")
    List<Appointment> findLastForDoctorBetween(@Param("doctorId") Long doctorId,
                                               @Param("dayStart") OffsetDateTime dayStart,
                                               @Param("dayEnd") OffsetDateTime dayEnd,
                                               Pageable pageable);

    // Locking fetch for booking/reschedule to prevent races
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentTime >= :from AND a.appointmentTime < :to ORDER BY a.appointmentTime")
    List<Appointment> lockAppointmentsForDoctorBetween(@Param("doctorId") Long doctorId,
                                                       @Param("from") OffsetDateTime from,
                                                       @Param("to") OffsetDateTime to);

    // lock single appointment by id for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.id = :id")
    Optional<Appointment> lockById(@Param("id") Long id);

    List<Appointment> findByDoctorIdAndAppointmentTimeBetweenAndStatusOrderByAppointmentTime(@Param("doctorId") Long doctorId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to, @Param("status") String status);

    List<Appointment> findByUserIdOrderByAppointmentTimeDesc(@Param("userId") Long userId);

    // Find appointments between dates for daily status
    List<Appointment> findByAppointmentTimeBetween(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    
    // Find pending appointments (BOOKED or RESCHEDULED status) between dates
    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime BETWEEN :from AND :to AND (a.status = 'BOOKED' OR a.status = 'RESCHEDULED')")
    List<Appointment> findPendingAppointmentsBetween(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    
    // Find completed or cancelled appointments (for history)
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentTime BETWEEN :from AND :to AND (a.status = 'COMPLETED' OR a.status = 'CANCELLED') ORDER BY a.appointmentTime DESC")
    List<Appointment> findHistoryByDoctorIdAndAppointmentTimeBetween(@Param("doctorId") Long doctorId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    
    // Find appointments by workplace
    @Query("SELECT a FROM Appointment a WHERE a.workplaceId = :workplaceId AND a.appointmentTime BETWEEN :from AND :to ORDER BY a.appointmentTime")
    List<Appointment> findByWorkplaceIdAndAppointmentTimeBetweenOrderByAppointmentTime(@Param("workplaceId") Long workplaceId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    
    // Find appointments by workplace type (for segregation)
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.workplaceType = :workplaceType AND a.appointmentTime BETWEEN :from AND :to ORDER BY a.appointmentTime")
    List<Appointment> findByDoctorIdAndWorkplaceTypeAndAppointmentTimeBetween(@Param("doctorId") Long doctorId, @Param("workplaceType") String workplaceType, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    
    // New methods for enhanced appointment system
    List<Appointment> findByUserIdAndAppointmentDateOrderByAppointmentTime(@Param("userId") Long userId, @Param("appointmentDate") String appointmentDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date")
    List<Appointment> findByAppointmentDate(@Param("date") String date);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.workplaceId = :workplaceId AND a.appointmentDate = :date")
    List<Appointment> findByDoctorIdAndWorkplaceIdAndAppointmentDate(@Param("doctorId") Long doctorId, @Param("workplaceId") Long workplaceId, @Param("date") String date);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.workplaceId = :workplaceId AND a.slot = :slot AND a.appointmentDate = :date")
    List<Appointment> findByDoctorIdAndWorkplaceIdAndSlotAndAppointmentDate(@Param("doctorId") Long doctorId, @Param("workplaceId") Long workplaceId, @Param("slot") String slot, @Param("date") String date);
    
    // ==================== NEW DOCTOR MANAGEMENT REPOSITORY METHODS ====================
    
    /**
     * Find all appointments for a doctor on a specific date, sorted by appointment time
     * For doctor appointment management screen
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDate = :appointmentDate ORDER BY a.appointmentTime ASC")
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(@Param("doctorId") Long doctorId, @Param("appointmentDate") String appointmentDate);
    
    /**
     * Find appointments for specific users on a specific date for bulk operations
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDate = :appointmentDate AND a.userId IN :userIds")
    List<Appointment> findByDoctorIdAndAppointmentDateAndUserIdIn(@Param("doctorId") Long doctorId, @Param("appointmentDate") String appointmentDate, @Param("userIds") List<Long> userIds);
    
    /**
     * Count appointments for queue position calculation
     */
    long countByDoctorIdAndWorkplaceIdAndAppointmentDate(@Param("doctorId") Long doctorId, @Param("workplaceId") Long workplaceId, @Param("appointmentDate") String appointmentDate);
    
    /**
     * Count appointments by doctor and date
     */
    long countByDoctorIdAndAppointmentDate(@Param("doctorId") Long doctorId, @Param("appointmentDate") String appointmentDate);
    
    /**
     * Find appointments by workplace and date - for workspace appointments API
     */
    @Query("SELECT a FROM Appointment a WHERE a.workplaceId = :workplaceId AND a.appointmentDate = :appointmentDate ORDER BY a.appointmentTime ASC")
    List<Appointment> findByWorkplaceIdAndAppointmentDate(@Param("workplaceId") Long workplaceId, @Param("appointmentDate") String appointmentDate);
    
    /**
     * Find all appointments by workplace - for workspace appointments API (all dates)
     */
    @Query("SELECT a FROM Appointment a WHERE a.workplaceId = :workplaceId")
    List<Appointment> findByWorkplaceIdOrderByAppointmentDateAndTime(@Param("workplaceId") Long workplaceId);

    // Native function call to increase_time_range using PostgreSQL array literal
    @Modifying
    @Transactional
    @Query(value = "SELECT increase_time_range(CAST(:ids AS int[]), CAST(:add_interval AS text))", nativeQuery = true)
    void increaseTimeRangeNative(@Param("ids") String ids, @Param("add_interval") String addInterval);

    @Modifying
    @Transactional
    @Query(value = "SELECT increase_time_range(CAST(:ids AS int[]), CAST(:add_interval AS text), CAST(:new_date AS date))", nativeQuery = true)
    void increaseTimeRangeNativeWithDate(@Param("ids") String ids, @Param("add_interval") String addInterval, @Param("new_date") String newDate);
}
