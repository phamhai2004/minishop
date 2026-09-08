package com.example.minishop.controller.admin;

import com.example.minishop.constant.ShopStatus;
import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.RejectShopRequest;
import com.example.minishop.dto.response.ShopResponse;
import com.example.minishop.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shops")
public class AdminShopController {

    private final ShopService shopService;

    public AdminShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PatchMapping("/{shopId}/approve")
    public ResponseEntity<ApiResponse<ShopResponse>> approveShop(
            @PathVariable Long shopId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Duyệt shop thành công",
                        shopService.approveShop(shopId)
                )
        );
    }

    @PatchMapping("/{shopId}/verify")
    public ResponseEntity<ApiResponse<ShopResponse>> verifyShop(
            @PathVariable Long shopId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xác minh shop thành công",
                        shopService.verifyShop(shopId)
                )
        );
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<?> getShop(
            @PathVariable Long shopId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopService.getAdminShopById(
                                shopId
                        )
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getShops(
            @RequestParam(required = false)
            ShopStatus status,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopService.getAdminShops(
                                status,
                                pageable
                        )
                )
        );
    }

    @PatchMapping("/{shopId}/reject")
    public ResponseEntity<?> rejectShop(
            @PathVariable Long shopId,
            @Valid @RequestBody RejectShopRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopService.rejectShop(
                                shopId,
                                request.getReason()
                        )
                )
        );
    }
}