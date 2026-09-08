package com.example.minishop.dto.response;

import java.math.BigDecimal;

public class RevenueChartPoint {

    private String label;
    private BigDecimal revenue;

    public RevenueChartPoint() {
    }

    public RevenueChartPoint(
            String label,
            BigDecimal revenue
    ) {
        this.label = label;
        this.revenue = revenue;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}