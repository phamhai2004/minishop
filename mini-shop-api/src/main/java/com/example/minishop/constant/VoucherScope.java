package com.example.minishop.constant;

public enum VoucherScope {

    PLATFORM("Toàn sàn"),
    SHOP("Theo shop");

    private final String displayName;

    VoucherScope(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}