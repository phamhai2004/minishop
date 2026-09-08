package com.example.minishop.mapper;

import com.example.minishop.dto.request.ProductRequest;
import com.example.minishop.dto.response.ProductImageResponse;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.dto.response.ProductVariantOptionResponse;
import com.example.minishop.dto.response.ProductVariantResponse;
import com.example.minishop.entity.*;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        Product product = new Product();

        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setDescription(request.getDescription());

        return product;
    }

    public ProductResponse toResponse(
            Product product,
            Category category
    ) {
        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setQuantity(calculateTotalQuantity(product));
        response.setDescription(product.getDescription());

        if (category != null) {
            response.setCategoryId(category.getId());
            response.setCategoryName(category.getName());
        }

        response.setImages(
                product.getImages()
                        .stream()
                        .map(this::toImageResponse)
                        .toList()
        );

        response.setVariants(
                product.getVariants()
                        .stream()
                        .map(this::toVariantResponse)
                        .toList()
        );

        return response;
    }

    private ProductImageResponse toImageResponse(
            ProductImage image
    ) {
        ProductImageResponse response =
                new ProductImageResponse();

        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setPrimaryImage(image.getPrimaryImage());
        response.setDisplayOrder(image.getDisplayOrder());

        return response;
    }

    private ProductVariantResponse toVariantResponse(
            ProductVariant variant
    ) {
        ProductVariantResponse response =
                new ProductVariantResponse();

        response.setId(variant.getId());
        response.setPrice(variant.getPrice());
        response.setQuantity(variant.getQuantity());
        response.setSku(variant.getSku());

        response.setOptions(
                variant.getOptions()
                        .stream()
                        .map(this::toVariantOptionResponse)
                        .toList()
        );

        return response;
    }

    private ProductVariantOptionResponse toVariantOptionResponse(
            ProductVariantOption variantOption
    ) {
        ProductVariantOptionResponse response =
                new ProductVariantOptionResponse();

        ProductOptionValue optionValue =
                variantOption.getOptionValue();

        response.setOptionValueId(optionValue.getId());
        response.setOptionValueName(optionValue.getName());

        if (optionValue.getOptionType() != null) {
            response.setOptionTypeId(
                    optionValue.getOptionType().getId()
            );

            response.setOptionTypeName(
                    optionValue.getOptionType().getName()
            );
        }

        return response;
    }

    private Integer calculateTotalQuantity(
            Product product
    ) {
        if (product.getVariants() == null
                || product.getVariants().isEmpty()) {

            return product.getQuantity() != null
                    ? product.getQuantity()
                    : 0;
        }

        return product.getVariants()
                .stream()
                .mapToInt(variant ->
                        variant.getQuantity() != null
                                ? variant.getQuantity()
                                : 0
                )
                .sum();
    }

    public void updateEntity(
            Product product,
            ProductRequest request
    ) {
        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setDescription(request.getDescription());
    }
}