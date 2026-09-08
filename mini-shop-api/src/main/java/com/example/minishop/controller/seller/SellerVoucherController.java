package com.example.minishop.controller.seller;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.SellerVoucherRequest;
import com.example.minishop.dto.response.VoucherResponse;
import com.example.minishop.service.SellerVoucherService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller/vouchers")
public class SellerVoucherController {

    private final SellerVoucherService sellerVoucherService;

    public SellerVoucherController(
            SellerVoucherService sellerVoucherService
    ) {
        this.sellerVoucherService = sellerVoucherService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> create(
            @Valid @RequestBody SellerVoucherRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo voucher của shop thành công",
                                sellerVoucherService.create(request)
                        )
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getMyVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sellerVoucherService.getMyVouchers(page, size)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SellerVoucherRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật voucher thành công",
                        sellerVoucherService.update(id, request)
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<VoucherResponse>> deactivate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã tắt voucher",
                        sellerVoucherService.deactivate(id)
                )
        );
    }
}