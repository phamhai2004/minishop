package com.example.minishop.projection;

import java.math.BigDecimal;

public interface TopAdminSeller {

    Long getShopId();

    String getShopName();

    BigDecimal getRevenue();

    Long getOrderCount();
}