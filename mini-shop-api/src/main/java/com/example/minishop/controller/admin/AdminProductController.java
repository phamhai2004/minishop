package com.example.minishop.controller.admin;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.ProductRequest;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getSummary()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getById(id)
                )
        );
    }

    @GetMapping("/stock/greater-than")
    public ResponseEntity<?> getProductsByQuantityGreaterThan(
            @RequestParam Integer quantity
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProductsByQuantityGreaterThan(
                                quantity
                        )
                )
        );
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStock(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getStock(id)
                )
        );
    }
}