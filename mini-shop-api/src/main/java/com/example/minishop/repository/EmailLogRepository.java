package com.example.minishop.repository;

import com.example.minishop.constant.EmailStatus;
import com.example.minishop.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailLogRepository
        extends JpaRepository<EmailLog, Long> {

    Optional<EmailLog> findByEventKey(String eventKey);

    boolean existsByEventKeyAndStatus(
            String eventKey,
            EmailStatus status
    );
}