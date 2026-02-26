package com.app.auth.repository;

import com.app.auth.entity.BlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, Long> {

    // Find all active blocked slots for a doctor on a specific date
    @Query("SELECT b FROM BlockedSlot b WHERE b.doctorId = :doctorId AND b.blockDate = :date AND b.isActive = true")
    List<BlockedSlot> findActiveBlockedSlotsByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Find all active blocked slots for a doctor and workplace on a specific date
    @Query("SELECT b FROM BlockedSlot b WHERE b.doctorId = :doctorId " +
           "AND (b.workplaceId = :workplaceId OR b.workplaceId IS NULL) " +
           "AND b.blockDate = :date AND b.isActive = true")
    List<BlockedSlot> findActiveBlockedSlotsByDoctorWorkplaceAndDate(
            @Param("doctorId") Long doctorId, 
            @Param("workplaceId") Long workplaceId, 
            @Param("date") LocalDate date);

    // Find all active blocked slots for a doctor within date range
    @Query("SELECT b FROM BlockedSlot b WHERE b.doctorId = :doctorId " +
           "AND b.blockDate BETWEEN :fromDate AND :toDate AND b.isActive = true")
    List<BlockedSlot> findActiveBlockedSlotsByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // Find all active blocked slots for a workplace within date range
    @Query("SELECT b FROM BlockedSlot b WHERE " +
           "(b.workplaceId = :workplaceId OR b.workplaceId IS NULL) " +
           "AND b.doctorId = :doctorId " +
           "AND b.blockDate BETWEEN :fromDate AND :toDate AND b.isActive = true")
    List<BlockedSlot> findActiveBlockedSlotsByWorkplaceAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("workplaceId") Long workplaceId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // Check if a specific time is blocked for a doctor and workplace
    @Query("SELECT b FROM BlockedSlot b WHERE b.doctorId = :doctorId " +
           "AND (b.workplaceId = :workplaceId OR b.workplaceId IS NULL) " +
           "AND b.blockDate = :date AND b.isActive = true " +
           "AND (b.isFullDay = true OR (b.startTime <= :time AND b.endTime > :time))")
    List<BlockedSlot> findBlockedSlotsForTime(
            @Param("doctorId") Long doctorId,
            @Param("workplaceId") Long workplaceId,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time);

    // Check if entire day is blocked for a doctor (any workspace or all)
    @Query("SELECT b FROM BlockedSlot b WHERE b.doctorId = :doctorId " +
           "AND b.blockDate = :date AND b.isFullDay = true AND b.isActive = true " +
           "AND (b.workplaceId = :workplaceId OR b.workplaceId IS NULL)")
    List<BlockedSlot> findFullDayBlocksForWorkplace(
            @Param("doctorId") Long doctorId,
            @Param("workplaceId") Long workplaceId,
            @Param("date") LocalDate date);

    // Find all active blocks for a doctor
    @Query("SELECT b FROM BlockedSlot b WHERE b.doctorId = :doctorId AND b.isActive = true ORDER BY b.blockDate DESC")
    List<BlockedSlot> findAllActiveBlocksByDoctor(@Param("doctorId") Long doctorId);

    // Find all active blocks for a workplace
    @Query("SELECT b FROM BlockedSlot b WHERE b.workplaceId = :workplaceId AND b.isActive = true ORDER BY b.blockDate DESC")
    List<BlockedSlot> findAllActiveBlocksByWorkplace(@Param("workplaceId") Long workplaceId);
}
