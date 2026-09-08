package com.example.minishop.service;

import com.example.minishop.constant.NotificationType;
import com.example.minishop.constant.OrderStatus;
import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.entity.Order;
import com.example.minishop.entity.OutboxEvent;
import com.example.minishop.entity.Payment;
import com.example.minishop.entity.ShopOrder;
import com.example.minishop.repository.OrderRepository;
import com.example.minishop.repository.PaymentRepository;
import com.example.minishop.repository.ShopOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxEventHandler {

    private final OrderRepository orderRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final ReliableEmailService reliableEmailService;

    public OutboxEventHandler(
            OrderRepository orderRepository,
            ShopOrderRepository shopOrderRepository,
            PaymentRepository paymentRepository,
            NotificationService notificationService,
            ReliableEmailService reliableEmailService
    ) {
        this.orderRepository = orderRepository;
        this.shopOrderRepository = shopOrderRepository;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
        this.reliableEmailService = reliableEmailService;
    }

    public void handle(OutboxEvent event) {
        switch (event.getEventType()) {
            case ORDER_CREATED ->
                    handleOrderCreated(
                            event.getAggregateId()
                    );

            case ORDER_CANCELLED ->
                    handleOrderCancelled(event);

            case ORDER_RECEIVED ->
                    handleOrderReceived(event);

            case SHOP_ORDER_CONFIRMED ->
                    handleShopOrderConfirmed(
                            event.getAggregateId()
                    );

            case SHOP_ORDER_PACKING ->
                    handleShopOrderPacking(
                            event.getAggregateId()
                    );

            case SHOP_ORDER_SHIPPING ->
                    handleShopOrderShipping(
                            event.getAggregateId()
                    );

            case SHOP_ORDER_DELIVERED ->
                    handleShopOrderDelivered(
                            event.getAggregateId()
                    );

            case SHOP_ORDER_CANCELLED ->
                    handleShopOrderCancelled(
                            event.getAggregateId()
                    );

            case PAYMENT_PAID ->
                    handlePaymentPaid(
                            event.getAggregateId()
                    );

            default -> throw new IllegalArgumentException(
                    "Chưa hỗ trợ Outbox event: "
                            + event.getEventType()
            );
        }
    }

    private void handleOrderCreated(Long orderId) {

        Order order = orderRepository
                .findByIdForNotification(orderId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Order not found: " + orderId
                        )
                );

        notificationService.createIfAbsent(
                "ORDER_CREATED:" + orderId,
                order.getUser(),
                "Đặt hàng thành công",
                "Đơn hàng của bạn đã được tạo thành công ",
                NotificationType.ORDER_CREATED
        );

        List<ShopOrder> shopOrders =
                shopOrderRepository.findAllByOrderIdForNotification(orderId);

        for (ShopOrder shopOrder : shopOrders) {

            String customerName =
                    order.getUser().getFullName();

            String paymentMethod =
                    switch (shopOrder.getPaymentMethod()) {
                        case COD -> "COD";
                        case VNPAY -> "VNPay";
                    };

            String paymentStatus =
                    switch (shopOrder.getPaymentStatus()) {
                        case PENDING -> "Chưa thanh toán";
                        case PROCESSING -> "Đang xử lý";
                        case PAID -> "Đã thanh toán";
                        case FAILED -> "Thanh toán thất bại";
                        case CANCELLED -> "Đã hủy";
                        case EXPIRED -> "Đã hết hạn";
                        case REFUNDED -> "Đã hoàn tiền";
                    };

            StringBuilder productInfo =
                    new StringBuilder();

            shopOrder.getItems().forEach(item -> {

                if (productInfo.length() > 0) {
                    productInfo.append(", ");
                }

                productInfo.append(
                        item.getProduct().getName()
                );

                productInfo.append(" x");
                productInfo.append(item.getQuantity());
            });

            String content =
                    customerName
                            + " đã mua "
                            + productInfo
                            + " với phương thức thanh toán "
                            + paymentMethod
                            + " - "
                            + paymentStatus
                            + ".";

            notificationService.createIfAbsent(
                    "ORDER_CREATED_SELLER:" + shopOrder.getId(),
                    shopOrder.getShop().getOwner(),
                    "Bạn có một đơn hàng mới",
                    content,
                    NotificationType.ORDER_CREATED
            );

            reliableEmailService.sendShopOrderCreatedOnce(shopOrder);
        }

    }

    private void handleOrderCancelled(
            OutboxEvent event
    ) {

        if ("SHOP_ORDER".equals(
                event.getAggregateType()
        )) {

            ShopOrder shopOrder =
                    shopOrderRepository
                            .findDetailedById(
                                    event.getAggregateId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Không tìm thấy ShopOrder: "
                                                    + event.getAggregateId()
                                    )
                            );

            notificationService.createIfAbsent(
                    "SHOP_ORDER_CANCELLED_BY_CUSTOMER:"
                            + shopOrder.getId(),
                    shopOrder.getShop().getOwner(),
                    "Khách hàng đã hủy đơn",
                    shopOrder.getOrder()
                            .getUser()
                            .getFullName()
                            + " đã hủy đơn hàng #"
                            + shopOrder.getOrderCode()
                            + ".",
                    NotificationType.ORDER_CANCELLED
            );

            return;
        }

        handleLegacyOrderCancelled(
                event.getAggregateId()
        );
    }

    private void handleLegacyOrderCancelled(Long orderId) {

        Order order = getDetailedOrder(orderId);

        notificationService.createIfAbsent(
                "ORDER_CANCELLED:" + orderId,
                order.getUser(),
                "Đơn hàng đã bị hủy",
                "Đơn hàng #" + order.getId()
                        + " đã được hủy.",
                NotificationType.ORDER_CANCELLED
        );

        for (ShopOrder shopOrder : order.getShopOrders()) {

            String content =
                    order.getUser().getFullName()
                            + " đã hủy đơn hàng #"
                            + order.getId()
                            + ".";

            notificationService.createIfAbsent(
                    "ORDER_CANCELLED_SELLER:" + shopOrder.getId(),
                    shopOrder.getShop().getOwner(),
                    "Đơn hàng đã bị hủy",
                    content,
                    NotificationType.ORDER_CANCELLED
            );
        }

        reliableEmailService.sendOrderCancelledOnce(order);
    }

    private void handleOrderReceived(
            OutboxEvent event
    ) {

        if ("SHOP_ORDER".equals(
                event.getAggregateType()
        )) {

            ShopOrder shopOrder =
                    shopOrderRepository
                            .findDetailedById(
                                    event.getAggregateId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Không tìm thấy ShopOrder: "
                                                    + event.getAggregateId()
                                    )
                            );

            String content =
                    shopOrder.getOrder()
                            .getUser()
                            .getFullName()
                            + " đã xác nhận nhận đơn hàng #"
                            + shopOrder.getOrderCode()
                            + ".";

            if (shopOrder.getPaymentMethod()
                    == PaymentMethod.COD
                    && shopOrder.getPaymentStatus()
                    == PaymentStatus.PAID) {

                content +=
                        " Thanh toán COD: Đã thanh toán.";
            }

            notificationService.createIfAbsent(
                    "SHOP_ORDER_RECEIVED_SELLER:"
                            + shopOrder.getId(),
                    shopOrder.getShop().getOwner(),
                    "Khách hàng đã nhận hàng",
                    content,
                    NotificationType.ORDER_RECEIVED
            );

            return;
        }

        handleLegacyOrderReceived(
                event.getAggregateId()
        );
    }

    private void handleLegacyOrderReceived(Long orderId) {

        Order order = getDetailedOrder(orderId);

        for (ShopOrder shopOrder : order.getShopOrders()) {

            String content =
                    order.getUser().getFullName()
                            + " đã xác nhận nhận đơn hàng #"
                            + order.getId()
                            + ".";

            if (order.getPaymentMethod() == PaymentMethod.COD
                    && order.getPaymentStatus() == PaymentStatus.PAID) {

                content += " Thanh toán COD: Đã thanh toán.";
            }

            notificationService.createIfAbsent(
                    "ORDER_RECEIVED_SELLER:" + shopOrder.getId(),
                    shopOrder.getShop().getOwner(),
                    "Khách hàng đã nhận hàng",
                    content,
                    NotificationType.ORDER_RECEIVED
            );
        }
    }

    private void handleShopOrderConfirmed(
            Long shopOrderId
    ) {
        ShopOrder shopOrder = shopOrderRepository
                .findDetailedById(shopOrderId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy ShopOrder: "
                                        + shopOrderId
                        )
                );

        notificationService.createIfAbsent(
                "SHOP_ORDER_CONFIRMED:" + shopOrderId,
                shopOrder.getOrder().getUser(),
                "Shop đã xác nhận đơn hàng",
                "Shop "
                        + shopOrder.getShop().getName()
                        + " đã xác nhận đơn hàng #"
                        + shopOrder.getOrderCode(),
                NotificationType.ORDER_CONFIRMED
        );
    }

    private void handleShopOrderPacking(Long shopOrderId) {

        ShopOrder shopOrder = shopOrderRepository
                .findDetailedById(shopOrderId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy ShopOrder: "
                                        + shopOrderId
                        )
                );

        notificationService.createIfAbsent(
                "SHOP_ORDER_PACKING:" + shopOrderId,
                shopOrder.getOrder().getUser(),
                "Shop đang đóng gói đơn hàng",
                "Shop "
                        + shopOrder.getShop().getName()
                        + " đang đóng gói đơn hàng #"
                        + shopOrder.getOrderCode()
                        + ".",
                NotificationType.ORDER_PACKING
        );
    }

    private void handleShopOrderShipping(Long shopOrderId) {

        ShopOrder shopOrder = shopOrderRepository
                .findDetailedById(shopOrderId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy ShopOrder: "
                                        + shopOrderId
                        )
                );

        notificationService.createIfAbsent(
                "SHOP_ORDER_SHIPPING:" + shopOrderId,
                shopOrder.getOrder().getUser(),
                "Đơn hàng đang được vận chuyển",
                "Đơn hàng #"
                        + shopOrder.getOrderCode()
                        + " của bạn đang được vận chuyển.",
                NotificationType.ORDER_SHIPPING
        );
    }

    private void handleShopOrderDelivered(
            Long shopOrderId
    ) {

        ShopOrder shopOrder =
                shopOrderRepository
                        .findDetailedById(shopOrderId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Không tìm thấy ShopOrder: "
                                                + shopOrderId
                                )
                        );

        notificationService.createIfAbsent(
                "SHOP_ORDER_DELIVERED:"
                        + shopOrderId,
                shopOrder.getOrder().getUser(),
                "Đơn hàng đã giao",
                "Đơn hàng #"
                        + shopOrder.getOrderCode()
                        + " đã được giao thành công.",
                NotificationType.ORDER_DELIVERED
        );

        notificationService.createIfAbsent(
                "SHOP_ORDER_DELIVERED_SELLER:"
                        + shopOrderId,
                shopOrder.getShop().getOwner(),
                "Đơn hàng đã giao thành công",
                "Đơn hàng #"
                        + shopOrder.getOrderCode()
                        + " đã được giao thành công đến khách hàng.",
                NotificationType.ORDER_DELIVERED
        );

        reliableEmailService
                .sendShopOrderDeliveredOnce(
                        shopOrder
                );
    }

    private void handleShopOrderCancelled(Long shopOrderId) {

        ShopOrder shopOrder = shopOrderRepository
                .findDetailedById(shopOrderId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy ShopOrder: "
                                        + shopOrderId
                        )
                );

        notificationService.createIfAbsent(
                "SHOP_ORDER_CANCELLED:" + shopOrderId,
                shopOrder.getOrder().getUser(),
                "Shop đã hủy đơn hàng",
                "Shop "
                        + shopOrder.getShop().getName()
                        + " đã hủy đơn hàng #"
                        + shopOrder.getOrderCode()
                        + ". Lý do: "
                        + shopOrder.getCancelReason(),
                NotificationType.ORDER_CANCELLED
        );
    }

    private void handlePaymentPaid(Long paymentId) {

        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy Payment: "
                                        + paymentId
                        )
                );

        Order order = getDetailedOrder(
                payment.getOrder().getId()
        );

        LocalDateTime paidAt =
                order.getPaidAt() != null
                        ? order.getPaidAt()
                        : LocalDateTime.now();

        for (ShopOrder shopOrder :
                order.getShopOrders()) {

            shopOrder.setPaymentMethod(
                    PaymentMethod.VNPAY
            );

            shopOrder.setPaymentStatus(
                    PaymentStatus.PAID
            );

            shopOrder.setPaymentTransactionId(
                    order.getPaymentTransactionId()
            );

            shopOrder.setPaidAt(
                    paidAt
            );
        }

        shopOrderRepository.saveAll(
                order.getShopOrders()
        );

        notificationService.createIfAbsent(
                "PAYMENT_PAID:" + paymentId,
                order.getUser(),
                "Thanh toán thành công",
                "Các đơn hàng trong lần đặt hàng này "
                        + "đã được thanh toán VNPay thành công.",
                NotificationType.PAYMENT_SUCCESS
        );

        for (ShopOrder shopOrder : order.getShopOrders()) {

            String content =
                    "Đơn hàng #"
                            + shopOrder.getOrderCode()
                            + " của "
                            + order.getUser().getFullName()
                            + " đã được thanh toán qua VNPay.";

            notificationService.createIfAbsent(
                    "PAYMENT_PAID_SELLER:" + shopOrder.getId(),
                    shopOrder.getShop().getOwner(),
                    "Đơn hàng đã thanh toán",
                    content,
                    NotificationType.PAYMENT_SUCCESS
            );
        }
    }

    private Order getDetailedOrder(Long orderId) {
        return orderRepository
                .findDetailedById(orderId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy Order: "
                                        + orderId
                        )
                );
    }
}