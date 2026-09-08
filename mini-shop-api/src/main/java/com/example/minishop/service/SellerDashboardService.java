package com.example.minishop.service;

import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.constant.RevenuePeriod;
import com.example.minishop.constant.ShopOrderStatus;
import com.example.minishop.dto.response.RevenueChartPoint;
import com.example.minishop.dto.response.SellerDashboardResponse;
import com.example.minishop.dto.response.SellerPerformanceResponse;
import com.example.minishop.dto.response.SellerProductPerformanceResponse;
import com.example.minishop.entity.Shop;
import com.example.minishop.projection.NeverSoldProduct;
import com.example.minishop.projection.SellerCustomerAnalytics;
import com.example.minishop.projection.SellerProductAnalytics;
import com.example.minishop.projection.SlowMovingProduct;
import com.example.minishop.repository.OrderRepository;
import com.example.minishop.repository.ProductRepository;
import com.example.minishop.repository.ShopOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SellerDashboardService {

    private final ShopService shopService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ShopOrderRepository shopOrderRepository;
    private static final int LOW_STOCK_MIN = 1;
    private static final int LOW_STOCK_MAX = 5;

    public SellerDashboardService(
            ShopService shopService,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            ShopOrderRepository shopOrderRepository
    ) {

        this.shopService = shopService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.shopOrderRepository = shopOrderRepository;

    }

    @Transactional(readOnly = true)
    public SellerDashboardResponse dashboard(){

        Shop shop =
                shopService.getCurrentSellerShop();



        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfMonth =
                now.toLocalDate()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        SellerDashboardResponse response =
                new SellerDashboardResponse();

        response.setShopName(
                shop.getName()
        );

        response.setCodOrders(
                shopOrderRepository
                        .countDashboardOrdersByShopAndPaymentMethod(
                                shop.getId(),
                                PaymentMethod.COD,
                                PaymentMethod.COD,
                                PaymentMethod.VNPAY,
                                PaymentStatus.PAID
                        )
        );

        response.setVnpayOrders(
                shopOrderRepository
                        .countDashboardOrdersByShopAndPaymentMethod(
                                shop.getId(),
                                PaymentMethod.VNPAY,
                                PaymentMethod.COD,
                                PaymentMethod.VNPAY,
                                PaymentStatus.PAID
                        )
        );

        response.setAverageOrderValue(
                shopOrderRepository
                        .calculateSellerAverageOrderValue(
                                shop.getId(),
                                PaymentStatus.PAID
                        )
        );

        response.setTotalProducts(
                productRepository.countByShop_Id(
                        shop.getId()
                )
        );

        response.setInStockProducts(
                productRepository.countInStockProductsByShop(
                        shop.getId(),
                        LOW_STOCK_MAX
                )
        );

        response.setLowStockProducts(
                productRepository.countLowStockProductsByShop(
                        shop.getId(),
                        LOW_STOCK_MIN,
                        LOW_STOCK_MAX
                )
        );

        response.setOutOfStockProducts(
                productRepository.countOutOfStockProductsByShop(
                        shop.getId()
                )
        );

        long totalOrders =
                shopOrderRepository.countDashboardOrdersByShop(
                        shop.getId(),
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                );

        response.setTotalOrders(totalOrders);

        response.setRevenue(
                shopOrderRepository.calculateDashboardRevenue(
                        shop.getId(),
                        PaymentStatus.PAID
                )
        );
        response.setPendingOrders(
                shopOrderRepository.countDashboardOrdersByShopAndStatus(
                        shop.getId(),
                        ShopOrderStatus.PENDING,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                )
        );

        response.setConfirmedOrders(
                shopOrderRepository.countDashboardOrdersByShopAndStatus(
                        shop.getId(),
                        ShopOrderStatus.CONFIRMED,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                )
        );

        response.setPackingOrders(
                shopOrderRepository.countDashboardOrdersByShopAndStatus(
                        shop.getId(),
                        ShopOrderStatus.PACKING,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                )
        );

        response.setShippingOrders(
                shopOrderRepository.countDashboardOrdersByShopAndStatus(
                        shop.getId(),
                        ShopOrderStatus.SHIPPING,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                )
        );

        response.setDeliveredOrders(
                shopOrderRepository.countDashboardOrdersByShopAndStatus(
                        shop.getId(),
                        ShopOrderStatus.DELIVERED,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                )
        );

        long completedOrders =
                shopOrderRepository.countDashboardOrdersByShopAndStatus(
                        shop.getId(),
                        ShopOrderStatus.COMPLETED,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                );

        response.setCompletedOrders(completedOrders);

        long cancelledOrders =
                shopOrderRepository.countDashboardOrdersByShopAndStatus(
                        shop.getId(),
                        ShopOrderStatus.CANCELLED,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                );

        response.setCancelledOrders(cancelledOrders);

        if (totalOrders > 0) {

            response.setCompletionRate(
                    (double) completedOrders
                            / totalOrders
                            * 100
            );

            response.setCancellationRate(
                    (double) cancelledOrders
                            / totalOrders
                            * 100
            );

        } else {

            response.setCompletionRate(0.0);
            response.setCancellationRate(0.0);
        }

        response.setTopProducts(
                shopOrderRepository.topProductsByShop(
                        shop.getId(),
                        PaymentStatus.PAID,
                        PageRequest.of(0, 5)
                )
        );

        response.setMonthlyRevenue(
                shopOrderRepository.monthlyRevenueByShop(
                        shop.getId(),
                        LocalDate.now().getYear(),
                        PaymentStatus.PAID
                )
        );

        response.setPaidOrders(
                shopOrderRepository.countByShop_IdAndPaymentStatus(
                        shop.getId(),
                        PaymentStatus.PAID
                )
        );

        response.setCustomersPurchased(
                shopOrderRepository.countCustomersPurchasedByShop(
                        shop.getId(),
                        ShopOrderStatus.CANCELLED,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                )
        );

        response.setNewCustomers(
                shopOrderRepository.countNewCustomersByShop(
                        shop.getId(),
                        startOfMonth,
                        now,
                        ShopOrderStatus.CANCELLED,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                )
        );

        response.setReturningCustomers(
                shopOrderRepository.findReturningCustomerIdsByShop(
                        shop.getId(),
                        ShopOrderStatus.CANCELLED,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                ).size()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public Page<SellerProductAnalytics> getProductAnalytics(
            String keyword,
            Pageable pageable
    ) {

        Shop shop =
                shopService.getCurrentSellerShop();

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        return productRepository.findSellerProductAnalytics(
                shop.getId(),
                normalizedKeyword,
                PaymentStatus.PAID,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<NeverSoldProduct> getProductsNeverSold() {

        Shop shop =
                shopService.getCurrentSellerShop();

        return productRepository
                .findSellerProductsNeverSold(
                        shop.getId(),
                        PaymentStatus.PAID
                );
    }

    @Transactional(readOnly = true)
    public List<SlowMovingProduct> getSlowMovingProducts() {

        Shop shop =
                shopService.getCurrentSellerShop();

        return productRepository
                .findSellerSlowMovingProducts(
                        shop.getId(),
                        PaymentStatus.PAID,
                        1L,
                        2L,
                        5L
                );
    }

    @Transactional(readOnly = true)
    public List<SellerProductAnalytics> getTopProductsByRevenue() {

        Shop shop =
                shopService.getCurrentSellerShop();

        return productRepository
                .findSellerTopProductsByRevenue(
                        shop.getId(),
                        PaymentStatus.PAID,
                        PageRequest.of(0, 5)
                );
    }

    @Transactional(readOnly = true)
    public Page<SellerCustomerAnalytics> getCustomerAnalytics(
            String keyword,
            String customerType,
            Pageable pageable
    ) {

        Shop shop =
                shopService.getCurrentSellerShop();

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        String normalizedType =
                customerType == null || customerType.isBlank()
                        ? null
                        : customerType.trim().toUpperCase();

        if ("NEW".equals(normalizedType)) {
            return shopOrderRepository
                    .findSellerNewCustomerAnalytics(
                            shop.getId(),
                            normalizedKeyword,
                            ShopOrderStatus.CANCELLED,
                            PaymentMethod.COD,
                            PaymentMethod.VNPAY,
                            PaymentStatus.PAID,
                            pageable
                    );
        }

        if ("RETURNING".equals(normalizedType)) {
            return shopOrderRepository
                    .findSellerReturningCustomerAnalytics(
                            shop.getId(),
                            normalizedKeyword,
                            ShopOrderStatus.CANCELLED,
                            PaymentMethod.COD,
                            PaymentMethod.VNPAY,
                            PaymentStatus.PAID,
                            pageable
                    );
        }

        return shopOrderRepository
                .findSellerCustomerAnalytics(
                        shop.getId(),
                        normalizedKeyword,
                        ShopOrderStatus.CANCELLED,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public SellerPerformanceResponse getPerformance() {

        Shop shop =
                shopService.getCurrentSellerShop();

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime currentMonthStart =
                now.toLocalDate()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        LocalDateTime nextMonthStart =
                currentMonthStart.plusMonths(1);

        LocalDateTime previousMonthStart =
                currentMonthStart.minusMonths(1);

        BigDecimal currentMonthRevenue =
                shopOrderRepository
                        .calculateRevenueByShopAndPaidAtBetween(
                                shop.getId(),
                                PaymentStatus.PAID,
                                currentMonthStart,
                                nextMonthStart
                        );

        BigDecimal previousMonthRevenue =
                shopOrderRepository
                        .calculateRevenueByShopAndPaidAtBetween(
                                shop.getId(),
                                PaymentStatus.PAID,
                                previousMonthStart,
                                currentMonthStart
                        );

        long currentMonthPaidOrders =
                shopOrderRepository
                        .countPaidOrdersByShopAndPaidAtBetween(
                                shop.getId(),
                                PaymentStatus.PAID,
                                currentMonthStart,
                                nextMonthStart
                        );

        long previousMonthPaidOrders =
                shopOrderRepository
                        .countPaidOrdersByShopAndPaidAtBetween(
                                shop.getId(),
                                PaymentStatus.PAID,
                                previousMonthStart,
                                currentMonthStart
                        );

        SellerPerformanceResponse response =
                new SellerPerformanceResponse();

        response.setCurrentMonthRevenue(
                currentMonthRevenue
        );

        response.setPreviousMonthRevenue(
                previousMonthRevenue
        );

        response.setCurrentMonthPaidOrders(
                currentMonthPaidOrders
        );

        response.setPreviousMonthPaidOrders(
                previousMonthPaidOrders
        );

        response.setRevenueGrowthRate(
                calculateGrowthRate(
                        previousMonthRevenue,
                        currentMonthRevenue
                )
        );

        response.setPaidOrderGrowthRate(
                calculateGrowthRate(
                        previousMonthPaidOrders,
                        currentMonthPaidOrders
                )
        );

        return response;
    }

    private double calculateGrowthRate(
            BigDecimal previous,
            BigDecimal current
    ) {

        if (previous == null
                || previous.compareTo(BigDecimal.ZERO) == 0) {

            return current != null
                    && current.compareTo(BigDecimal.ZERO) > 0
                    ? 100.0
                    : 0.0;
        }

        return current
                .subtract(previous)
                .divide(
                        previous,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .doubleValue();
    }

    private double calculateGrowthRate(
            long previous,
            long current
    ) {

        if (previous == 0) {
            return current > 0
                    ? 100.0
                    : 0.0;
        }

        return ((double) current - previous)
                / previous
                * 100;
    }

    @Transactional(readOnly = true)
    public List<SellerProductPerformanceResponse>
    getProductPerformance() {

        Shop shop =
                shopService.getCurrentSellerShop();

        BigDecimal totalRevenue =
                shopOrderRepository.calculateDashboardRevenue(
                        shop.getId(),
                        PaymentStatus.PAID
                );

        List<SellerProductAnalytics> products =
                productRepository
                        .findSellerTopProductsByRevenue(
                                shop.getId(),
                                PaymentStatus.PAID,
                                PageRequest.of(0, 100)
                        );

        return products.stream()
                .filter(product ->
                        product.getRevenue() != null
                                && product.getRevenue()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .map(product -> {

                    SellerProductPerformanceResponse item =
                            new SellerProductPerformanceResponse();

                    item.setProductId(
                            product.getProductId()
                    );

                    item.setProductName(
                            product.getProductName()
                    );

                    item.setSold(
                            product.getSold()
                    );

                    item.setRevenue(
                            product.getRevenue()
                    );

                    double revenueShare = 0.0;

                    if (totalRevenue != null
                            && totalRevenue.compareTo(
                            BigDecimal.ZERO
                    ) > 0) {

                        revenueShare =
                                product.getRevenue()
                                        .divide(
                                                totalRevenue,
                                                6,
                                                RoundingMode.HALF_UP
                                        )
                                        .multiply(
                                                BigDecimal.valueOf(100)
                                        )
                                        .doubleValue();
                    }

                    item.setRevenueShare(revenueShare);

                    return item;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RevenueChartPoint> getRevenueChart(
            RevenuePeriod period
    ) {

        Shop shop =
                shopService.getCurrentSellerShop();

        return switch (period) {
            case DAY -> getDailyRevenue(shop.getId());
            case WEEK -> getWeeklyRevenue(shop.getId());
            case MONTH -> getMonthlyRevenueChart(shop.getId());
            case YEAR -> getYearlyRevenue(shop.getId());
        };
    }

    private List<RevenueChartPoint> getDailyRevenue(
            Long shopId
    ) {

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
                            .calculateRevenueByShopAndPaidAtBetween(
                                    shopId,
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

    private List<RevenueChartPoint> getWeeklyRevenue(
            Long shopId
    ) {

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
                            .calculateRevenueByShopAndPaidAtBetween(
                                    shopId,
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

    private List<RevenueChartPoint> getMonthlyRevenueChart(
            Long shopId
    ) {

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
                            .calculateRevenueByShopAndPaidAtBetween(
                                    shopId,
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

    private List<RevenueChartPoint> getYearlyRevenue(
            Long shopId
    ) {

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
                            .calculateRevenueByShopAndPaidAtBetween(
                                    shopId,
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