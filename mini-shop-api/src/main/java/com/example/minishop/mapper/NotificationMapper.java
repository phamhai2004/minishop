package com.example.minishop.mapper;

import com.example.minishop.dto.response.NotificationResponse;
import com.example.minishop.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();

        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setType(notification.getType());
        response.setReadStatus(notification.getReadStatus());
        response.setCreatedAt(notification.getCreatedAt());

        return response;
    }
}