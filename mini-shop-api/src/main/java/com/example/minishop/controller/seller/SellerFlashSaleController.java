package com.example.minishop.controller.seller;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.SellerFlashSaleRequest;
import com.example.minishop.dto.response.SellerFlashSaleResponse;
import com.example.minishop.service.SellerFlashSaleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller/flash-sales")
public class SellerFlashSaleController {

    private final SellerFlashSaleService sellerFlashSaleService;

    public SellerFlashSaleController(
            SellerFlashSaleService sellerFlashSaleService
    ) {
        this.sellerFlashSaleService = sellerFlashSaleService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody SellerFlashSaleRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo Flash Sale thành công",
                                sellerFlashSaleService.create(request)
                        )
                );
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "id"
                        )
                );

        Page<SellerFlashSaleResponse> result =
                sellerFlashSaleService.getAll(
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách Flash Sale thành công",
                        result
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            SellerFlashSaleRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật Flash Sale thành công",
                        sellerFlashSaleService.update(
                                id,
                                request
                        )
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tắt Flash Sale thành công",
                        sellerFlashSaleService.deactivate(
                                id
                        )
                )
        );
    }
}