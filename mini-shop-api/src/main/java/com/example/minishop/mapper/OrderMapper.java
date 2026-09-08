package com.example.minishop.mapper;

import com.example.minishop.dto.response.OrderItemResponse;
import com.example.minishop.dto.response.OrderResponse;
import com.example.minishop.dto.response.ProductVariantOptionResponse;
import com.example.minishop.dto.response.ProductVariantResponse;
import com.example.minishop.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setStatusName(order.getStatus().getDisplayName());
        response.setCreatedAt(order.getCreatedAt());

        response.setItems(
                order.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList()
        );

        if (order.getUser() != null) {
            response.setUserId(order.getUser().getId());
            response.setCustomerName(order.getUser().getFullName());
            response.setCustomerEmail(order.getUser().getEmail());
            response.setCustomerPhone(order.getUser().getPhone());
        }

        mapAddress(order, response);

        response.setVoucherCode(order.getVoucherCode());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setFinalAmount(order.getFinalAmount());

        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setPaidAt(order.getPaidAt());

        return response;
    }

    private void mapAddress(
            Order order,
            OrderResponse response
    ) {
        Address address = order.getAddress();

        if (address == null) {
            return;
        }

        response.setReceiverName(address.getReceiverName());
        response.setReceiverPhone(address.getPhone());

        response.setShippingAddress(
                String.join(
                        ", ",
                        address.getDetail(),
                        address.getWard(),
                        address.getProvince()
                )
        );
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();

        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setImageUrl(getPrimaryImageUrl(item.getProduct()));

        ProductVariant variant = item.getVariant();

        response.setVariantId(
                variant != null
                        ? variant.getId()
                        : null
        );

        response.setVariant(
                variant != null
                        ? toVariantResponse(variant)
                        : null
        );

        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setSubtotal(item.getSubtotal());

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
    private String getPrimaryImageUrl(Product product) {
        if (product == null || product.getImages() == null) {
            return null;
        }

        return product.getImages()
                .stream()
                .filter(image -> Boolean.TRUE.equals(image.getPrimaryImage()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElseGet(() ->
                        product.getImages()
                                .stream()
                                .map(ProductImage::getImageUrl)
                                .filter(url -> url != null && !url.isBlank())
                                .findFirst()
                                .orElse(null)
                );
    }
}