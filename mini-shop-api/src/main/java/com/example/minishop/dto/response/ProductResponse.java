package com.example.minishop.dto.response;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.dto.response.ProductVariantResponse;

import java.math.BigDecimal;
import java.util.List;

public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String description;
    private List<ProductImageResponse> images;
    private Long categoryId;
    private String categoryName;
    private BigDecimal salePrice;
    private Boolean flashSale;
    private Integer flashSaleQuantity;
    private Integer flashSaleSold;
    private Integer remain;
    private Long shopId;
    private String shopName;
    private ProductStatus status;
    private String statusName;
    private List<ProductVariantResponse> variants;

    public ProductResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<ProductImageResponse> getImages() {
        return images;
    }

    public void setImages(List<ProductImageResponse> images) {
        this.images = images;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public Boolean getFlashSale() {
        return flashSale;
    }

    public void setFlashSale(Boolean flashSale) {
        this.flashSale = flashSale;
    }

    public Integer getFlashSaleQuantity() {
        return flashSaleQuantity;
    }

    public void setFlashSaleQuantity(Integer flashSaleQuantity) {
        this.flashSaleQuantity = flashSaleQuantity;
    }

    public Integer getFlashSaleSold() {
        return flashSaleSold;
    }

    public void setFlashSaleSold(Integer flashSaleSold) {
        this.flashSaleSold = flashSaleSold;
    }

    public Integer getRemain() {
        return remain;
    }

    public void setRemain(Integer remain) {
        this.remain = remain;
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

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public List<ProductVariantResponse> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantResponse> variants) {
        this.variants = variants;
    }
}
