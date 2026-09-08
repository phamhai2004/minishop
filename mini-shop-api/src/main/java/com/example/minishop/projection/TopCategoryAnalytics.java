package com.example.minishop.projection;

import java.math.BigDecimal;

public interface TopCategoryAnalytics {

    Long getCategoryId();

    String getCategoryName();

    Long getSold();

    BigDecimal getRevenue();
}