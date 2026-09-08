package com.example.minishop.projection;

import java.math.BigDecimal;

public interface SlowMovingProduct {

    Long getProductId();

    String getProductName();

    String getShopName();

    Long getSold();

    BigDecimal getRevenue();

    Long getEffectiveStock();
}