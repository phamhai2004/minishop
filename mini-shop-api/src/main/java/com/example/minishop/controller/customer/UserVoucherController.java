package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.response.UserVoucherResponse;
import com.example.minishop.service.UserVoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/my-vouchers")
public class UserVoucherController {

    private final UserVoucherService userVoucherService;

    public UserVoucherController(UserVoucherService userVoucherService) {
        this.userVoucherService = userVoucherService;
    }

    @PostMapping("/{code}/collect")
    public ResponseEntity<ApiResponse<UserVoucherResponse>> collect(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Đã lưu voucher", userVoucherService.collect(code))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserVoucherResponse>>> getMyVouchers() {
        return ResponseEntity.ok(
                ApiResponse.success(userVoucherService.getMyVouchers())
        );
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<UserVoucherResponse>>> getAvailable() {
        return ResponseEntity.ok(
                ApiResponse.success(userVoucherService.getAvailableVouchers())
        );
    }

    @GetMapping("/used")
    public ResponseEntity<ApiResponse<List<UserVoucherResponse>>>
    getUsed() {

        return ResponseEntity.ok(
                ApiResponse.success(userVoucherService.getUsedVouchers())
        );
    }
}