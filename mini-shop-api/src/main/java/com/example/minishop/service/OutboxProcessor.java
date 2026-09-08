package com.example.minishop.service;

import com.example.minishop.constant.OutboxStatus;
import com.example.minishop.entity.OutboxEvent;
import com.example.minishop.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxProcessor.class);

    private static final int BATCH_SIZE = 20;
    private static final int STALE_MINUTES = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxWorkerService outboxWorkerService;

    public OutboxProcessor(
            OutboxEventRepository outboxEventRepository,
            OutboxWorkerService outboxWorkerService
    ) {
        this.outboxEventRepository =
                outboxEventRepository;

        this.outboxWorkerService =
                outboxWorkerService;
    }

    @Scheduled(
            fixedDelayString =
                    "${outbox.poll-delay-ms:5000}"
    )
    public void processEvents() {

        //        recoverStaleEvents();

        List<OutboxEvent> events =
                outboxEventRepository.findReadyEvents(
                        OutboxStatus.PENDING,
                        LocalDateTime.now(),
                        PageRequest.of(
                                0,
                                BATCH_SIZE
                        )
                );

        for (OutboxEvent event : events) {

            try {
                outboxWorkerService.processOne(event.getId());

            } catch (Exception exception) {

                log.error(
                        "Xử lý OutboxEvent {} thất bại",
                        event.getId(),
                        exception
                );
            }
        }
    }

    protected void recoverStaleEvents() {

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime staleBefore =
                now.minusMinutes(
                        STALE_MINUTES
                );

        outboxEventRepository
                .recoverStaleProcessingEvents(
                        OutboxStatus.PROCESSING,
                        OutboxStatus.PENDING,
                        staleBefore,
                        now
                );
    }
}