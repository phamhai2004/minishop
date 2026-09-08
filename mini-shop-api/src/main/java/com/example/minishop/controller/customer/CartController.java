package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.AddToCartRequest;
import com.example.minishop.dto.request.UpdateCartItemRequest;
import com.example.minishop.dto.response.CartResponse;
import com.example.minishop.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart() {
        return ResponseEntity.ok(
                ApiResponse.success(cartService.getMyCart())
        );
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Đã thêm vào giỏ hàng", cartService.addToCart(request))
        );
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã cập nhật giỏ hàng",
                        cartService.updateItem(productId, variantId, request)
                )
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã xóa sản phẩm khỏi giỏ",
                        cartService.removeItem(productId, variantId)
                )
        );
    }
}