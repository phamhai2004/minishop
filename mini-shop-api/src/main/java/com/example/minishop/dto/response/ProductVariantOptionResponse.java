package com.example.minishop.dto.response;

public class ProductVariantOptionResponse {

    private Long optionValueId;
    private String optionValueName;

    private Long optionTypeId;
    private String optionTypeName;

    public ProductVariantOptionResponse() {
    }

    public Long getOptionValueId() {
        return optionValueId;
    }

    public void setOptionValueId(Long optionValueId) {
        this.optionValueId = optionValueId;
    }

    public String getOptionValueName() {
        return optionValueName;
    }

    public void setOptionValueName(String optionValueName) {
        this.optionValueName = optionValueName;
    }

    public Long getOptionTypeId() {
        return optionTypeId;
    }

    public void setOptionTypeId(Long optionTypeId) {
        this.optionTypeId = optionTypeId;
    }

    public String getOptionTypeName() {
        return optionTypeName;
    }

    public void setOptionTypeName(String optionTypeName) {
        this.optionTypeName = optionTypeName;
    }
}