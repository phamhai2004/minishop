package com.example.minishop.constant;

public enum ShopStatus {

    PENDING("Chờ xét duyệt"),
    ACTIVE("Đang hoạt động"),
    SUSPENDED("Tạm khóa"),
    REJECTED("Bị từ chối");

    private final String displayName;

    ShopStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}