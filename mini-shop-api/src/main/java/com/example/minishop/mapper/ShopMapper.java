package com.example.minishop.mapper;
import com.example.minishop.dto.request.CreateShopRequest;
import com.example.minishop.dto.response.ShopResponse;
import com.example.minishop.entity.Shop;
import org.springframework.stereotype.Component;
@Component public class ShopMapper {

    public Shop toEntity(CreateShopRequest request) {
        Shop shop = new Shop();

        shop.setName(request.getName().trim());
        shop.setDescription(request.getDescription());
        shop.setLogoUrl(request.getLogoUrl());
        shop.setCoverUrl(request.getCoverUrl());
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());
        shop.setPickupAddress(request.getPickupAddress());

        return shop;
    }
    public ShopResponse toResponse(Shop shop) {
        ShopResponse response = new ShopResponse();
        response.setId(shop.getId());
        response.setName(shop.getName());
        response.setDescription(shop.getDescription());
        response.setLogoUrl(shop.getLogoUrl());
        response.setCoverUrl(shop.getCoverUrl());
        response.setPhone(shop.getPhone());
        response.setEmail(shop.getEmail());
        response.setPickupAddress(shop.getPickupAddress());
        response.setRating(shop.getRating());
        response.setTotalFollowers(shop.getTotalFollowers());
        response.setVerified(shop.getVerified());
        response.setStatus(shop.getStatus());
        response.setStatusName( shop.getStatus().getDisplayName() );
        response.setOwnerId(shop.getOwner().getId());
        response.setOwnerName(shop.getOwner().getFullName());
        response.setCreatedAt(shop.getCreatedAt());
        response.setApprovedAt(shop.getApprovedAt());
        response.setRejectionReason(shop.getRejectionReason());

        return response;
    }
}