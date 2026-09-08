package com.example.minishop.projection;

import java.math.BigDecimal;

public interface AdminCategoryAnalytics {

    Long getCategoryId();

    String getCategoryName();

    Long getProductCount();

    Long getSold();

    BigDecimal getRevenue();
}