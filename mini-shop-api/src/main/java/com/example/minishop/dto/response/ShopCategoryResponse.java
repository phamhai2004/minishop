package com.example.minishop.dto.response;

public class ShopCategoryResponse {

    private Long categoryId;
    private String categoryName;
    private Long productCount;

    public ShopCategoryResponse(
            Long categoryId,
            String categoryName,
            Long productCount
    ) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productCount = productCount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getProductCount() {
        return productCount;
    }
}