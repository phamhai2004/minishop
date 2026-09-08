package com.example.minishop.ai.chat;

import java.math.BigDecimal;

public record ChatQuery(
        String keyword,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal budgetTarget,
        String purpose
) {
}