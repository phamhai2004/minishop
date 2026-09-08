package com.example.minishop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductRequest {
    @NotBlank (message = "Tên sản phẩm không được bỏ trống")
    private String name;
    @DecimalMin(value = "1", message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    @NotBlank(message = "Mô tả không được bỏ trống")
    @Size(max = 500, message = "Mô tả không đươc quá 500 ký tự")
    private String description;
    @NotNull(message = "Danh mục không được bỏ trống")
    private Long categoryId;
    @Valid
    private List<ProductVariantRequest> variants = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<ProductVariantRequest> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantRequest> variants) {
        this.variants = variants;
    }
}
