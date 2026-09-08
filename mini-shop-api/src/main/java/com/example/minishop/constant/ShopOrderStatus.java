package com.example.minishop.constant;

public enum ShopOrderStatus {

    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    PACKING("Đang đóng gói"),
    SHIPPING("Đang vận chuyển"),
    DELIVERED("Đã giao hàng"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy");

    private final String displayName;

    ShopOrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}