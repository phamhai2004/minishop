package com.example.minishop.controller.seller;

import com.example.minishop.constant.RevenuePeriod;
import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.service.SellerDashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/dashboard")
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;

    public SellerDashboardController(
            SellerDashboardService dashboardService
    ) {

        this.dashboardService = dashboardService;

    }

    @GetMapping
    public ResponseEntity<?> dashboard(){

        return ResponseEntity.ok(

                ApiResponse.success(

                        dashboardService.dashboard()

                )

        );

    }

    @GetMapping("/products")
    public ResponseEntity<?> products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getProductAnalytics(
                                keyword,
                                PageRequest.of(page, size)
                        )
                )
        );
    }

    @GetMapping("/products/never-sold")
    public ResponseEntity<?> productsNeverSold() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getProductsNeverSold()
                )
        );
    }

    @GetMapping("/products/slow-moving")
    public ResponseEntity<?> slowMovingProducts() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getSlowMovingProducts()
                )
        );
    }

    @GetMapping("/products/top-revenue")
    public ResponseEntity<?> topProductsByRevenue() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getTopProductsByRevenue()
                )
        );
    }

    @GetMapping("/customers")
    public ResponseEntity<?> customers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String customerType
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getCustomerAnalytics(
                                keyword,
                                customerType,
                                PageRequest.of(page, size)
                        )
                )
        );
    }

    @GetMapping("/performance")
    public ResponseEntity<?> performance() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getPerformance()
                )
        );
    }

    @GetMapping("/performance/products")
    public ResponseEntity<?> productPerformance() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboardService.getProductPerformance()
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