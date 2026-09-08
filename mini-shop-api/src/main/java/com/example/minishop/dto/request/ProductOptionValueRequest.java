package com.example.minishop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductOptionValueRequest {

    @NotBlank(message = "Tên giá trị phân loại không được để trống")
    @Size(max = 100, message = "Tên giá trị không được vượt quá 100 ký tự")
    private String name;

    @NotNull(message = "optionTypeId không được để trống")
    private Long optionTypeId;

    public ProductOptionValueRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOptionTypeId() {
        return optionTypeId;
    }

    public void setOptionTypeId(Long optionTypeId) {
        this.optionTypeId = optionTypeId;
    }
}