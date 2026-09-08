package com.example.minishop.ai.chat;

import java.math.BigDecimal;

public record PendingRecommendationContext(
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal budgetTarget,
        String purpose
) {
}