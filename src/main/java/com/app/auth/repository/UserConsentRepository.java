package com.app.auth.repository;

import com.app.auth.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    List<UserConsent> findByUserIdOrderByConsentTimestampDesc(Long userId);

    @Query("SELECT uc FROM UserConsent uc WHERE uc.userId = :userId AND uc.consentType = :type " +
           "AND uc.withdrawnAt IS NULL ORDER BY uc.consentTimestamp DESC")
    Optional<UserConsent> findActiveConsent(@Param("userId") Long userId, @Param("type") String type);

    @Query("SELECT uc FROM UserConsent uc WHERE uc.userId = :userId AND uc.withdrawnAt IS NULL")
    List<UserConsent> findActiveConsentsByUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserConsent uc WHERE uc.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
