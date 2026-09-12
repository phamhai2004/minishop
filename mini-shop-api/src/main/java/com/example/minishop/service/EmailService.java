package com.example.minishop.service;

import com.example.minishop.entity.*;
import com.example.minishop.repository.ProductImageRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final BrevoEmailClient brevoEmailClient;
    private final TemplateEngine templateEngine;
    private final ProductImageRepository productImageRepository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.mail.from-name}")
    private String senderName;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public EmailService(
            BrevoEmailClient brevoEmailClient,
            TemplateEngine templateEngine,
            ProductImageRepository productImageRepository
    ) {
        this.brevoEmailClient = brevoEmailClient;
        this.templateEngine = templateEngine;
        this.productImageRepository = productImageRepository;
    }

    public void sendOrderCancelledEmail(Order order)
            throws MessagingException {

        Context context = new Context();

        context.setVariable(
                "customerName",
                order.getUser().getFullName()
        );

        context.setVariable(
                "orderId",
                order.getId()
        );

        context.setVariable(
                "orderStatus",
                order.getStatus().getDisplayName()
        );

        String htmlContent = templateEngine.process(
                "email/order-cancelled",
                context
        );

        brevoEmailClient.sendHtmlEmail(
                order.getUser().getEmail(),
                "Hair - Đơn hàng #" + order.getId() + " đã bị hủy",
                htmlContent
        );
    }

    public void sendRegistrationVerificationEmail(
            RegistrationVerification verification
    ) throws MessagingException {

        Context context = new Context();

        context.setVariable(
                "verificationUrl",
                frontendUrl
                        + "/register/verify?token="
                        + verification.getToken()
        );

        context.setVariable(
                "expiresAt",
                verification.getExpiresAt()
        );

        String htmlContent = templateEngine.process(
                "email/registration-verification",
                context
        );

        brevoEmailClient.sendHtmlEmail(
                verification.getEmail(),
                "Hair - Xác minh email đăng ký tài khoản",
                htmlContent
        );
    }

    public void sendEmailChangeVerificationEmail(
            EmailVerificationToken verification
    ) throws MessagingException {

        Context context = new Context();

        context.setVariable(
                "verificationUrl",
                frontendUrl
                        + "/email/verify?token="
                        + verification.getToken()
        );

        context.setVariable(
                "expiresAt",
                verification.getExpiresAt()
        );

        String htmlContent = templateEngine.process(
                "email/email-change-verification",
                context
        );

        brevoEmailClient.sendHtmlEmail(
                verification.getEmail(),
                "Hair - Xác minh email tài khoản",
                htmlContent
        );
    }

    public void sendShopOrderCreatedEmail(
            ShopOrder shopOrder
    ) throws MessagingException {

        Order order =
                shopOrder.getOrder();

        Context context =
                new Context();

        context.setVariable(
                "customerName",
                order.getUser().getFullName()
        );

        context.setVariable(
                "orderCode",
                shopOrder.getOrderCode()
        );

        context.setVariable(
                "shopName",
                shopOrder.getShop().getName()
        );

        context.setVariable(
                "orderStatus",
                "Đã đặt hàng"
        );

        context.setVariable(
                "orderDate",
                shopOrder.getCreatedAt() != null
                        ? shopOrder.getCreatedAt().format(
                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy HH:mm"
                        )
                )
                        : "-"
        );

        String paymentMethod =
                switch (shopOrder.getPaymentMethod()) {

                    case COD ->
                            "Thanh toán khi nhận hàng";

                    case VNPAY ->
                            "VNPay";
                };

        context.setVariable(
                "paymentMethod",
                paymentMethod
        );

        List<Map<String, Object>> emailItems =
                buildShopOrderEmailItems(
                        shopOrder
                );

        context.setVariable(
                "items",
                emailItems
        );

        BigDecimal discountAmount =
                shopOrder.getDiscountAmount() == null
                        ? BigDecimal.ZERO
                        : shopOrder.getDiscountAmount();

        BigDecimal shippingFee =
                shopOrder.getShippingFee() == null
                        ? BigDecimal.ZERO
                        : shopOrder.getShippingFee();

        context.setVariable(
                "totalAmount",
                shopOrder.getSubtotal()
        );

        context.setVariable(
                "discountAmount",
                discountAmount
        );

        context.setVariable(
                "shippingFee",
                shippingFee
        );

        context.setVariable(
                "finalAmount",
                shopOrder.getFinalAmount()
        );

        Address address =
                order.getAddress();

        context.setVariable(
                "receiverName",
                address.getReceiverName()
        );

        context.setVariable(
                "receiverPhone",
                address.getPhone()
        );

        context.setVariable(
                "shippingAddress",
                buildShippingAddress(address)
        );

        context.setVariable(
                "orderUrl",
                frontendUrl
                        + "/customer/orders/"
                        + shopOrder.getOrderCode()
        );

        String htmlContent =
                templateEngine.process(
                        "email/shop-order-created",
                        context
                );

        brevoEmailClient.sendHtmlEmail(
                order.getUser().getEmail(),
                "Hair - Đặt hàng thành công tại "
                        + shopOrder.getShop().getName()
                        + " - Đơn #"
                        + shopOrder.getOrderCode(),
                htmlContent
        );
    }

    public void sendShopOrderDeliveredEmail(
            ShopOrder shopOrder
    ) throws MessagingException {

        Order order =
                shopOrder.getOrder();

        Context context =
                new Context();

        context.setVariable(
                "customerName",
                order.getUser().getFullName()
        );

        context.setVariable(
                "orderCode",
                shopOrder.getOrderCode()
        );

        context.setVariable(
                "shopName",
                shopOrder.getShop().getName()
        );

        context.setVariable(
                "orderDate",
                shopOrder.getCreatedAt() != null
                        ? shopOrder.getCreatedAt().format(
                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy HH:mm"
                        )
                )
                        : "-"
        );

        context.setVariable(
                "deliveredDate",
                shopOrder.getDeliveredAt() != null
                        ? shopOrder.getDeliveredAt().format(
                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy HH:mm"
                        )
                )
                        : "-"
        );

        String paymentMethod =
                switch (shopOrder.getPaymentMethod()) {

                    case COD ->
                            "Thanh toán khi nhận hàng";

                    case VNPAY ->
                            "VNPay";
                };

        context.setVariable(
                "paymentMethod",
                paymentMethod
        );

        List<Map<String, Object>> emailItems =
                buildShopOrderEmailItems(
                        shopOrder
                );

        context.setVariable(
                "items",
                emailItems
        );

        BigDecimal discountAmount =
                shopOrder.getDiscountAmount() == null
                        ? BigDecimal.ZERO
                        : shopOrder.getDiscountAmount();

        BigDecimal shippingFee =
                shopOrder.getShippingFee() == null
                        ? BigDecimal.ZERO
                        : shopOrder.getShippingFee();

        context.setVariable(
                "totalAmount",
                shopOrder.getSubtotal()
        );

        context.setVariable(
                "discountAmount",
                discountAmount
        );

        context.setVariable(
                "shippingFee",
                shippingFee
        );

        context.setVariable(
                "finalAmount",
                shopOrder.getFinalAmount()
        );

        Address address =
                order.getAddress();

        context.setVariable(
                "receiverName",
                address.getReceiverName()
        );

        context.setVariable(
                "receiverPhone",
                address.getPhone()
        );

        context.setVariable(
                "shippingAddress",
                buildShippingAddress(address)
        );

        context.setVariable(
                "orderUrl",
                frontendUrl
                        + "/customer/orders/"
                        + shopOrder.getOrderCode()
        );

        String htmlContent =
                templateEngine.process(
                        "email/shop-order-delivered",
                        context
                );

        brevoEmailClient.sendHtmlEmail(
                order.getUser().getEmail(),
                "Hair - Đơn hàng #"
                        + shopOrder.getOrderCode()
                        + " từ "
                        + shopOrder.getShop().getName()
                        + " đã giao thành công",
                htmlContent
        );
    }

    private String buildShippingAddress(Address address) {
        return String.join(
                ", ",
                address.getDetail(),
                address.getWard(),
                address.getProvince()
        );
    }

    private List<Map<String, Object>>
    buildShopOrderEmailItems(
            ShopOrder shopOrder
    ) {

        List<Map<String, Object>> emailItems =
                new ArrayList<>();

        for (OrderItem item :
                shopOrder.getItems()) {

            Map<String, Object> emailItem =
                    new HashMap<>();

            Product product =
                    item.getProduct();

            String imageUrl =
                    productImageRepository
                            .findFirstByProduct_IdOrderByDisplayOrderAsc(
                                    product.getId()
                            )
                            .map(
                                    ProductImage::getImageUrl
                            )
                            .orElse(null);

            String variantSummary =
                    buildVariantSummary(
                            item.getVariant()
                    );

            emailItem.put(
                    "productName",
                    product.getName()
            );

            emailItem.put(
                    "imageUrl",
                    imageUrl
            );

            emailItem.put(
                    "variantSummary",
                    variantSummary
            );

            emailItem.put(
                    "sku",
                    item.getVariant() != null
                            ? item.getVariant().getSku()
                            : null
            );

            emailItem.put(
                    "quantity",
                    item.getQuantity()
            );

            emailItem.put(
                    "price",
                    item.getPrice()
            );

            emailItem.put(
                    "subtotal",
                    item.getSubtotal()
            );

            emailItems.add(emailItem);
        }

        return emailItems;
    }

    private String buildVariantSummary(
            ProductVariant variant
    ) {

        if (variant == null
                || variant.getOptions() == null
                || variant.getOptions().isEmpty()) {

            return null;
        }

        return variant.getOptions()
                .stream()
                .map(option ->
                        option.getOptionValue().getName()
                )
                .filter(value ->
                        value != null && !value.isBlank()
                )
                .collect(
                        Collectors.joining(" - ")
                );
    }
}