package com.example.minishop.dto.response;

import java.math.BigDecimal;

public class SellerProductPerformanceResponse {

    private Long productId;
    private String productName;
    private long sold;
    private BigDecimal revenue;
    private double revenueShare;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getSold() {
        return sold;
    }

    public void setSold(long sold) {
        this.sold = sold;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public double getRevenueShare() {
        return revenueShare;
    }

    public void setRevenueShare(double revenueShare) {
        this.revenueShare = revenueShare;
    }
}