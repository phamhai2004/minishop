package com.example.minishop.dto.response;

public class ProductOptionValueResponse {

    private Long id;
    private String name;
    private Long optionTypeId;
    private String optionTypeName;

    public ProductOptionValueResponse() {
    }

    public ProductOptionValueResponse(
            Long id,
            String name,
            Long optionTypeId,
            String optionTypeName
    ) {
        this.id = id;
        this.name = name;
        this.optionTypeId = optionTypeId;
        this.optionTypeName = optionTypeName;
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