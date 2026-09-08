package com.example.minishop.service;

import com.example.minishop.constant.NotificationType;
import com.example.minishop.dto.response.NotificationResponse;
import com.example.minishop.entity.Notification;
import com.example.minishop.entity.User;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.NotificationMapper;
import com.example.minishop.repository.NotificationRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void create(
            User user,
            String title,
            String content,
            NotificationType type
    ) {
        Notification notification = new Notification();

        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        Long userId = SecurityUtils.getCurrentUserId();

        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        Long userId = SecurityUtils.getCurrentUserId();

        return notificationRepository.countByUser_IdAndReadStatusFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Notification notification = notificationRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));

        notification.setReadStatus(true);

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();

        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void createIfAbsent(
            String eventKey,
            User user,
            String title,
            String content,
            NotificationType type
    ) {
        if (notificationRepository.existsByEventKey(eventKey)) {
            return;
        }

        Notification notification = new Notification();

        notification.setEventKey(eventKey);
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }
}