package com.example.minishop.dto.request;

import com.example.minishop.constant.ProductStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateProductStatusRequest {

    @NotNull(message = "Trạng thái sản phẩm không được bỏ trống")
    private ProductStatus status;

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}