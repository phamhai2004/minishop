package com.example.minishop.service;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.constant.OutboxEventType;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.constant.ShopOrderStatus;
import com.example.minishop.entity.Order;
import com.example.minishop.entity.OrderItem;
import com.example.minishop.entity.ShopOrder;
import com.example.minishop.event.ProductStockChangedEvent;
import com.example.minishop.event.payload.OrderOutboxPayload;
import com.example.minishop.exception.BadRequestException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class OrderCancellationService {

    private final StockService stockService;
    private final OutboxService outboxService;
    private final ApplicationEventPublisher eventPublisher;
    private final FlashSalePurchaseService flashSalePurchaseService;

    public OrderCancellationService(
            StockService stockService,
            OutboxService outboxService,
            ApplicationEventPublisher eventPublisher,
            FlashSalePurchaseService flashSalePurchaseService
    ) {
        this.stockService = stockService;
        this.outboxService = outboxService;
        this.eventPublisher = eventPublisher;
        this.flashSalePurchaseService = flashSalePurchaseService;
    }

    @Transactional
    public void cancel(Order order) {

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException(
                    "Không thể hủy đơn hàng đã hoàn thành"
            );
        }

        Set<Long> restoredProductIds = new HashSet<>();
        LocalDateTime cancelledAt = LocalDateTime.now();

        for (ShopOrder shopOrder : order.getShopOrders()) {

            if (shopOrder.getStatus()
                    == ShopOrderStatus.CANCELLED) {
                continue;
            }

            for (OrderItem item : shopOrder.getItems()) {

                stockService.restoreStock(
                        item.getProduct(),
                        item.getVariant(),
                        item.getQuantity(),
                        order
                );

                if (item.getFlashSale() != null) {
                    flashSalePurchaseService.restore(
                            item.getFlashSale().getId(),
                            item.getQuantity()
                    );
                }

                restoredProductIds.add(
                        item.getProduct().getId()
                );
            }

            shopOrder.setStatus(
                    ShopOrderStatus.CANCELLED
            );

            shopOrder.setCancelledAt(cancelledAt);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.CANCELLED);
        order.setCancelledAt(cancelledAt);

        outboxService.addEvent(
                OutboxEventType.ORDER_CANCELLED,
                "ORDER",
                order.getId(),
                new OrderOutboxPayload(order.getId())
        );

        if (!restoredProductIds.isEmpty()) {
            eventPublisher.publishEvent(
                    new ProductStockChangedEvent(
                            restoredProductIds
                    )
            );
        }
    }
}