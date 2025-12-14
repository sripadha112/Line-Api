package com.app.auth.repository;

import com.app.auth.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

    Optional<UserDetails> findByMobileNumber(String mobileNumber);

    Optional<UserDetails> findByEmail(String email);
}
