package com.example.minishop.dto.response;

import com.example.minishop.projection.MonthlySellerRevenue;
import com.example.minishop.projection.TopSellerProduct;

import java.math.BigDecimal;
import java.util.List;

public class SellerDashboardResponse {

    private String shopName;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal revenue;
    private long pendingOrders;
    private long shippingOrders;
    private long completedOrders;
    private long cancelledOrders;
    private long paidOrders;
    private List<TopSellerProduct> topProducts;
    private List<MonthlySellerRevenue> monthlyRevenue;
    private long confirmedOrders;
    private long packingOrders;
    private long deliveredOrders;
    private long inStockProducts;
    private long lowStockProducts;
    private long outOfStockProducts;
    private long customersPurchased;
    private long newCustomers;
    private long returningCustomers;
    private long codOrders;
    private long vnpayOrders;
    private BigDecimal averageOrderValue;
    private double completionRate;
    private double cancellationRate;

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getShippingOrders() {
        return shippingOrders;
    }

    public void setShippingOrders(long shippingOrders) {
        this.shippingOrders = shippingOrders;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public List<TopSellerProduct> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopSellerProduct> topProducts) {
        this.topProducts = topProducts;
    }

    public List<MonthlySellerRevenue> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(List<MonthlySellerRevenue> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public long getPaidOrders() {
        return paidOrders;
    }

    public void setPaidOrders(long paidOrders) {
        this.paidOrders = paidOrders;
    }

    public long getConfirmedOrders() {
        return confirmedOrders;
    }

    public void setConfirmedOrders(long confirmedOrders) {
        this.confirmedOrders = confirmedOrders;
    }

    public long getPackingOrders() {
        return packingOrders;
    }

    public void setPackingOrders(long packingOrders) {
        this.packingOrders = packingOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public long getInStockProducts() {
        return inStockProducts;
    }

    public void setInStockProducts(long inStockProducts) {
        this.inStockProducts = inStockProducts;
    }

    public long getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(long lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public long getOutOfStockProducts() {
        return outOfStockProducts;
    }

    public void setOutOfStockProducts(long outOfStockProducts) {
        this.outOfStockProducts = outOfStockProducts;
    }

    public long getCustomersPurchased() {
        return customersPurchased;
    }

    public void setCustomersPurchased(long customersPurchased) {
        this.customersPurchased = customersPurchased;
    }

    public long getNewCustomers() {
        return newCustomers;
    }

    public void setNewCustomers(long newCustomers) {
        this.newCustomers = newCustomers;
    }

    public long getReturningCustomers() {
        return returningCustomers;
    }

    public void setReturningCustomers(long returningCustomers) {
        this.returningCustomers = returningCustomers;
    }

    public long getCodOrders() {
        return codOrders;
    }

    public void setCodOrders(long codOrders) {
        this.codOrders = codOrders;
    }

    public long getVnpayOrders() {
        return vnpayOrders;
    }

    public void setVnpayOrders(long vnpayOrders) {
        this.vnpayOrders = vnpayOrders;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public double getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(double cancellationRate) {
        this.cancellationRate = cancellationRate;
    }
}