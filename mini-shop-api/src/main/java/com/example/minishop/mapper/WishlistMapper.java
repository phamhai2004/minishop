package com.example.minishop.mapper;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.dto.response.WishlistResponse;
import com.example.minishop.entity.Product;
import com.example.minishop.entity.ProductImage;
import com.example.minishop.entity.Shop;
import com.example.minishop.entity.Wishlist;
import org.springframework.stereotype.Component;

@Component
public class WishlistMapper {

    public WishlistResponse toResponse(
            Wishlist wishlist
    ) {

        Product product =
                wishlist.getProduct();

        Shop shop =
                product.getShop();

        WishlistResponse response =
                new WishlistResponse();

        response.setId(
                wishlist.getId()
        );

        response.setProductId(
                product.getId()
        );

        response.setProductName(
                product.getName()
        );

        response.setPrice(
                product.getPrice()
        );

        response.setImageUrl(
                product.getImages()
                        .stream()
                        .findFirst()
                        .map(
                                ProductImage::getImageUrl
                        )
                        .orElse(null)
        );

        if (shop != null) {
            response.setShopId(
                    shop.getId()
            );

            response.setShopName(
                    shop.getName()
            );
        }

        response.setProductStatus(
                product.getStatus()
        );

        if (product.getStatus() != null) {
            response.setProductStatusName(
                    product.getStatus()
                            .getDisplayName()
            );
        }

        boolean available =
                product.getStatus()
                        == ProductStatus.ACTIVE
                        &&
                        shop != null
                        &&
                        shop.getStatus()
                                == ShopStatus.ACTIVE;

        response.setAvailable(
                available
        );

        response.setCreatedAt(
                wishlist.getCreatedAt()
        );

        return response;
    }
}