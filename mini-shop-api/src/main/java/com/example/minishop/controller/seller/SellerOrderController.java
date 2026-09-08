package com.example.minishop.controller.seller;

import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.constant.ShopOrderStatus;
import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.CancelShopOrderRequest;
import com.example.minishop.dto.response.SellerOrderResponse;
import com.example.minishop.service.SellerOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/seller/orders")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    public SellerOrderController(
            SellerOrderService sellerOrderService
    ) {
        this.sellerOrderService = sellerOrderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SellerOrderResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "ALL") String timeRange
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sellerOrderService.getMyShopOrders(
                                page,
                                size,
                                direction,
                                timeRange
                        )
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<SellerOrderResponse>>> getByStatus(
            @PathVariable ShopOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String timeRange
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sellerOrderService.getByStatus(
                                status,
                                page,
                                size,
                                timeRange
                        )
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sellerOrderService.getById(id)
                )
        );
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> confirm(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xác nhận đơn hàng thành công",
                        sellerOrderService.confirm(id)
                )
        );
    }

    @PatchMapping("/{id}/packing")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> packing(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Bắt đầu đóng gói đơn hàng",
                        sellerOrderService.startPacking(id)
                )
        );
    }

    @PatchMapping("/{id}/shipping")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> shipping(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đơn hàng đã chuyển sang vận chuyển",
                        sellerOrderService.startShipping(id)
                )
        );
    }

    @PatchMapping("/{id}/delivered")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> delivered(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đơn hàng đã được giao",
                        sellerOrderService.markDelivered(id)
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelShopOrderRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hủy đơn hàng thành công",
                        sellerOrderService.cancel(
                                id,
                                request.getReason()
                        )
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<SellerOrderResponse>>> search(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            ShopOrderStatus status,

            @RequestParam(required = false)
            PaymentMethod paymentMethod,

            @RequestParam(required = false)
            PaymentStatus paymentStatus,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sellerOrderService.searchOrders(
                                keyword,
                                status,
                                paymentMethod,
                                paymentStatus,
                                fromDate,
                                toDate,
                                page,
                                size,
                                direction
                        )
                )
        );
    }
}