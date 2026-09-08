package com.example.minishop.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SellerCustomerAnalytics {

    Long getCustomerId();

    String getCustomerName();

    String getCustomerEmail();

    String getCustomerPhone();

    Long getOrderCount();

    BigDecimal getTotalSpent();

    LocalDateTime getLastOrderAt();

    String getCustomerType();
}