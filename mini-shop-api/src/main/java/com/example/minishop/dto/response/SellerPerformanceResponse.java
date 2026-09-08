package com.example.minishop.dto.response;

import java.math.BigDecimal;

public class SellerPerformanceResponse {

    private BigDecimal currentMonthRevenue;
    private BigDecimal previousMonthRevenue;
    private double revenueGrowthRate;

    private long currentMonthPaidOrders;
    private long previousMonthPaidOrders;
    private double paidOrderGrowthRate;

    public BigDecimal getCurrentMonthRevenue() {
        return currentMonthRevenue;
    }

    public void setCurrentMonthRevenue(
            BigDecimal currentMonthRevenue
    ) {
        this.currentMonthRevenue = currentMonthRevenue;
    }

    public BigDecimal getPreviousMonthRevenue() {
        return previousMonthRevenue;
    }

    public void setPreviousMonthRevenue(
            BigDecimal previousMonthRevenue
    ) {
        this.previousMonthRevenue = previousMonthRevenue;
    }

    public double getRevenueGrowthRate() {
        return revenueGrowthRate;
    }

    public void setRevenueGrowthRate(
            double revenueGrowthRate
    ) {
        this.revenueGrowthRate = revenueGrowthRate;
    }

    public long getCurrentMonthPaidOrders() {
        return currentMonthPaidOrders;
    }

    public void setCurrentMonthPaidOrders(
            long currentMonthPaidOrders
    ) {
        this.currentMonthPaidOrders = currentMonthPaidOrders;
    }

    public long getPreviousMonthPaidOrders() {
        return previousMonthPaidOrders;
    }

    public void setPreviousMonthPaidOrders(
            long previousMonthPaidOrders
    ) {
        this.previousMonthPaidOrders = previousMonthPaidOrders;
    }

    public double getPaidOrderGrowthRate() {
        return paidOrderGrowthRate;
    }

    public void setPaidOrderGrowthRate(
            double paidOrderGrowthRate
    ) {
        this.paidOrderGrowthRate = paidOrderGrowthRate;
    }
}