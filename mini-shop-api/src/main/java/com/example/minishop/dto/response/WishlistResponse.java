package com.example.minishop.dto.response;

import com.example.minishop.constant.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WishlistResponse {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
    private Long shopId;
    private String shopName;
    private ProductStatus productStatus;
    private String productStatusName;
    private Boolean available;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(
            String productName
    ) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(
            BigDecimal price
    ) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(
            String imageUrl
    ) {
        this.imageUrl = imageUrl;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(
            String shopName
    ) {
        this.shopName = shopName;
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(
            ProductStatus productStatus
    ) {
        this.productStatus = productStatus;
    }

    public String getProductStatusName() {
        return productStatusName;
    }

    public void setProductStatusName(
            String productStatusName
    ) {
        this.productStatusName =
                productStatusName;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(
            Boolean available
    ) {
        this.available = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }
}