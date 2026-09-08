package com.example.minishop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PublicFlashSaleResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer quantity;
    private Integer sold;
    private Integer remainingQuantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(
            Long productId
    ) {
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

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(
            String productImage
    ) {
        this.productImage = productImage;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(
            BigDecimal originalPrice
    ) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(
            BigDecimal salePrice
    ) {
        this.salePrice = salePrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(
            Integer quantity
    ) {
        this.quantity = quantity;
    }

    public Integer getSold() {
        return sold;
    }

    public void setSold(
            Integer sold
    ) {
        this.sold = sold;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(
            Integer remainingQuantity
    ) {
        this.remainingQuantity =
                remainingQuantity;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(
            LocalDateTime startTime
    ) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(
            LocalDateTime endTime
    ) {
        this.endTime = endTime;
    }
}