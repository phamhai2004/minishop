package com.example.minishop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelShopOrderRequest {

    @NotBlank(message = "Lý do hủy không được bỏ trống")
    @Size(max = 500, message = "Lý do hủy tối đa 500 ký tự")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}