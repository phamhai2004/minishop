package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.CreateOrderRequest;
import com.example.minishop.dto.response.CustomerShopOrderResponse;
import com.example.minishop.dto.response.OrderResponse;
import com.example.minishop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService
    ) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response =
                orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Đặt hàng thành công",
                                response
                        )
                );
    }

    @GetMapping("/my")
    public ResponseEntity<
            ApiResponse<
                    List<CustomerShopOrderResponse>
                    >
            > getMyOrders() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getMyShopOrders()
                )
        );
    }

    @PatchMapping("/{orderCode}/confirm-received")
    public ResponseEntity<
            ApiResponse<CustomerShopOrderResponse>
            > confirmReceived(
            @PathVariable String orderCode
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xác nhận đã nhận hàng thành công",
                        orderService.confirmReceived(
                                orderCode
                        )
                )
        );
    }

    @PatchMapping("/{orderCode}/cancel")
    public ResponseEntity<
            ApiResponse<CustomerShopOrderResponse>
            > cancelOrder(
            @PathVariable String orderCode
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hủy đơn hàng thành công",
                        orderService.cancelOrder(
                                orderCode
                        )
                )
        );
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<
            ApiResponse<CustomerShopOrderResponse>
            > getByOrderCode(
            @PathVariable String orderCode
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService
                                .getMyShopOrderByCodeResponse(
                                        orderCode
                                )
                )
        );
    }
}