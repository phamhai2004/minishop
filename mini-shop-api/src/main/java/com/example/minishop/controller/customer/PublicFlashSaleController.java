package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.service.PublicFlashSaleService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flash-sales")
public class PublicFlashSaleController {

    private final PublicFlashSaleService
            publicFlashSaleService;

    public PublicFlashSaleController(
            PublicFlashSaleService publicFlashSaleService
    ) {
        this.publicFlashSaleService =
                publicFlashSaleService;
    }

    @GetMapping
    public ResponseEntity<?> getActiveFlashSales(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "6"
            )
            int size
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        publicFlashSaleService
                                .getActiveFlashSales(
                                        page,
                                        size
                                )
                )
        );
    }
}