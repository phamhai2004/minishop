package com.example.minishop.projection;

import java.math.BigDecimal;

public interface SellerProductAnalytics {

    Long getProductId();

    String getProductName();

    Long getSold();

    BigDecimal getRevenue();

    Long getEffectiveStock();
}