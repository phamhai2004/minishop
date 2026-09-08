package com.example.minishop.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AddWishlistRequest {

    @NotNull(
            message =
                    "Product ID không được bỏ trống"
    )
    @Positive(
            message =
                    "Product ID phải lớn hơn 0"
    )
    private Long productId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(
            Long productId
    ) {
        this.productId = productId;
    }
}