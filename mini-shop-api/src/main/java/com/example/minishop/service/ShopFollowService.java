package com.example.minishop.service;

import com.example.minishop.constant.ShopStatus;
import com.example.minishop.dto.response.FollowingShopResponse;
import com.example.minishop.dto.response.ShopFollowStatusResponse;
import com.example.minishop.entity.Shop;
import com.example.minishop.entity.ShopFollow;
import com.example.minishop.entity.User;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.repository.ShopFollowRepository;
import com.example.minishop.repository.ShopRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShopFollowService {

    private final ShopFollowRepository
            shopFollowRepository;

    private final ShopRepository
            shopRepository;

    public ShopFollowService(
            ShopFollowRepository shopFollowRepository,
            ShopRepository shopRepository
    ) {
        this.shopFollowRepository =
                shopFollowRepository;

        this.shopRepository =
                shopRepository;
    }

    @Transactional
    public ShopFollowStatusResponse follow(
            Long shopId
    ) {
        User currentUser =
                SecurityUtils
                        .getCurrentUser()
                        .getUser();

        Shop shop =
                getActiveShop(shopId);

        if (
                shopFollowRepository
                        .existsByUser_IdAndShop_Id(
                                currentUser.getId(),
                                shopId
                        )
        ) {
            return buildStatus(
                    currentUser.getId(),
                    shop
            );
        }

        ShopFollow follow =
                new ShopFollow();

        follow.setUser(currentUser);
        follow.setShop(shop);
        follow.setCreatedAt(
                LocalDateTime.now()
        );

        shopFollowRepository.save(follow);
        shopFollowRepository.flush();

        syncFollowerCount(shop);

        return buildStatus(
                currentUser.getId(),
                shop
        );
    }

    @Transactional
    public ShopFollowStatusResponse unfollow(
            Long shopId
    ) {
        Long userId =
                SecurityUtils
                        .getCurrentUserId();

        Shop shop =
                getActiveShop(shopId);

        shopFollowRepository
                .findByUser_IdAndShop_Id(
                        userId,
                        shopId
                )
                .ifPresent(follow -> {
                    shopFollowRepository
                            .delete(follow);

                    shopFollowRepository
                            .flush();
                });

        syncFollowerCount(shop);

        return buildStatus(
                userId,
                shop
        );
    }

    @Transactional(readOnly = true)
    public ShopFollowStatusResponse getStatus(
            Long shopId
    ) {
        Long userId =
                SecurityUtils
                        .getCurrentUserId();

        Shop shop =
                getActiveShop(shopId);

        return buildStatus(
                userId,
                shop
        );
    }

    @Transactional(readOnly = true)
    public List<FollowingShopResponse>
    getMyFollowingShops() {

        Long userId =
                SecurityUtils.getCurrentUserId();

        return shopFollowRepository
                .findByUser_IdOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::toFollowingShopResponse)
                .toList();
    }

    private FollowingShopResponse
    toFollowingShopResponse(
            ShopFollow follow
    ) {
        Shop shop = follow.getShop();

        FollowingShopResponse response =
                new FollowingShopResponse();

        response.setShopId(
                shop.getId()
        );

        response.setShopName(
                shop.getName()
        );

        response.setLogoUrl(
                shop.getLogoUrl()
        );

        response.setStatusName(
                shop.getStatus() != null
                        ? shop.getStatus().getDisplayName()
                        : null
        );

        response.setVerified(
                shop.getVerified()
        );

        response.setFollowedAt(
                follow.getCreatedAt()
        );

        return response;
    }

    private Shop getActiveShop(
            Long shopId
    ) {
        Shop shop =
                shopRepository
                        .findById(shopId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy shop với id: "
                                                        + shopId
                                        )
                        );

        if (
                shop.getStatus()
                        != ShopStatus.ACTIVE
        ) {
            throw new ResourceNotFoundException(
                    "Shop hiện không hoạt động"
            );
        }

        return shop;
    }

    private ShopFollowStatusResponse buildStatus(
            Long userId,
            Shop shop
    ) {
        boolean following =
                shopFollowRepository
                        .existsByUser_IdAndShop_Id(
                                userId,
                                shop.getId()
                        );

        long totalFollowers =
                shopFollowRepository
                        .countByShop_Id(
                                shop.getId()
                        );

        return new ShopFollowStatusResponse(
                following,
                totalFollowers
        );
    }

    private void syncFollowerCount(
            Shop shop
    ) {
        long total =
                shopFollowRepository
                        .countByShop_Id(
                                shop.getId()
                        );

        shop.setTotalFollowers(
                Math.toIntExact(total)
        );
    }
}