package com.example.minishop.service;

import com.example.minishop.entity.OutboxEvent;
import com.example.minishop.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OutboxWorkerService {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxWorkerService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventHandler outboxEventHandler;
    private final OutboxClaimService outboxClaimService;
    private final OutboxStateService outboxStateService;

    private final String workerId =
            UUID.randomUUID().toString();

    public OutboxWorkerService(
            OutboxEventRepository outboxEventRepository,
            OutboxEventHandler outboxEventHandler,
            OutboxClaimService outboxClaimService,
            OutboxStateService outboxStateService
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxEventHandler = outboxEventHandler;
        this.outboxClaimService = outboxClaimService;
        this.outboxStateService = outboxStateService;
    }

    public void processOne(Long eventId) {

        boolean claimed =
                outboxClaimService.claim(
                        eventId,
                        workerId
                );

        if (!claimed) {
            return;
        }

        try {

            OutboxEvent event =
                    outboxEventRepository
                            .findById(eventId)
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "Không tìm thấy OutboxEvent: "
                                                    + eventId
                                    )
                            );

            log.debug(
                    "Bắt đầu xử lý OutboxEvent id={}, type={}, aggregateId={}",
                    event.getId(),
                    event.getEventType(),
                    event.getAggregateId()
            );

            outboxEventHandler.handle(event);
            outboxStateService.markProcessed(eventId);

            log.info(
                    "OutboxEvent {} đã xử lý thành công",
                    eventId
            );

        } catch (Exception exception) {

            log.error(
                    "Xử lý OutboxEvent {} thất bại",
                    eventId,
                    exception
            );
            try {

                outboxStateService.markFailed(
                        eventId,
                        exception
                );

            } catch (Exception stateException) {

                log.error(
                        "Không thể cập nhật trạng thái retry cho OutboxEvent {}",
                        eventId,
                        stateException
                );
            }
        }
    }
}