package com.example.minishop.constant;

public enum OrderStatus {

    PENDING("Đang chờ xử lý"),
    WAITING_PAYMENT("Chờ thanh toán"),
    PAID("Đã thanh toán"),
    CONFIRMED("Đã xác nhận"),
    PACKING("Đang đóng gói"),
    SHIPPING("Đang vận chuyển"),
    DELIVERED("Đã giao hàng"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy"),
    RETURNED("Đã trả hàng");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}