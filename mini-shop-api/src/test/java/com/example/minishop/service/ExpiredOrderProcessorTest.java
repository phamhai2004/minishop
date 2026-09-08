package com.example.minishop.service;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.entity.Order;
import com.example.minishop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiredOrderProcessorTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderCancellationService orderCancellationService;

    private ExpiredOrderProcessor expiredOrderProcessor;

    private LocalDateTime expiredBefore;

    @BeforeEach
    void setUp() {
        expiredOrderProcessor =
                new ExpiredOrderProcessor(
                        orderRepository,
                        orderCancellationService
                );

        expiredBefore =
                LocalDateTime.of(
                        2026,
                        7,
                        25,
                        10,
                        0
                );
    }

    @Test
    void cancelIfExpired_shouldCancelExpiredPendingOrder() {
        Order order = createOrder(
                OrderStatus.PENDING,
                PaymentStatus.PENDING,
                expiredBefore.minusMinutes(1)
        );

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        boolean result =
                expiredOrderProcessor.cancelIfExpired(
                        1L,
                        expiredBefore
                );

        assertThat(result).isTrue();

        assertThat(order.getPaymentStatus())
                .isEqualTo(PaymentStatus.EXPIRED);

        verify(orderRepository)
                .findByIdForUpdate(1L);

        verify(orderCancellationService)
                .cancel(order);
    }

    @Test
    void cancelIfExpired_shouldReturnFalseWhenOrderDoesNotExist() {
        when(orderRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        boolean result =
                expiredOrderProcessor.cancelIfExpired(
                        99L,
                        expiredBefore
                );

        assertThat(result).isFalse();

        verify(orderCancellationService, never())
                .cancel(any(Order.class));
    }

    @Test
    void cancelIfExpired_shouldNotCancelNonPendingOrder() {
        Order order = createOrder(
                OrderStatus.CANCELLED,
                PaymentStatus.PENDING,
                expiredBefore.minusMinutes(1)
        );

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        boolean result =
                expiredOrderProcessor.cancelIfExpired(
                        1L,
                        expiredBefore
                );

        assertThat(result).isFalse();

        verify(orderCancellationService, never())
                .cancel(any(Order.class));
    }

    @Test
    void cancelIfExpired_shouldNotCancelPaidOrder() {
        Order order = createOrder(
                OrderStatus.PENDING,
                PaymentStatus.PAID,
                expiredBefore.minusMinutes(1)
        );

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        boolean result =
                expiredOrderProcessor.cancelIfExpired(
                        1L,
                        expiredBefore
                );

        assertThat(result).isFalse();

        verify(orderCancellationService, never())
                .cancel(any(Order.class));
    }

    @Test
    void cancelIfExpired_shouldNotCancelOrderWithNullCreatedAt() {
        Order order = createOrder(
                OrderStatus.PENDING,
                PaymentStatus.PENDING,
                null
        );

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        boolean result =
                expiredOrderProcessor.cancelIfExpired(
                        1L,
                        expiredBefore
                );

        assertThat(result).isFalse();

        verify(orderCancellationService, never())
                .cancel(any(Order.class));
    }

    @Test
    void cancelIfExpired_shouldNotCancelOrderCreatedExactlyAtThreshold() {
        Order order = createOrder(
                OrderStatus.PENDING,
                PaymentStatus.PENDING,
                expiredBefore
        );

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        boolean result =
                expiredOrderProcessor.cancelIfExpired(
                        1L,
                        expiredBefore
                );

        assertThat(result).isFalse();

        verify(orderCancellationService, never())
                .cancel(any(Order.class));
    }

    @Test
    void cancelIfExpired_shouldNotCancelOrderCreatedAfterThreshold() {
        Order order = createOrder(
                OrderStatus.PENDING,
                PaymentStatus.PENDING,
                expiredBefore.plusMinutes(1)
        );

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        boolean result =
                expiredOrderProcessor.cancelIfExpired(
                        1L,
                        expiredBefore
                );

        assertThat(result).isFalse();

        verify(orderCancellationService, never())
                .cancel(any(Order.class));
    }

    private Order createOrder(
            OrderStatus orderStatus,
            PaymentStatus paymentStatus,
            LocalDateTime createdAt
    ) {
        Order order = new Order();

        order.setId(1L);
        order.setStatus(orderStatus);
        order.setPaymentStatus(paymentStatus);
        order.setCreatedAt(createdAt);

        return order;
    }
}