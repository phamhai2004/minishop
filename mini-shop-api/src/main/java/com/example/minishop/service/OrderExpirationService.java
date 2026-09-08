package com.example.minishop.service;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderExpirationService {

    private final OrderRepository orderRepository;
    private final ExpiredOrderProcessor expiredOrderProcessor;
    private final long expirationMinutes;
    private final int batchSize;

    public OrderExpirationService(
            OrderRepository orderRepository,
            ExpiredOrderProcessor expiredOrderProcessor,

            @Value(
                    "${app.order.unpaid-expiration-minutes:30}"
            )
            long expirationMinutes,

            @Value(
                    "${app.scheduler.cancel-unpaid-orders-batch-size:100}"
            )
            int batchSize
    ) {
        this.orderRepository = orderRepository;
        this.expiredOrderProcessor = expiredOrderProcessor;
        this.expirationMinutes = expirationMinutes;
        this.batchSize = batchSize;
    }

    private static final Logger log =
            LoggerFactory.getLogger(
                    OrderExpirationService.class
            );

    public int cancelExpiredOrders() {

        if (expirationMinutes <= 0) {
            log.warn(
                    "Bỏ qua scheduler vì expirationMinutes không hợp lệ: {}",
                    expirationMinutes
            );
            return 0;
        }

        if (batchSize <= 0) {
            log.warn(
                    "Bỏ qua scheduler vì batchSize không hợp lệ: {}",
                    batchSize
            );
            return 0;
        }

        LocalDateTime expiredBefore =
                LocalDateTime.now()
                        .minusMinutes(expirationMinutes);

        List<Long> orderIds =
                orderRepository.findExpiredUnpaidOrderIds(
                        OrderStatus.PENDING,
                        PaymentStatus.PENDING,
                        PaymentMethod.VNPAY,
                        expiredBefore,
                        PageRequest.of(0, batchSize)
                );

        int cancelledCount = 0;

        for (Long orderId : orderIds) {
            try {
                boolean cancelled =
                        expiredOrderProcessor.cancelIfExpired(
                                orderId,
                                expiredBefore
                        );

                if (cancelled) {
                    cancelledCount++;
                }
            } catch (Exception exception) {
                log.error(
                        "Không thể xử lý đơn hết hạn id={}",
                        orderId,
                        exception
                );
            }
        }

        return cancelledCount;
    }
}