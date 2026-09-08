package com.example.minishop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectShopRequest {

    @NotBlank(message = "Lý do từ chối không được bỏ trống")
    @Size(
            max = 500,
            message = "Lý do từ chối không được vượt quá 500 ký tự"
    )
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}