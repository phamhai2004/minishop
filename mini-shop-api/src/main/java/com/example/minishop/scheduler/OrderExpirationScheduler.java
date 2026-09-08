package com.example.minishop.scheduler;

import com.example.minishop.service.OrderExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExpirationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    OrderExpirationScheduler.class
            );

    private final OrderExpirationService
            orderExpirationService;

    public OrderExpirationScheduler(
            OrderExpirationService orderExpirationService
    ) {
        this.orderExpirationService =
                orderExpirationService;
    }

    @Scheduled(
            fixedDelayString =
                    "${app.scheduler.cancel-unpaid-orders-delay-ms:60000}"
    )
    public void cancelExpiredOrders() {

        try {
            int cancelled =
                    orderExpirationService
                            .cancelExpiredOrders();

            if (cancelled > 0) {
                log.info(
                        "Đã tự động hủy {} đơn hết hạn",
                        cancelled
                );
            }
        } catch (Exception exception) {
            log.error(
                    "Lỗi khi hủy đơn hết hạn",
                    exception
            );
        }
    }
}