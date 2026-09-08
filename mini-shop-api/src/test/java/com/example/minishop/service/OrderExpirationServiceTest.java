package com.example.minishop.service;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderExpirationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ExpiredOrderProcessor expiredOrderProcessor;

    @Test
    void cancelExpiredOrders_shouldReturnNumberOfCancelledOrders() {
        OrderExpirationService service =
                new OrderExpirationService(
                        orderRepository,
                        expiredOrderProcessor,
                        30,
                        100
                );

        when(orderRepository.findExpiredUnpaidOrderIds(
                eq(OrderStatus.PENDING),
                eq(PaymentStatus.PENDING),
                eq(PaymentMethod.VNPAY),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of(1L, 2L, 3L));

        when(expiredOrderProcessor.cancelIfExpired(
                eq(1L),
                any(LocalDateTime.class)
        )).thenReturn(true);

        when(expiredOrderProcessor.cancelIfExpired(
                eq(2L),
                any(LocalDateTime.class)
        )).thenReturn(false);

        when(expiredOrderProcessor.cancelIfExpired(
                eq(3L),
                any(LocalDateTime.class)
        )).thenReturn(true);

        int result = service.cancelExpiredOrders();

        assertThat(result).isEqualTo(2);

        verify(expiredOrderProcessor)
                .cancelIfExpired(
                        eq(1L),
                        any(LocalDateTime.class)
                );

        verify(expiredOrderProcessor)
                .cancelIfExpired(
                        eq(2L),
                        any(LocalDateTime.class)
                );

        verify(expiredOrderProcessor)
                .cancelIfExpired(
                        eq(3L),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void cancelExpiredOrders_shouldContinueWhenOneOrderThrowsException() {
        OrderExpirationService service =
                new OrderExpirationService(
                        orderRepository,
                        expiredOrderProcessor,
                        30,
                        100
                );

        when(orderRepository.findExpiredUnpaidOrderIds(
                eq(OrderStatus.PENDING),
                eq(PaymentStatus.PENDING),
                eq(PaymentMethod.VNPAY),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of(1L, 2L, 3L));

        when(expiredOrderProcessor.cancelIfExpired(
                eq(1L),
                any(LocalDateTime.class)
        )).thenReturn(true);

        when(expiredOrderProcessor.cancelIfExpired(
                eq(2L),
                any(LocalDateTime.class)
        )).thenThrow(
                new RuntimeException("Test exception")
        );

        when(expiredOrderProcessor.cancelIfExpired(
                eq(3L),
                any(LocalDateTime.class)
        )).thenReturn(true);

        int result = service.cancelExpiredOrders();

        assertThat(result).isEqualTo(2);

        verify(expiredOrderProcessor)
                .cancelIfExpired(
                        eq(3L),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void cancelExpiredOrders_shouldReturnZeroWhenNoOrdersFound() {
        OrderExpirationService service =
                new OrderExpirationService(
                        orderRepository,
                        expiredOrderProcessor,
                        30,
                        100
                );

        when(orderRepository.findExpiredUnpaidOrderIds(
                eq(OrderStatus.PENDING),
                eq(PaymentStatus.PENDING),
                eq(PaymentMethod.VNPAY),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of());

        int result = service.cancelExpiredOrders();

        assertThat(result).isZero();

        verifyNoInteractions(expiredOrderProcessor);
    }

    @Test
    void cancelExpiredOrders_shouldSkipWhenExpirationMinutesIsInvalid() {
        OrderExpirationService service =
                new OrderExpirationService(
                        orderRepository,
                        expiredOrderProcessor,
                        0,
                        100
                );

        int result = service.cancelExpiredOrders();

        assertThat(result).isZero();

        verifyNoInteractions(
                orderRepository,
                expiredOrderProcessor
        );
    }

    @Test
    void cancelExpiredOrders_shouldSkipWhenBatchSizeIsInvalid() {
        OrderExpirationService service =
                new OrderExpirationService(
                        orderRepository,
                        expiredOrderProcessor,
                        30,
                        0
                );

        int result = service.cancelExpiredOrders();

        assertThat(result).isZero();

        verifyNoInteractions(
                orderRepository,
                expiredOrderProcessor
        );
    }

    @Test
    void cancelExpiredOrders_shouldUseConfiguredBatchSize() {
        OrderExpirationService service =
                new OrderExpirationService(
                        orderRepository,
                        expiredOrderProcessor,
                        30,
                        25
                );

        when(orderRepository.findExpiredUnpaidOrderIds(
                eq(OrderStatus.PENDING),
                eq(PaymentStatus.PENDING),
                eq(PaymentMethod.VNPAY),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of());

        service.cancelExpiredOrders();

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(orderRepository)
                .findExpiredUnpaidOrderIds(
                        eq(OrderStatus.PENDING),
                        eq(PaymentStatus.PENDING),
                        eq(PaymentMethod.VNPAY),
                        any(LocalDateTime.class),
                        pageableCaptor.capture()
                );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(25);
    }

    @Test
    void cancelExpiredOrders_shouldPassSameExpirationTimeToEveryOrder() {
        OrderExpirationService service =
                new OrderExpirationService(
                        orderRepository,
                        expiredOrderProcessor,
                        30,
                        100
                );

        when(orderRepository.findExpiredUnpaidOrderIds(
                eq(OrderStatus.PENDING),
                eq(PaymentStatus.PENDING),
                eq(PaymentMethod.VNPAY),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of(1L, 2L));

        when(expiredOrderProcessor.cancelIfExpired(
                anyLong(),
                any(LocalDateTime.class)
        )).thenReturn(false);

        service.cancelExpiredOrders();

        ArgumentCaptor<LocalDateTime> timeCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        verify(expiredOrderProcessor, times(2))
                .cancelIfExpired(
                        anyLong(),
                        timeCaptor.capture()
                );

        List<LocalDateTime> capturedTimes =
                timeCaptor.getAllValues();

        assertThat(capturedTimes)
                .hasSize(2);

        assertThat(capturedTimes.get(0))
                .isEqualTo(capturedTimes.get(1));
    }
}