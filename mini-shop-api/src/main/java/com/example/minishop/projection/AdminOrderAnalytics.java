package com.example.minishop.projection;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AdminOrderAnalytics {

    Long getOrderId();

    Long getCustomerId();

    String getCustomerName();

    String getCustomerEmail();

    BigDecimal getFinalAmount();

    OrderStatus getOrderStatus();

    PaymentMethod getPaymentMethod();

    PaymentStatus getPaymentStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getPaidAt();
}