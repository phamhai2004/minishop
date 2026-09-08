package com.example.minishop.controller.seller;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.ProductRequest;
import com.example.minishop.dto.request.UpdateProductStatusRequest;
import com.example.minishop.dto.response.ProductImageResponse;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.service.ProductImageService;
import com.example.minishop.service.SellerProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/seller/products")
@SecurityRequirement(name = "bearerAuth")
public class SellerProductController {

    private final SellerProductService sellerProductService;
    private final ProductImageService productImageService;

    public SellerProductController(
            SellerProductService sellerProductService,
            ProductImageService productImageService
    ) {
        this.sellerProductService = sellerProductService;
        this.productImageService = productImageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sellerProductService.getMyProducts(
                                page,
                                size,
                                sort,
                                direction
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
                        sellerProductService.getMyProductById(id)
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo sản phẩm thành công",
                                sellerProductService.create(request)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật sản phẩm thành công",
                        sellerProductService.update(id, request)
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật trạng thái sản phẩm thành công",
                        sellerProductService.updateStatus(id, request)
                )
        );
    }

    @PatchMapping("/{id}/discontinue")
    public ResponseEntity<ApiResponse<ProductResponse>> discontinue(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã ngừng kinh doanh sản phẩm",
                        sellerProductService.discontinue(id)
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchMyProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sellerProductService.searchMyProducts(
                                keyword,
                                page,
                                size,
                                sort,
                                direction
                        )
                )
        );
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countMyProducts() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sellerProductService.countMyProducts()
                )
        );
    }

    @PostMapping(
            value = "/{productId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadImages(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tải ảnh sản phẩm thành công",
                        productImageService.uploadImages(
                                productId,
                                files
                        )
                )
        );
    }

    @PatchMapping("/images/{imageId}/primary")
    public ResponseEntity<ApiResponse<ProductImageResponse>> setPrimary(
            @PathVariable Long imageId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đặt ảnh đại diện sản phẩm thành công",
                        productImageService.setPrimaryImage(imageId)
                )
        );
    }
    
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long imageId
    ) {
        productImageService.deleteImage(imageId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xóa ảnh sản phẩm thành công",
                        null
                )
        );
    }
}