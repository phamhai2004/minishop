package com.example.minishop.controller.admin;

import com.example.minishop.constant.*;
import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<?> dashboard(){

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getDashboard()
                )
        );

    }

    @GetMapping("/products")
    public ResponseEntity<?> products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getProductAnalytics(
                                PageRequest.of(page, size)
                        )
                )
        );
    }

    @GetMapping("/orders")
    public ResponseEntity<?> orders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            OrderStatus orderStatus,

            @RequestParam(required = false)
            PaymentMethod paymentMethod,

            @RequestParam(required = false)
            PaymentStatus paymentStatus,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getOrderAnalytics(
                                keyword,
                                orderStatus,
                                paymentMethod,
                                paymentStatus,
                                fromDate,
                                toDate,
                                PageRequest.of(page, size)
                        )
                )
        );
    }

    @GetMapping("/users")
    public ResponseEntity<?> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Role role,

            @RequestParam(required = false)
            Boolean active,

            @RequestParam(required = false)
            AuthProvider authProvider,

            @RequestParam(required = false)
            Boolean emailVerified

    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getUserAnalytics(
                                keyword,
                                role,
                                active,
                                emailVerified,
                                authProvider,
                                PageRequest.of(page, size)
                        )
                )
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<?> categories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,

            @RequestParam(required = false)
            String keyword
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getCategoryAnalytics(
                                keyword,
                                PageRequest.of(page, size)
                        )
                )
        );
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> revenue(
            @RequestParam(
                    defaultValue = "MONTH"
            )
            RevenuePeriod period
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getRevenueChart(
                                period
                        )
                )
        );
    }

}