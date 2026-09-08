package com.example.minishop.controller.seller;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.response.StockMovementResponse;
import com.example.minishop.mapper.StockMovementMapper;
import com.example.minishop.repository.StockMovementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock-movements")
public class StockMovementController {

    private final StockMovementRepository stockMovementRepository;
    private final StockMovementMapper stockMovementMapper;

    public StockMovementController(
            StockMovementRepository stockMovementRepository,
            StockMovementMapper stockMovementMapper
    ) {
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementMapper = stockMovementMapper;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getByProduct(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        stockMovementRepository.findByProduct_IdOrderByCreatedAtDesc(productId)
                                .stream()
                                .map(stockMovementMapper::toResponse)
                                .toList()
                )
        );
    }
}