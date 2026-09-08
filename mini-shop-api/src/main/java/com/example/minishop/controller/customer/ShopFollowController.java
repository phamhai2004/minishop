package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.response.FollowingShopResponse;
import com.example.minishop.dto.response.ShopFollowStatusResponse;
import com.example.minishop.service.ShopFollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop-follows")
public class ShopFollowController {

    private final ShopFollowService
            shopFollowService;

    public ShopFollowController(
            ShopFollowService shopFollowService
    ) {
        this.shopFollowService =
                shopFollowService;
    }

    @PostMapping("/{shopId}")
    public ResponseEntity<
            ApiResponse<ShopFollowStatusResponse>
            > follow(
            @PathVariable Long shopId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Theo dõi shop thành công",
                        shopFollowService
                                .follow(shopId)
                )
        );
    }

    @DeleteMapping("/{shopId}")
    public ResponseEntity<
            ApiResponse<ShopFollowStatusResponse>
            > unfollow(
            @PathVariable Long shopId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Bỏ theo dõi shop thành công",
                        shopFollowService
                                .unfollow(shopId)
                )
        );
    }

    @GetMapping("/{shopId}/status")
    public ResponseEntity<
            ApiResponse<ShopFollowStatusResponse>
            > getStatus(
            @PathVariable Long shopId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shopFollowService
                                .getStatus(shopId)
                )
        );
    }

    @GetMapping("/my")
    public ResponseEntity<
            ApiResponse<List<FollowingShopResponse>>
            >
    getMyFollowingShops() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopFollowService
                                .getMyFollowingShops()
                )
        );
    }
}