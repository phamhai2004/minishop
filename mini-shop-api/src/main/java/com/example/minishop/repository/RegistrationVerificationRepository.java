package com.example.minishop.repository;

import com.example.minishop.entity.RegistrationVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationVerificationRepository
        extends JpaRepository<RegistrationVerification, Long> {

    Optional<RegistrationVerification> findByToken(String token);

    Optional<RegistrationVerification> findByEmail(String email);

    void deleteByEmail(String email);
}