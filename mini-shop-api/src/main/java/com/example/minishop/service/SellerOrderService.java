package com.example.minishop.service;

import com.example.minishop.constant.*;
import com.example.minishop.dto.response.SellerOrderResponse;
import com.example.minishop.entity.Order;
import com.example.minishop.entity.OrderItem;
import com.example.minishop.entity.Shop;
import com.example.minishop.entity.ShopOrder;
import com.example.minishop.event.payload.ShopOrderOutboxPayload;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.SellerOrderMapper;
import com.example.minishop.repository.ShopOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SellerOrderService {

    private final ShopOrderRepository shopOrderRepository;
    private final ShopService shopService;
    private final SellerOrderMapper sellerOrderMapper;
    private final StockService stockService;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxService outboxService;

    public SellerOrderService(
            ShopOrderRepository shopOrderRepository,
            ShopService shopService,
            SellerOrderMapper sellerOrderMapper,
            StockService stockService,
            ApplicationEventPublisher eventPublisher,
            OutboxService outboxService
    ) {
        this.shopOrderRepository = shopOrderRepository;
        this.shopService = shopService;
        this.sellerOrderMapper = sellerOrderMapper;
        this.stockService = stockService;
        this.eventPublisher = eventPublisher;
        this.outboxService = outboxService;
    }

    private ShopOrder getMyShopOrder(Long shopOrderId) {
        Shop shop = shopService.getCurrentSellerShop();

        return shopOrderRepository
                .findByIdAndShop_Id(
                        shopOrderId,
                        shop.getId(),
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn hàng thuộc shop của bạn"
                ));
    }

    private ShopOrder getActiveMyShopOrder(Long shopOrderId) {
        Shop shop = shopService.getActiveCurrentSellerShop();

        return findMyShopOrder(shopOrderId, shop);
    }

    private ShopOrder findMyShopOrder(
            Long shopOrderId,
            Shop shop
    ) {
        return shopOrderRepository
                .findByIdAndShop_Id(
                        shopOrderId,
                        shop.getId(),
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn hàng thuộc shop của bạn"
                ));
    }

    @Transactional(readOnly = true)
    public Page<SellerOrderResponse> getByStatus(
            ShopOrderStatus status,
            int page,
            int size,
            String timeRange
    ) {
        validatePagination(page, size);

        Shop shop = shopService.getCurrentSellerShop();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        LocalDateTime[] range = resolveTimeRange(timeRange);

        if (range == null) {
            return shopOrderRepository
                    .findByShop_IdAndStatus(
                            shop.getId(),
                            status,
                            PaymentMethod.COD,
                            PaymentMethod.VNPAY,
                            PaymentStatus.PAID,
                            pageable
                    )
                    .map(sellerOrderMapper::toResponse);
        }

        return shopOrderRepository
                .findByShop_IdAndStatusAndCreatedAtBetween(
                        shop.getId(),
                        status,
                        range[0],
                        range[1],
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID,
                        pageable
                )
                .map(sellerOrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SellerOrderResponse getById(Long shopOrderId) {
        return sellerOrderMapper.toResponse(
                getMyShopOrder(shopOrderId)
        );
    }


    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new BadRequestException(
                    "Page không được nhỏ hơn 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException(
                    "Size phải từ 1 đến 100"
            );
        }
    }

    private Sort.Direction parseDirection(String direction) {
        if ("asc".equalsIgnoreCase(direction)) {
            return Sort.Direction.ASC;
        }

        if ("desc".equalsIgnoreCase(direction)) {
            return Sort.Direction.DESC;
        }

        throw new BadRequestException(
                "Direction chỉ chấp nhận asc hoặc desc"
        );
    }

    private LocalDateTime[] resolveTimeRange(String timeRange) {
        LocalDateTime now = LocalDateTime.now();

        if (timeRange == null || timeRange.isBlank()
                || "ALL".equalsIgnoreCase(timeRange)) {
            return null;
        }

        return switch (timeRange.toUpperCase()) {
            case "TODAY" -> {
                LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
                yield new LocalDateTime[]{startOfDay, now};
            }

            case "LAST_7_DAYS" -> {
                LocalDateTime fromDate = now.minusDays(7);
                yield new LocalDateTime[]{fromDate, now};
            }

            case "LAST_30_DAYS" -> {
                LocalDateTime fromDate = now.minusDays(30);
                yield new LocalDateTime[]{fromDate, now};
            }

            case "LAST_3_MONTHS" -> {
                LocalDateTime fromDate = now.minusMonths(3);
                yield new LocalDateTime[]{fromDate, now};
            }

            case "LAST_6_MONTHS" -> {
                LocalDateTime fromDate = now.minusMonths(6);
                yield new LocalDateTime[]{fromDate, now};
            }

            default -> throw new BadRequestException(
                    "Time range không hợp lệ"
            );
        };
    }

    @Transactional
    public SellerOrderResponse confirm(Long shopOrderId) {
        ShopOrder shopOrder = getActiveMyShopOrder(shopOrderId);

        if (shopOrder.getStatus() != ShopOrderStatus.PENDING) {
            throw new BadRequestException(
                    "Chỉ đơn hàng đang chờ mới có thể xác nhận"
            );
        }

        shopOrder.setStatus(ShopOrderStatus.CONFIRMED);
        shopOrder.setConfirmedAt(LocalDateTime.now());

        updateParentOrderStatus(shopOrder.getOrder());

        outboxService.addEvent(
                OutboxEventType.SHOP_ORDER_CONFIRMED,
                "SHOP_ORDER",
                shopOrder.getId(),
                new ShopOrderOutboxPayload(shopOrder.getId())
        );

        return sellerOrderMapper.toResponse(shopOrder);
    }

    @Transactional
    public SellerOrderResponse startPacking(Long shopOrderId) {
        ShopOrder shopOrder = getActiveMyShopOrder(shopOrderId);

        if (shopOrder.getStatus() != ShopOrderStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Chỉ đơn hàng đã xác nhận mới được đóng gói"
            );
        }

        shopOrder.setStatus(ShopOrderStatus.PACKING);

        updateParentOrderStatus(shopOrder.getOrder());

        outboxService.addEvent(
                OutboxEventType.SHOP_ORDER_PACKING,
                "SHOP_ORDER",
                shopOrder.getId(),
                new ShopOrderOutboxPayload(shopOrder.getId())
        );

        return sellerOrderMapper.toResponse(shopOrder);
    }

    @Transactional
    public SellerOrderResponse startShipping(Long shopOrderId) {
        ShopOrder shopOrder = getActiveMyShopOrder(shopOrderId);

        if (shopOrder.getStatus() != ShopOrderStatus.PACKING) {
            throw new BadRequestException(
                    "Đơn hàng phải được đóng gói trước khi giao"
            );
        }

        shopOrder.setStatus(ShopOrderStatus.SHIPPING);
        shopOrder.setShippedAt(LocalDateTime.now());

        updateParentOrderStatus(shopOrder.getOrder());

        outboxService.addEvent(
                OutboxEventType.SHOP_ORDER_SHIPPING,
                "SHOP_ORDER",
                shopOrder.getId(),
                new ShopOrderOutboxPayload(shopOrder.getId())
        );

        return sellerOrderMapper.toResponse(shopOrder);
    }

    @Transactional
    public SellerOrderResponse markDelivered(Long shopOrderId) {
        ShopOrder shopOrder = getActiveMyShopOrder(shopOrderId);

        if (shopOrder.getStatus() != ShopOrderStatus.SHIPPING) {
            throw new BadRequestException(
                    "Chỉ đơn đang vận chuyển mới có thể xác nhận đã giao hàng"
            );
        }

        shopOrder.setStatus(ShopOrderStatus.DELIVERED);
        shopOrder.setDeliveredAt(LocalDateTime.now());

        updateParentOrderStatus(shopOrder.getOrder());

        outboxService.addEvent(
                OutboxEventType.SHOP_ORDER_DELIVERED,
                "SHOP_ORDER",
                shopOrder.getId(),
                new ShopOrderOutboxPayload(shopOrder.getId())
        );

        return sellerOrderMapper.toResponse(shopOrder);
    }

    private void updateParentOrderStatus(Order order) {

        boolean allCancelled = order.getShopOrders()
                .stream()
                .allMatch(shopOrder ->
                        shopOrder.getStatus() == ShopOrderStatus.CANCELLED
                );

        if (allCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
            return;
        }

        boolean allFinished = order.getShopOrders()
                .stream()
                .allMatch(shopOrder ->
                        shopOrder.getStatus() == ShopOrderStatus.COMPLETED
                                || shopOrder.getStatus() == ShopOrderStatus.CANCELLED
                );

        if (allFinished) {
            order.setStatus(OrderStatus.COMPLETED);
            return;
        }

        boolean anyShipping = order.getShopOrders()
                .stream()
                .anyMatch(shopOrder ->
                        shopOrder.getStatus() == ShopOrderStatus.SHIPPING
                );

        if (anyShipping) {
            order.setStatus(OrderStatus.SHIPPING);
            return;
        }

        boolean hasDelivered = order.getShopOrders()
                .stream()
                .anyMatch(shopOrder ->
                        shopOrder.getStatus()
                                == ShopOrderStatus.DELIVERED
                );

        boolean allDeliveredOrCancelled =
                order.getShopOrders()
                        .stream()
                        .allMatch(shopOrder ->
                                shopOrder.getStatus()
                                        == ShopOrderStatus.DELIVERED
                                        ||
                                        shopOrder.getStatus()
                                                == ShopOrderStatus.CANCELLED
                        );

        if (hasDelivered && allDeliveredOrCancelled) {

            order.setStatus(
                    OrderStatus.DELIVERED
            );

            return;
        }

        boolean nonePending = order.getShopOrders()
                .stream()
                .noneMatch(shopOrder ->
                        shopOrder.getStatus() == ShopOrderStatus.PENDING
                );

        if (nonePending) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
    }

    @Transactional
    public SellerOrderResponse cancel(
            Long shopOrderId,
            String reason
    ) {
        ShopOrder shopOrder = getActiveMyShopOrder(shopOrderId);

        if (shopOrder.getStatus() != ShopOrderStatus.PENDING
                && shopOrder.getStatus() != ShopOrderStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Không thể hủy đơn ở trạng thái hiện tại"
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new BadRequestException(
                    "Lý do hủy không được để trống"
            );
        }

        for (OrderItem item : shopOrder.getItems()) {
            stockService.restoreStock(
                    item.getProduct(),
                    item.getVariant(),
                    item.getQuantity(),
                    shopOrder.getOrder()
            );
        }

        shopOrder.setStatus(ShopOrderStatus.CANCELLED);
        shopOrder.setCancelledAt(LocalDateTime.now());
        shopOrder.setCancelReason(reason.trim());

        updateParentOrderStatus(shopOrder.getOrder());

        outboxService.addEvent(
                OutboxEventType.SHOP_ORDER_CANCELLED,
                "SHOP_ORDER",
                shopOrder.getId(),
                new ShopOrderOutboxPayload(shopOrder.getId())
        );

        return sellerOrderMapper.toResponse(shopOrder);
    }

    @Transactional(readOnly = true)
    public Page<SellerOrderResponse> getMyShopOrders(
            int page,
            int size,
            String direction,
            String timeRange
    ) {
        validatePagination(page, size);

        Shop shop = shopService.getCurrentSellerShop();

        Sort.Direction sortDirection =
                parseDirection(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, "createdAt")
        );

        LocalDateTime[] range = resolveTimeRange(timeRange);

        if (range == null) {
            return shopOrderRepository
                    .findByShop_Id(
                            shop.getId(),
                            PaymentMethod.COD,
                            PaymentMethod.VNPAY,
                            PaymentStatus.PAID,
                            pageable
                    )
                    .map(sellerOrderMapper::toResponse);
        }

        return shopOrderRepository
                .findByShop_IdAndCreatedAtBetween(
                        shop.getId(),
                        range[0],
                        range[1],
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID,
                        pageable
                )
                .map(sellerOrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SellerOrderResponse> searchOrders(
            String keyword,
            ShopOrderStatus status,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size,
            String direction
    ) {

        validatePagination(page, size);

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new BadRequestException(
                    "fromDate không được lớn hơn toDate"
            );
        }

        Shop shop =
                shopService.getCurrentSellerShop();

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        Sort.Direction sortDirection =
                parseDirection(direction);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                sortDirection,
                                "createdAt"
                        )
                );

        return shopOrderRepository
                .findSellerOrderAnalytics(
                        shop.getId(),
                        normalizedKeyword,
                        status,
                        paymentMethod,
                        paymentStatus,
                        fromDate,
                        toDate,
                        PaymentMethod.COD,
                        PaymentMethod.VNPAY,
                        PaymentStatus.PAID,
                        pageable
                )
                .map(sellerOrderMapper::toResponse);
    }
}