package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.ProductSearchRequest;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProducts(page, size, sort, direction)
                )
        );
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "8") int limit
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProductSuggestions(
                                keyword,
                                limit
                        )
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getPublicProductById(id)
                )
        );
    }

    @GetMapping("/{id}/similar")
    public ApiResponse<List<ProductResponse>> getSimilarProducts(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                productService.getSimilarProducts(id)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @ModelAttribute ProductSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc")
            String direction
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.searchProducts(
                                request,
                                page,
                                size,
                                sort,
                                direction
                        )
                )
        );
    }

    @GetMapping("/price/greater-than")
    public ResponseEntity<?> getProductsByPriceGreaterThan(
            @RequestParam BigDecimal price
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProductsByPriceGreaterThan(price)
                )
        );
    }

    @GetMapping("/price/between")
    public ResponseEntity<?> getProductsByPriceBetween(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProductsByPriceBetween(min, max)
                )
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable Long categoryId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "12")
            int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService
                                .getPublicProductsByCategory(
                                        categoryId,
                                        page,
                                        size
                                )
                )
        );
    }

    @GetMapping("/category/{categoryId}/count")
    public ResponseEntity<?> countProductsByCategory(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.countProductsByCategory(categoryId)
                )
        );
    }

    @GetMapping("/category/{categoryId}/price-desc")
    public ResponseEntity<?> getProductsByCategoryOrderByPriceDesc(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProductsByCategoryOrderByPriceDesc(
                                categoryId
                        )
                )
        );
    }

    @GetMapping("/category-name")
    public ResponseEntity<?> findByCategoryName(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.findProductsByCategoryName(name)
                )
        );
    }

    @PostMapping("/search/image")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchByImage(
            @RequestParam MultipartFile file
    ) throws IOException {

        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.searchByImage(file)
                )
        );
    }

}