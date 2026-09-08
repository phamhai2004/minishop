package com.example.minishop.controller.admin;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.UpdateOrderStatusRequest;
import com.example.minishop.dto.response.OrderResponse;
import com.example.minishop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(
            OrderService orderService
    ) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getAll(page, size)
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật trạng thái đơn hàng thành công",
                        orderService.updateStatus(id, request)
                )
        );
    }
}