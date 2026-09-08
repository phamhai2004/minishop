package com.example.minishop.projection;

import java.math.BigDecimal;

public interface AdminProductAnalytics {

    Long getProductId();

    String getProductName();

    String getShopName();

    Long getSold();

    BigDecimal getRevenue();

    Long getEffectiveStock();
}