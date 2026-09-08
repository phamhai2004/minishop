package com.example.minishop.service;

import com.example.minishop.constant.*;
import com.example.minishop.dto.response.DashboardResponse;
import com.example.minishop.dto.response.RevenueChartPoint;
import com.example.minishop.projection.AdminCategoryAnalytics;
import com.example.minishop.projection.AdminOrderAnalytics;
import com.example.minishop.projection.AdminProductAnalytics;
import com.example.minishop.projection.AdminUserAnalytics;
import com.example.minishop.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final CategoryRepository categoryRepository;
    private static final int LOW_STOCK_MIN = 1;
    private static final int LOW_STOCK_MAX = 5;

    public DashboardService(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            ShopOrderRepository shopOrderRepository,
            CategoryRepository categoryRepository
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.shopOrderRepository = shopOrderRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        DashboardResponse response =
                new DashboardResponse();

        response.setTotalUsers(
                userRepository.count()
        );

        response.setTotalCustomers(
                userRepository.countByRole(
                        Role.CUSTOMER
                )
        );

        response.setTotalSellers(
                userRepository.countByRole(
                        Role.SELLER
                )
        );

        response.setTotalAdmins(
                userRepository.countByRole(
                        Role.ADMIN
                )
        );

        response.setActiveUsers(
                userRepository.countByActive(true)
        );

        response.setInactiveUsers(
                userRepository.countByActive(false)
        );

        response.setVerifiedUsers(
                userRepository.countByEmailVerified(true)
        );

        response.setUnverifiedUsers(
                userRepository.countByEmailVerified(false)
        );

        response.setTotalProducts(
                productRepository.count()
        );

        response.setInStockProducts(
                productRepository.countInStockProducts(
                        LOW_STOCK_MAX
                )
        );

        response.setLowStockProducts(
                productRepository.countLowStockProducts(
                        LOW_STOCK_MIN,
                        LOW_STOCK_MAX
                )
        );

        response.setOutOfStockProducts(
                productRepository.countOutOfStockProducts()
        );

        response.setTotalOrders(
                orderRepository.count()
        );

        response.setRevenue(
                shopOrderRepository.calculateAdminRevenue(
                        PaymentStatus.PAID
                )
        );

        response.setPendingOrders(
                orderRepository.countByStatus(
                        OrderStatus.PENDING
                )
        );

        response.setWaitingPaymentOrders(
                orderRepository.countByStatus(
                        OrderStatus.WAITING_PAYMENT
                )
        );

        response.setPaidStatusOrders(
                orderRepository.countByStatus(
                        OrderStatus.PAID
                )
        );

        response.setConfirmedOrders(
                orderRepository.countByStatus(
                        OrderStatus.CONFIRMED
                )
        );

        response.setPackingOrders(
                orderRepository.countByStatus(
                        OrderStatus.PACKING
                )
        );

        response.setShippingOrders(
                orderRepository.countByStatus(
                        OrderStatus.SHIPPING
                )
        );

        response.setDeliveredOrders(
                orderRepository.countByStatus(
                        OrderStatus.DELIVERED
                )
        );

        response.setCompletedOrders(
                orderRepository.countByStatus(
                        OrderStatus.COMPLETED
                )
        );

        response.setCancelledOrders(
                orderRepository.countByStatus(
                        OrderStatus.CANCELLED
                )
        );

        response.setReturnedOrders(
                orderRepository.countByStatus(
                        OrderStatus.RETURNED
                )
        );

        response.setPaidOrders(
                orderRepository.countByPaymentStatus(
                        PaymentStatus.PAID
                )
        );

        response.setCodOrders(
                orderRepository.countByPaymentMethod(
                        PaymentMethod.COD
                )
        );

        response.setVnpayOrders(
                orderRepository.countByPaymentMethod(
                        PaymentMethod.VNPAY
                )
        );

        response.setPendingPaymentStatusOrders(
                orderRepository.countByPaymentStatus(
                        PaymentStatus.PENDING
                )
        );

        response.setProcessingPaymentOrders(
                orderRepository.countByPaymentStatus(
                        PaymentStatus.PROCESSING
                )
        );

        response.setFailedPaymentOrders(
                orderRepository.countByPaymentStatus(
                        PaymentStatus.FAILED
                )
        );

        response.setCancelledPaymentOrders(
                orderRepository.countByPaymentStatus(
                        PaymentStatus.CANCELLED
                )
        );

        response.setExpiredPaymentOrders(
                orderRepository.countByPaymentStatus(
                        PaymentStatus.EXPIRED
                )
        );

        response.setRefundedPaymentOrders(
                orderRepository.countByPaymentStatus(
                        PaymentStatus.REFUNDED
                )
        );

        response.setMonthlyRevenue(
                shopOrderRepository.adminMonthlyRevenue(
                        LocalDate.now().getYear(),
                        PaymentStatus.PAID
                )
        );

        response.setTopProducts(
                shopOrderRepository.adminTopProducts(
                        PaymentStatus.PAID,
                        PageRequest.of(0, 10)
                )
        );

        response.setTopProductsByRevenue(
                shopOrderRepository.adminTopProductsByRevenue(
                        PaymentStatus.PAID,
                        PageRequest.of(0, 10)
                )
        );

        response.setProductsNeverSold(
                productRepository.findProductsNeverSold(
                        PaymentStatus.PAID
                )
        );

        response.setSlowMovingProducts(
                productRepository.findSlowMovingProducts(
                        PaymentStatus.PAID,
                        1L,
                        2L,
                        LOW_STOCK_MAX
                )
        );

        response.setTopSellers(
                shopOrderRepository.topAdminSellers(
                        PaymentStatus.PAID,
                        PageRequest.of(0, 10)
                )
        );

        response.setAverageOrderValue(
                shopOrderRepository.calculateAdminAverageOrderValue(
                        PaymentStatus.PAID
                )
        );

        long totalOrders = response.getTotalOrders();

        if (totalOrders > 0) {
            response.setCompletionRate(
                    response.getCompletedOrders() * 100.0 / totalOrders
            );

            response.setCancellationRate(
                    response.getCancelledOrders() * 100.0 / totalOrders
            );

            response.setReturnRate(
                    response.getReturnedOrders() * 100.0 / totalOrders
            );
        } else {
            response.setCompletionRate(0);
            response.setCancellationRate(0);
            response.setReturnRate(0);
        }

        response.setUserAuthProviders(
                userRepository.countUsersByAuthProvider()
        );

        response.setMonthlyUserGrowth(
                userRepository.monthlyUserGrowth(
                        LocalDate.now().getYear()
                )
        );

        response.setTotalCategories(
                categoryRepository.count()
        );

        response.setCategoriesWithProducts(
                categoryRepository.countCategoriesWithProducts()
        );

        response.setEmptyCategories(
                categoryRepository.countEmptyCategories()
        );

        var topRevenue =
                categoryRepository.findTopCategoriesByRevenue(
                        PaymentStatus.PAID,
                        PageRequest.of(0, 1)
                );

        response.setTopCategoryByRevenue(
                topRevenue.isEmpty()
                        ? null
                        : topRevenue.get(0)
        );

        var topSold =
                categoryRepository.findTopCategoriesBySold(
                        PaymentStatus.PAID,
                        PageRequest.of(0, 1)
                );

        response.setTopCategoryBySold(
                topSold.isEmpty()
                        ? null
                        : topSold.get(0)
        );

        return response;
    }

    @Transactional(readOnly = true)
    public Page<AdminProductAnalytics> getProductAnalytics(
            Pageable pageable
    ) {
        return productRepository.findAdminProductAnalytics(
                PaymentStatus.PAID,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminOrderAnalytics> getOrderAnalytics(
            String keyword,
            OrderStatus orderStatus,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        return orderRepository.findAdminOrderAnalytics(
                normalizedKeyword,
                orderStatus,
                paymentMethod,
                paymentStatus,
                fromDate,
                toDate,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminUserAnalytics> getUserAnalytics(
            String keyword,
            Role role,
            Boolean active,
            Boolean emailVerified,
            AuthProvider authProvider,
            Pageable pageable
    ) {

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        return userRepository.findAdminUserAnalytics(
                normalizedKeyword,
                role,
                active,
                emailVerified,
                authProvider,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminCategoryAnalytics> getCategoryAnalytics(
            String keyword,
            Pageable pageable
    ) {

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        return categoryRepository.findAdminCategoryAnalytics(
                normalizedKeyword,
                PaymentStatus.PAID,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<RevenueChartPoint> getRevenueChart(
            RevenuePeriod period
    ) {

        return switch (period) {
            case DAY -> getDailyRevenue();
            case WEEK -> getWeeklyRevenue();
            case MONTH -> getMonthlyRevenueChart();
            case YEAR -> getYearlyRevenue();
        };
    }

    private List<RevenueChartPoint> getDailyRevenue() {

        List<RevenueChartPoint> result =
                new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {

            LocalDate date =
                    today.minusDays(i);

            LocalDateTime fromDate =
                    date.atStartOfDay();

            LocalDateTime toDate =
                    date.plusDays(1).atStartOfDay();

            BigDecimal revenue =
                    shopOrderRepository
                            .calculateAdminRevenueByPaidAtBetween(
                                    PaymentStatus.PAID,
                                    fromDate,
                                    toDate
                            );

            result.add(
                    new RevenueChartPoint(
                            date.format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM"
                                    )
                            ),
                            revenue
                    )
            );
        }

        return result;
    }

    private List<RevenueChartPoint> getWeeklyRevenue() {

        List<RevenueChartPoint> result =
                new ArrayList<>();

        LocalDate today = LocalDate.now();

        LocalDate currentWeekStart =
                today.with(DayOfWeek.MONDAY);

        for (int i = 7; i >= 0; i--) {

            LocalDate weekStart =
                    currentWeekStart.minusWeeks(i);

            LocalDate weekEnd =
                    weekStart.plusWeeks(1);

            BigDecimal revenue =
                    shopOrderRepository
                            .calculateAdminRevenueByPaidAtBetween(
                                    PaymentStatus.PAID,
                                    weekStart.atStartOfDay(),
                                    weekEnd.atStartOfDay()
                            );

            result.add(
                    new RevenueChartPoint(
                            weekStart.format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM"
                                    )
                            ),
                            revenue
                    )
            );
        }

        return result;
    }

    private List<RevenueChartPoint> getMonthlyRevenueChart() {

        List<RevenueChartPoint> result =
                new ArrayList<>();

        int currentYear =
                LocalDate.now().getYear();

        for (int month = 1; month <= 12; month++) {

            YearMonth yearMonth =
                    YearMonth.of(
                            currentYear,
                            month
                    );

            LocalDateTime fromDate =
                    yearMonth
                            .atDay(1)
                            .atStartOfDay();

            LocalDateTime toDate =
                    yearMonth
                            .plusMonths(1)
                            .atDay(1)
                            .atStartOfDay();

            BigDecimal revenue =
                    shopOrderRepository
                            .calculateAdminRevenueByPaidAtBetween(
                                    PaymentStatus.PAID,
                                    fromDate,
                                    toDate
                            );

            result.add(
                    new RevenueChartPoint(
                            "T" + month,
                            revenue
                    )
            );
        }

        return result;
    }

    private List<RevenueChartPoint> getYearlyRevenue() {

        List<RevenueChartPoint> result =
                new ArrayList<>();

        int currentYear =
                LocalDate.now().getYear();

        for (int year = currentYear - 4;
             year <= currentYear;
             year++) {

            LocalDateTime fromDate =
                    LocalDate
                            .of(year, 1, 1)
                            .atStartOfDay();

            LocalDateTime toDate =
                    LocalDate
                            .of(year + 1, 1, 1)
                            .atStartOfDay();

            BigDecimal revenue =
                    shopOrderRepository
                            .calculateAdminRevenueByPaidAtBetween(
                                    PaymentStatus.PAID,
                                    fromDate,
                                    toDate
                            );

            result.add(
                    new RevenueChartPoint(
                            String.valueOf(year),
                            revenue
                    )
            );
        }

        return result;
    }
}