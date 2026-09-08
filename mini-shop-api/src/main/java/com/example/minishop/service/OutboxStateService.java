package com.example.minishop.service;

import com.example.minishop.constant.OutboxStatus;
import com.example.minishop.entity.OutboxEvent;
import com.example.minishop.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OutboxStateService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxStateService(
            OutboxEventRepository outboxEventRepository
    ) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessed(Long eventId) {

        OutboxEvent event = outboxEventRepository
                .findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy OutboxEvent: " + eventId
                ));

        event.setStatus(OutboxStatus.PROCESSED);
        event.setProcessedAt(LocalDateTime.now());
        event.setLastError(null);
        event.setLockedAt(null);
        event.setLockedBy(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(
            Long eventId,
            Exception exception
    ) {

        OutboxEvent event = outboxEventRepository
                .findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy OutboxEvent: " + eventId
                ));

        int nextRetryCount =
                event.getRetryCount() + 1;

        event.setRetryCount(nextRetryCount);

        event.setLastError(
                truncate(
                        exception.getMessage(),
                        2000
                )
        );

        event.setLockedAt(null);
        event.setLockedBy(null);

        if (nextRetryCount >= event.getMaxRetries()) {

            event.setStatus(OutboxStatus.FAILED);

            return;
        }

        event.setStatus(OutboxStatus.PENDING);

        event.setNextRetryAt(
                LocalDateTime.now()
                        .plusSeconds(
                                calculateDelay(nextRetryCount)
                        )
        );
    }

    private long calculateDelay(int retryCount) {

        return Math.min(
                300,
                (long) Math.pow(2, retryCount) * 5
        );
    }

    private String truncate(
            String value,
            int maxLength
    ) {

        if (value == null) {
            return null;
        }

        return value.length() <= maxLength
                ? value
                : value.substring(0, maxLength);
    }
}