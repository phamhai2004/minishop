package com.example.minishop.controller;

import com.example.minishop.constant.VoucherScope;
import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.response.VoucherResponse;
import com.example.minishop.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/voucher-catalog")
public class VoucherCatalogController {

    private final VoucherService voucherService;

    public VoucherCatalogController(
            VoucherService voucherService
    ) {
        this.voucherService =
                voucherService;
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<VoucherResponse>>
            >
    getAvailable(
            @RequestParam(
                    required = false
            )
            VoucherScope scope,

            @RequestParam(
                    required = false
            )
            Long shopId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        voucherService
                                .getAvailableCatalog(
                                        scope,
                                        shopId
                                )
                )
        );
    }
}
