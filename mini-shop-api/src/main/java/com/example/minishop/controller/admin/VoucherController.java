package com.example.minishop.controller.admin;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.VoucherRequest;
import com.example.minishop.dto.response.VoucherResponse;
import com.example.minishop.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> create(
            @Valid @RequestBody VoucherRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Tạo voucher thành công", voucherService.create(request))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(voucherService.getAll())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<
            ApiResponse<VoucherResponse>
            >
    update(
            @PathVariable Long id,
            @Valid @RequestBody VoucherRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật voucher thành công",
                        voucherService.update(
                                id,
                                request
                        )
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<
            ApiResponse<VoucherResponse>
            >
    deactivate(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã tắt voucher",
                        voucherService.deactivate(id)
                )
        );
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(voucherService.getByCode(code))
        );
    }
}