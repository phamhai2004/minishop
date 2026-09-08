package com.example.minishop.constant;

public enum ProductStatus {

    ACTIVE("Đang bán"),
    HIDDEN("Đã ẩn"),
    OUT_OF_STOCK("Hết hàng"),
    DISCONTINUED("Ngừng kinh doanh");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}