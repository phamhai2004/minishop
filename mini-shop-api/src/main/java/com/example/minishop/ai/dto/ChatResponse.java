package com.example.minishop.ai.dto;

import com.example.minishop.dto.response.ProductResponse;

import java.util.List;

public record ChatResponse(
        String message,
        List<ProductResponse> products
) {
}