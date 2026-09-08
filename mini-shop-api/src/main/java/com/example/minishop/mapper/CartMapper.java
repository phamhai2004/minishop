package com.example.minishop.mapper;

import com.example.minishop.dto.response.CartItemResponse;
import com.example.minishop.dto.response.CartResponse;
import com.example.minishop.dto.response.ProductVariantOptionResponse;
import com.example.minishop.dto.response.ProductVariantResponse;
import com.example.minishop.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();

        response.setCartId(cart.getId());
        response.setUserId(cart.getUser().getId());

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        response.setItems(items);

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setTotalAmount(total);

        return response;
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();

        CartItemResponse response = new CartItemResponse();

        response.setProductId(product.getId());
        response.setProductName(product.getName());

        response.setVariantId(
                variant != null ? variant.getId() : null
        );

        response.setVariant(
                variant != null
                        ? toVariantResponse(variant)
                        : null
        );

        BigDecimal price = variant != null
                ? variant.getPrice()
                : product.getPrice();

        response.setPrice(price);
        response.setQuantity(item.getQuantity());

        response.setSubtotal(
                price.multiply(
                        BigDecimal.valueOf(item.getQuantity())
                )
        );

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
                        .map(option -> {

                            ProductVariantOptionResponse optionResponse =
                                    new ProductVariantOptionResponse();

                            ProductOptionValue optionValue =
                                    option.getOptionValue();

                            optionResponse.setOptionValueId(
                                    optionValue.getId()
                            );

                            optionResponse.setOptionValueName(
                                    optionValue.getName()
                            );

                            optionResponse.setOptionTypeId(
                                    optionValue.getOptionType().getId()
                            );

                            optionResponse.setOptionTypeName(
                                    optionValue.getOptionType().getName()
                            );

                            return optionResponse;
                        })
                        .toList()
        );

        return response;
    }
}
