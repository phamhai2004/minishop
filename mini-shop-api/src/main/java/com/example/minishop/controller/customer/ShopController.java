package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.CreateShopRequest;
import com.example.minishop.dto.request.UpdateShopRegistrationRequest;
import com.example.minishop.dto.response.ShopCategoryResponse;
import com.example.minishop.dto.response.ShopResponse;
import com.example.minishop.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(
            ShopService shopService
    ) {
        this.shopService = shopService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShopResponse>>
    registerShop(
            @Valid
            @RequestBody
            CreateShopRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Đăng ký shop thành công, vui lòng chờ xét duyệt",
                                shopService.registerShop(request)
                        )
                );
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResponse>>
    getById(
            @PathVariable Long shopId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopService.getById(shopId)
                )
        );
    }

    @GetMapping("/{shopId}/categories")
    public ResponseEntity<
            ApiResponse<List<ShopCategoryResponse>>
            >
    getShopCategories(
            @PathVariable Long shopId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopService.getShopCategories(
                                shopId
                        )
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyShop() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopService.getMyRegisteredShop()
                )
        );
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyRegistration(
            @Valid
            @RequestBody
            UpdateShopRegistrationRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopService.updateMyRegistration(
                                request
                        )
                )
        );
    }

    @PatchMapping("/me/resubmit")
    public ResponseEntity<?> resubmitMyShop() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopService.resubmitMyShop()
                )
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getShopsByCategory(
            @PathVariable Long categoryId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "6")
            int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shopService
                                .getPublicShopsByCategory(
                                        categoryId,
                                        page,
                                        size
                                )
                )
        );
    }
}