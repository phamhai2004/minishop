package com.example.minishop.projection;

import java.math.BigDecimal;

public interface ProductSummary {

    Long getId();
    String getName();
    BigDecimal getPrice();
    String getImage_url();

}