package com.example.minishop.service;

import com.example.minishop.constant.OutboxEventType;
import com.example.minishop.constant.OutboxStatus;
import com.example.minishop.entity.OutboxEvent;
import com.example.minishop.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
public class OutboxService {

    private static final int DEFAULT_MAX_RETRIES = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void addEvent(
            OutboxEventType eventType,
            String aggregateType,
            Long aggregateId,
            Object payload
    ) {
        OutboxEvent event = new OutboxEvent();

        event.setEventType(eventType);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setPayload(toJson(payload));

        event.setStatus(OutboxStatus.PENDING);
        event.setRetryCount(0);
        event.setMaxRetries(DEFAULT_MAX_RETRIES);
        event.setCreatedAt(LocalDateTime.now());
        event.setNextRetryAt(LocalDateTime.now());

        outboxEventRepository.save(event);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Không thể chuyển payload Outbox thành JSON",
                    e
            );
        }
    }
}