package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.response.NotificationResponse;
import com.example.minishop.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        return ResponseEntity.ok(
                ApiResponse.success(notificationService.getMyNotifications())
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnread() {
        return ResponseEntity.ok(
                ApiResponse.success(notificationService.countUnread())
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Đã đọc thông báo", notificationService.markAsRead(id))
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {

        notificationService.markAllAsRead();

        return ResponseEntity.ok(
                ApiResponse.success("Đã đọc tất cả thông báo", null)
        );
    }
}
