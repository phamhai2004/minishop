package com.example.minishop.projection;

import java.math.BigDecimal;

public interface MonthlySellerRevenue {

    Integer getMonth();

    BigDecimal getRevenue();

}