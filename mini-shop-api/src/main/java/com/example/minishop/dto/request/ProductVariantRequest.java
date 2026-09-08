package com.example.minishop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantRequest {

    @NotNull(message = "Giá không được bỏ trống")
    @DecimalMin(
            value = "1",
            message = "Giá phải lớn hơn 0"
    )
    private BigDecimal price;

    @NotNull(message = "Số lượng không được bỏ trống")
    @Min(
            value = 1,
            message = "Số lượng phải lớn hơn 0"
    )
    private Integer quantity;

    private String sku;

    private List<Long> optionValueIds = new ArrayList<>();

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public List<Long> getOptionValueIds() {
        return optionValueIds;
    }

    public void setOptionValueIds(List<Long> optionValueIds) {
        this.optionValueIds = optionValueIds;
    }
}