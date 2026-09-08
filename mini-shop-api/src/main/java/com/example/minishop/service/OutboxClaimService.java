package com.example.minishop.service;

import com.example.minishop.constant.OutboxStatus;
import com.example.minishop.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OutboxClaimService {

    private final OutboxEventRepository repository;

    public OutboxClaimService(
            OutboxEventRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(Long eventId, String workerId) {

        int updated = repository.tryLock(
                eventId,
                OutboxStatus.PENDING,
                OutboxStatus.PROCESSING,
                LocalDateTime.now(),
                workerId
        );

        return updated == 1;
    }
}