package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.AddWishlistRequest;
import com.example.minishop.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(
            WishlistService wishlistService
    ) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<?> getMyWishlist(){

        return ResponseEntity.ok(
                ApiResponse.success(
                        wishlistService.getMyWishlist()
                )
        );

    }

    @PostMapping
    public ResponseEntity<?> add(
            @Valid
            @RequestBody
            AddWishlistRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Đã thêm sản phẩm vào danh sách yêu thích",
                                wishlistService.add(
                                        request
                                )
                        )
                );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> remove(
            @PathVariable Long productId
    ){

        wishlistService.remove(productId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã xóa sản phẩm khỏi danh sách yêu thích",
                        null
                )
        );

    }

    @GetMapping("/count/{productId}")
    public ResponseEntity<?> countByProduct(
            @PathVariable
            Long productId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        wishlistService
                                .countByProductId(
                                        productId
                                )
                )
        );
    }

}