package com.example.minishop.mapper;

import com.example.minishop.dto.response.PublicFlashSaleResponse;
import com.example.minishop.dto.response.SellerFlashSaleResponse;
import com.example.minishop.entity.FlashSale;
import com.example.minishop.entity.Product;
import com.example.minishop.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FlashSaleMapper {

    public SellerFlashSaleResponse toSellerResponse(
            FlashSale flashSale
    ) {
        SellerFlashSaleResponse response =
                new SellerFlashSaleResponse();

        response.setId(flashSale.getId());
        response.setSalePrice(flashSale.getSalePrice());
        response.setQuantity(flashSale.getQuantity());
        response.setSold(
                flashSale.getSold() != null
                        ? flashSale.getSold()
                        : 0
        );
        response.setRemainingQuantity(
                calculateRemainingQuantity(flashSale)
        );
        response.setStartTime(flashSale.getStartTime());
        response.setEndTime(flashSale.getEndTime());
        response.setActive(flashSale.getActive());
        response.setStatus(
                calculateStatus(flashSale)
        );

        Product product = flashSale.getProduct();

        if (product != null) {
            response.setProductId(product.getId());
            response.setProductName(product.getName());
            response.setOriginalPrice(product.getPrice());
            response.setProductImage(
                    findPrimaryImage(product)
            );
        }

        return response;
    }

    private Integer calculateRemainingQuantity(
            FlashSale flashSale
    ) {
        int quantity =
                flashSale.getQuantity() != null
                        ? flashSale.getQuantity()
                        : 0;

        int sold =
                flashSale.getSold() != null
                        ? flashSale.getSold()
                        : 0;

        return Math.max(quantity - sold, 0);
    }

    private String calculateStatus(
            FlashSale flashSale
    ) {
        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(
                flashSale.getActive()
        )) {
            return "INACTIVE";
        }

        if (flashSale.getEndTime() != null
                && now.isAfter(
                flashSale.getEndTime()
        )) {
            return "ENDED";
        }

        if (flashSale.getStartTime() != null
                && now.isBefore(
                flashSale.getStartTime()
        )) {
            return "UPCOMING";
        }

        if (calculateRemainingQuantity(
                flashSale
        ) <= 0) {
            return "SOLD_OUT";
        }

        return "ONGOING";
    }

    private String findPrimaryImage(
            Product product
    ) {
        if (product.getImages() == null
                || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages()
                .stream()
                .filter(image ->
                        Boolean.TRUE.equals(
                                image.getPrimaryImage()
                        )
                )
                .findFirst()
                .orElse(
                        product.getImages().get(0)
                )
                .getImageUrl();
    }

    public PublicFlashSaleResponse
    toPublicResponse(
            FlashSale flashSale
    ) {

        PublicFlashSaleResponse response =
                new PublicFlashSaleResponse();

        response.setId(flashSale.getId());
        response.setSalePrice(flashSale.getSalePrice());
        response.setQuantity(flashSale.getQuantity());

        response.setSold(flashSale.getSold() != null
                        ? flashSale.getSold()
                        : 0
        );
        response.setRemainingQuantity(calculateRemainingQuantity(flashSale));
        response.setStartTime(flashSale.getStartTime());
        response.setEndTime(flashSale.getEndTime());
        Product product = flashSale.getProduct();
        if (product != null) {
            response.setProductId(product.getId());
            response.setProductName(product.getName());
            response.setOriginalPrice(product.getPrice());
            response.setProductImage(findPrimaryImage(product));
        }

        return response;
    }
}