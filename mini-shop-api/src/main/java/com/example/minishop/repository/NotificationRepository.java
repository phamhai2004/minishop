package com.example.minishop.repository;

import com.example.minishop.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndReadStatusFalse(Long userId);

    Optional<Notification> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByEventKey(String eventKey);

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.readStatus = true
        WHERE n.user.id = :userId
          AND n.readStatus = false
    """)
    int markAllAsReadByUserId(@Param("userId") Long userId);
}