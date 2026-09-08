package com.example.minishop.dto.response;

import com.example.minishop.projection.*;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal revenue;
    private long pendingOrders;
    private long completedOrders;
    private long cancelledOrders;
    private long paidOrders;
    private long waitingPaymentOrders;
    private long paidStatusOrders;
    private long confirmedOrders;
    private long packingOrders;
    private long shippingOrders;
    private long deliveredOrders;
    private long returnedOrders;
    private long totalCustomers;
    private long totalSellers;
    private List<MonthlySellerRevenue> monthlyRevenue;
    private List<TopSellerProduct> topProducts;
    private List<TopAdminSeller> topSellers;
    private long inStockProducts;
    private long lowStockProducts;
    private long outOfStockProducts;
    private long codOrders;
    private long vnpayOrders;
    private long pendingPaymentStatusOrders;
    private long processingPaymentOrders;
    private long failedPaymentOrders;
    private long cancelledPaymentOrders;
    private long expiredPaymentOrders;
    private long refundedPaymentOrders;
    private List<TopProductRevenue> topProductsByRevenue;
    private List<NeverSoldProduct> productsNeverSold;
    private List<SlowMovingProduct> slowMovingProducts;
    private BigDecimal averageOrderValue;
    private double completionRate;
    private double cancellationRate;
    private double returnRate;
    private long totalAdmins;
    private long activeUsers;
    private long inactiveUsers;
    private long verifiedUsers;
    private long unverifiedUsers;
    private List<AuthProviderStat> userAuthProviders;
    private List<MonthlyUserGrowth> monthlyUserGrowth;
    private long totalCategories;
    private long categoriesWithProducts;
    private long emptyCategories;
    private TopCategoryAnalytics topCategoryByRevenue;
    private TopCategoryAnalytics topCategoryBySold;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
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

    public long getPaidOrders() {
        return paidOrders;
    }

    public void setPaidOrders(long paidOrders) {
        this.paidOrders = paidOrders;
    }

    public long getWaitingPaymentOrders() {
        return waitingPaymentOrders;
    }

    public void setWaitingPaymentOrders(long waitingPaymentOrders) {
        this.waitingPaymentOrders = waitingPaymentOrders;
    }

    public long getPaidStatusOrders() {
        return paidStatusOrders;
    }

    public void setPaidStatusOrders(long paidStatusOrders) {
        this.paidStatusOrders = paidStatusOrders;
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

    public long getShippingOrders() {
        return shippingOrders;
    }

    public void setShippingOrders(long shippingOrders) {
        this.shippingOrders = shippingOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public long getReturnedOrders() {
        return returnedOrders;
    }

    public void setReturnedOrders(long returnedOrders) {
        this.returnedOrders = returnedOrders;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalSellers() {
        return totalSellers;
    }

    public void setTotalSellers(long totalSellers) {
        this.totalSellers = totalSellers;
    }

    public List<MonthlySellerRevenue> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(List<MonthlySellerRevenue> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public List<TopSellerProduct> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopSellerProduct> topProducts) {
        this.topProducts = topProducts;
    }

    public List<TopAdminSeller> getTopSellers() {
        return topSellers;
    }

    public void setTopSellers(List<TopAdminSeller> topSellers) {
        this.topSellers = topSellers;
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

    public long getPendingPaymentStatusOrders() {
        return pendingPaymentStatusOrders;
    }

    public void setPendingPaymentStatusOrders(long pendingPaymentStatusOrders) {
        this.pendingPaymentStatusOrders = pendingPaymentStatusOrders;
    }

    public long getProcessingPaymentOrders() {
        return processingPaymentOrders;
    }

    public void setProcessingPaymentOrders(long processingPaymentOrders) {
        this.processingPaymentOrders = processingPaymentOrders;
    }

    public long getFailedPaymentOrders() {
        return failedPaymentOrders;
    }

    public void setFailedPaymentOrders(long failedPaymentOrders) {
        this.failedPaymentOrders = failedPaymentOrders;
    }

    public long getCancelledPaymentOrders() {
        return cancelledPaymentOrders;
    }

    public void setCancelledPaymentOrders(long cancelledPaymentOrders) {
        this.cancelledPaymentOrders = cancelledPaymentOrders;
    }

    public long getExpiredPaymentOrders() {
        return expiredPaymentOrders;
    }

    public void setExpiredPaymentOrders(long expiredPaymentOrders) {
        this.expiredPaymentOrders = expiredPaymentOrders;
    }

    public long getRefundedPaymentOrders() {
        return refundedPaymentOrders;
    }

    public void setRefundedPaymentOrders(long refundedPaymentOrders) {
        this.refundedPaymentOrders = refundedPaymentOrders;
    }

    public List<TopProductRevenue> getTopProductsByRevenue() {
        return topProductsByRevenue;
    }

    public void setTopProductsByRevenue(List<TopProductRevenue> topProductsByRevenue) {
        this.topProductsByRevenue = topProductsByRevenue;
    }

    public List<NeverSoldProduct> getProductsNeverSold() {
        return productsNeverSold;
    }

    public void setProductsNeverSold(List<NeverSoldProduct> productsNeverSold) {
        this.productsNeverSold = productsNeverSold;
    }

    public List<SlowMovingProduct> getSlowMovingProducts() {
        return slowMovingProducts;
    }

    public void setSlowMovingProducts(List<SlowMovingProduct> slowMovingProducts) {
        this.slowMovingProducts = slowMovingProducts;
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

    public double getReturnRate() {
        return returnRate;
    }

    public void setReturnRate(double returnRate) {
        this.returnRate = returnRate;
    }

    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public long getVerifiedUsers() {
        return verifiedUsers;
    }

    public void setVerifiedUsers(long verifiedUsers) {
        this.verifiedUsers = verifiedUsers;
    }

    public long getUnverifiedUsers() {
        return unverifiedUsers;
    }

    public void setUnverifiedUsers(long unverifiedUsers) {
        this.unverifiedUsers = unverifiedUsers;
    }

    public List<AuthProviderStat> getUserAuthProviders() {
        return userAuthProviders;
    }

    public void setUserAuthProviders(List<AuthProviderStat> userAuthProviders) {
        this.userAuthProviders = userAuthProviders;
    }

    public List<MonthlyUserGrowth> getMonthlyUserGrowth() {
        return monthlyUserGrowth;
    }

    public void setMonthlyUserGrowth(List<MonthlyUserGrowth> monthlyUserGrowth) {
        this.monthlyUserGrowth = monthlyUserGrowth;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public long getCategoriesWithProducts() {
        return categoriesWithProducts;
    }

    public void setCategoriesWithProducts(long categoriesWithProducts) {
        this.categoriesWithProducts = categoriesWithProducts;
    }

    public long getEmptyCategories() {
        return emptyCategories;
    }

    public void setEmptyCategories(long emptyCategories) {
        this.emptyCategories = emptyCategories;
    }

    public TopCategoryAnalytics getTopCategoryByRevenue() {
        return topCategoryByRevenue;
    }

    public void setTopCategoryByRevenue(TopCategoryAnalytics topCategoryByRevenue) {
        this.topCategoryByRevenue = topCategoryByRevenue;
    }

    public TopCategoryAnalytics getTopCategoryBySold() {
        return topCategoryBySold;
    }

    public void setTopCategoryBySold(TopCategoryAnalytics topCategoryBySold) {
        this.topCategoryBySold = topCategoryBySold;
    }
}