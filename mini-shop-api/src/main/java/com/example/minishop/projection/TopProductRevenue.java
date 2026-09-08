package com.example.minishop.projection;

import java.math.BigDecimal;

public interface TopProductRevenue {

    Long getProductId();

    String getProductName();

    BigDecimal getRevenue();

    Long getSold();
}