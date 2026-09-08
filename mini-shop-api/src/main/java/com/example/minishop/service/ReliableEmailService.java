package com.example.minishop.service;

import com.example.minishop.constant.EmailStatus;
import com.example.minishop.entity.EmailLog;
import com.example.minishop.entity.Order;
import com.example.minishop.entity.ShopOrder;
import com.example.minishop.repository.EmailLogRepository;
import com.example.minishop.repository.ShopOrderRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReliableEmailService {

    private final EmailLogRepository emailLogRepository;
    private final EmailService emailService;
    private final ShopOrderRepository shopOrderRepository;

    public ReliableEmailService(
            EmailLogRepository emailLogRepository,
            EmailService emailService,
            ShopOrderRepository shopOrderRepository
    ) {
        this.emailLogRepository = emailLogRepository;
        this.emailService = emailService;
        this.shopOrderRepository = shopOrderRepository;
    }

    @Transactional
    public void sendOrderCancelledOnce(Order order) {

        String eventKey =
                "ORDER_CANCELLED_EMAIL:" + order.getId();

        if (emailLogRepository.existsByEventKeyAndStatus(
                eventKey,
                EmailStatus.SENT
        )) {
            return;
        }

        EmailLog log = emailLogRepository
                .findByEventKey(eventKey)
                .orElseGet(() -> createLog(
                        eventKey,
                        order.getUser().getEmail(),
                        "Đơn hàng #" + order.getId()
                                + " đã bị hủy"
                ));

        try {
            emailService.sendOrderCancelledEmail(order);

            log.setStatus(EmailStatus.SENT);
            log.setSentAt(LocalDateTime.now());
            log.setLastError(null);

        } catch (MessagingException exception) {

            log.setStatus(EmailStatus.FAILED);
            log.setRetryCount(log.getRetryCount() + 1);
            log.setLastError(exception.getMessage());

            throw new IllegalStateException(
                    "Gửi email hủy đơn thất bại",
                    exception
            );
        }
    }

    @Transactional
    public void sendShopOrderCreatedOnce(
            ShopOrder shopOrder
    ) {

        ShopOrder managedShopOrder =
                shopOrderRepository
                        .findDetailedById(
                                shopOrder.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Không tìm thấy ShopOrder: "
                                                + shopOrder.getId()
                                )
                        );

        String eventKey =
                "SHOP_ORDER_CREATED_EMAIL:"
                        + managedShopOrder.getId();

        if (emailLogRepository
                .existsByEventKeyAndStatus(
                        eventKey,
                        EmailStatus.SENT
                )) {

            return;
        }

        Order order =
                managedShopOrder.getOrder();

        EmailLog log =
                emailLogRepository
                        .findByEventKey(eventKey)
                        .orElseGet(() ->
                                createLog(
                                        eventKey,
                                        order.getUser()
                                                .getEmail(),
                                        "Đặt hàng thành công tại "
                                                + managedShopOrder
                                                .getShop()
                                                .getName()
                                                + " - Đơn #"
                                                + managedShopOrder.getOrderCode()
                                )
                        );

        try {

            emailService
                    .sendShopOrderCreatedEmail(
                            managedShopOrder
                    );

            log.setStatus(EmailStatus.SENT);
            log.setSentAt(LocalDateTime.now());
            log.setLastError(null);

        } catch (MessagingException exception) {

            log.setStatus(EmailStatus.FAILED);

            log.setRetryCount(
                    log.getRetryCount() + 1
            );

            log.setLastError(
                    exception.getMessage()
            );

            throw new IllegalStateException(
                    "Gửi email đặt hàng theo shop thất bại",
                    exception
            );
        }
    }

    @Transactional
    public void sendShopOrderDeliveredOnce(
            ShopOrder shopOrder
    ) {

        ShopOrder managedShopOrder =
                shopOrderRepository
                        .findDetailedById(
                                shopOrder.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Không tìm thấy ShopOrder: "
                                                + shopOrder.getId()
                                )
                        );

        String eventKey =
                "SHOP_ORDER_DELIVERED_EMAIL:"
                        + shopOrder.getId();

        if (emailLogRepository
                .existsByEventKeyAndStatus(
                        eventKey,
                        EmailStatus.SENT
                )) {

            return;
        }

        Order order =
                managedShopOrder.getOrder();

        EmailLog log =
                emailLogRepository
                        .findByEventKey(eventKey)
                        .orElseGet(() ->
                                createLog(
                                        eventKey,
                                        order.getUser()
                                                .getEmail(),
                                        managedShopOrder
                                                .getShop()
                                                .getName()
                                                + " đã giao hàng - Đơn #"
                                                + managedShopOrder.getOrderCode()
                                )
                        );

        try {

            emailService
                    .sendShopOrderDeliveredEmail(
                            managedShopOrder
                    );

            log.setStatus(EmailStatus.SENT);
            log.setSentAt(LocalDateTime.now());
            log.setLastError(null);

        } catch (MessagingException exception) {

            log.setStatus(EmailStatus.FAILED);

            log.setRetryCount(
                    log.getRetryCount() + 1
            );

            log.setLastError(
                    exception.getMessage()
            );

            throw new IllegalStateException(
                    "Gửi email giao hàng theo shop thất bại",
                    exception
            );
        }
    }

    private EmailLog createLog(
            String eventKey,
            String recipient,
            String subject
    ) {
        EmailLog log = new EmailLog();

        log.setEventKey(eventKey);
        log.setRecipient(recipient);
        log.setSubject(subject);
        log.setStatus(EmailStatus.PENDING);
        log.setRetryCount(0);
        log.setCreatedAt(LocalDateTime.now());

        return emailLogRepository.save(log);
    }
}