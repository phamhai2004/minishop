package com.example.minishop.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class ProductVariantResponse {

    private Long id;
    private BigDecimal price;
    private Integer quantity;
    private String sku;
    private List<ProductVariantOptionResponse> options;

    public ProductVariantResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<ProductVariantOptionResponse> getOptions() {
        return options;
    }

    public void setOptions(List<ProductVariantOptionResponse> options) {
        this.options = options;
    }
}