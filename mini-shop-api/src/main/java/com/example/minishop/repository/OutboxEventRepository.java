package com.example.minishop.repository;

import com.example.minishop.constant.OutboxStatus;
import com.example.minishop.entity.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    @Query("""
        SELECT e
        FROM OutboxEvent e
        WHERE e.status = :status
          AND (
                e.nextRetryAt IS NULL
                OR e.nextRetryAt <= :now
              )
        ORDER BY e.createdAt ASC
    """)
    List<OutboxEvent> findReadyEvents(
            @Param("status") OutboxStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE OutboxEvent e
        SET e.status = :processingStatus,
            e.lockedAt = :lockedAt,
            e.lockedBy = :lockedBy
        WHERE e.id = :id
          AND e.status = :pendingStatus
    """)
    int tryLock(
            @Param("id") Long id,
            @Param("pendingStatus") OutboxStatus pendingStatus,
            @Param("processingStatus") OutboxStatus processingStatus,
            @Param("lockedAt") LocalDateTime lockedAt,
            @Param("lockedBy") String lockedBy
    );

    @Modifying
    @Query("""
        UPDATE OutboxEvent e
        SET e.status = :pendingStatus,
            e.lockedAt = NULL,
            e.lockedBy = NULL,
            e.nextRetryAt = :now
        WHERE e.status = :processingStatus
          AND e.lockedAt IS NOT NULL
          AND e.lockedAt < :staleBefore
    """)
    int recoverStaleProcessingEvents(
            @Param("processingStatus") OutboxStatus processingStatus,
            @Param("pendingStatus") OutboxStatus pendingStatus,
            @Param("staleBefore") LocalDateTime staleBefore,
            @Param("now") LocalDateTime now
    );
}