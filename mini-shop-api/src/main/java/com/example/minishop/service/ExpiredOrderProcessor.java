package com.example.minishop.service;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.entity.Order;
import com.example.minishop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ExpiredOrderProcessor {

    private final OrderRepository orderRepository;
    private final OrderCancellationService
            orderCancellationService;

    public ExpiredOrderProcessor(
            OrderRepository orderRepository,
            OrderCancellationService
                    orderCancellationService
    ) {
        this.orderRepository = orderRepository;
        this.orderCancellationService =
                orderCancellationService;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public boolean cancelIfExpired(
            Long orderId,
            LocalDateTime expiredBefore
    ) {
        Order order = orderRepository
                .findByIdForUpdate(orderId)
                .orElse(null);

        if (order == null) {
            return false;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            return false;
        }

        if (order.getPaymentStatus()
                != PaymentStatus.PENDING) {
            return false;
        }

        if (order.getPaymentMethod() != PaymentMethod.VNPAY) {
            return false;
        }

        if (order.getCreatedAt() == null
                || !order.getCreatedAt()
                .isBefore(expiredBefore)) {
            return false;
        }

        orderCancellationService.cancel(order);

        order.setPaymentStatus(
                PaymentStatus.EXPIRED
        );

        return true;
    }
}