package com.example.minishop.controller.seller;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.UpdateShopRequest;
import com.example.minishop.dto.response.ShopResponse;
import com.example.minishop.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/seller/shop")
public class SellerShopController {

    private final ShopService shopService;

    public SellerShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ShopResponse>> getMyShop() {
        return ResponseEntity.ok(
                ApiResponse.success(shopService.getMyShop())
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ShopResponse>> updateMyShop(
            @Valid @RequestBody UpdateShopRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật hồ sơ shop thành công",
                        shopService.updateMyShop(request)
                )
        );
    }

    @PostMapping(
            value = "/logo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ShopResponse>> uploadLogo(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật logo shop thành công",
                        shopService.uploadMyLogo(file)
                )
        );
    }

    @PostMapping(
            value = "/cover",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ShopResponse>> uploadCover(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật ảnh bìa shop thành công",
                        shopService.uploadMyCover(file)
                )
        );
    }


}