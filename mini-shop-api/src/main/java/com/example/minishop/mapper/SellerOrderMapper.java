package com.example.minishop.mapper;

import com.example.minishop.dto.response.ProductVariantOptionResponse;
import com.example.minishop.dto.response.ProductVariantResponse;
import com.example.minishop.dto.response.SellerOrderItemResponse;
import com.example.minishop.dto.response.SellerOrderResponse;
import com.example.minishop.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SellerOrderMapper {

    public SellerOrderResponse toResponse(ShopOrder shopOrder) {
        SellerOrderResponse response = new SellerOrderResponse();

        response.setShopOrderId(shopOrder.getId());
        response.setOrderId(shopOrder.getOrder().getId());
        response.setOrderCode(
                shopOrder.getOrderCode()
        );

        response.setStatus(shopOrder.getStatus());
        response.setStatusName(
                shopOrder.getStatus().getDisplayName()
        );

        response.setCustomerName(
                shopOrder.getOrder().getUser().getFullName()
        );

        response.setCustomerPhone(
                shopOrder.getOrder().getUser().getPhone()
        );

        response.setCustomerAvatarUrl(
                shopOrder.getOrder()
                        .getUser()
                        .getAvatarUrl()
        );

        response.setPaymentMethod(
                shopOrder.getPaymentMethod()
        );

        response.setPaymentStatus(
                shopOrder.getPaymentStatus()
        );

        response.setSubtotal(shopOrder.getSubtotal());
        response.setDiscountAmount(shopOrder.getDiscountAmount());
        response.setShippingFee(shopOrder.getShippingFee());
        response.setFinalAmount(shopOrder.getFinalAmount());
        response.setCreatedAt(shopOrder.getCreatedAt());

        mapAddress(response, shopOrder);

        List<SellerOrderItemResponse> items = shopOrder.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        response.setItems(items);

        return response;
    }

    private SellerOrderItemResponse toItemResponse(OrderItem item) {
        SellerOrderItemResponse response =
                new SellerOrderItemResponse();

        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        ProductVariant variant =
                item.getVariant();

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
        response.setImageUrl(
                item.getProduct()
                        .getImages()
                        .stream()
                        .filter(image -> Boolean.TRUE.equals(image.getPrimaryImage()))
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElseGet(() ->
                                item.getProduct()
                                        .getImages()
                                        .stream()
                                        .findFirst()
                                        .map(ProductImage::getImageUrl)
                                        .orElse(null)
                        )
        );
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setSubtotal(item.getSubtotal());

        return response;
    }

    private void mapAddress(
            SellerOrderResponse response,
            ShopOrder shopOrder
    ) {
        Address address = shopOrder.getOrder().getAddress();

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

    private ProductVariantResponse toVariantResponse(
            ProductVariant variant
    ) {

        ProductVariantResponse response =
                new ProductVariantResponse();

        response.setId(
                variant.getId()
        );

        response.setPrice(
                variant.getPrice()
        );

        response.setQuantity(
                variant.getQuantity()
        );

        response.setSku(
                variant.getSku()
        );

        response.setOptions(
                variant.getOptions()
                        .stream()
                        .map(option -> {

                            ProductVariantOptionResponse
                                    optionResponse =
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
                                    optionValue
                                            .getOptionType()
                                            .getId()
                            );

                            optionResponse.setOptionTypeName(
                                    optionValue
                                            .getOptionType()
                                            .getName()
                            );

                            return optionResponse;
                        })
                        .toList()
        );

        return response;
    }
}