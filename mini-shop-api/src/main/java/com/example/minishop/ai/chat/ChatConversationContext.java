package com.example.minishop.ai.chat;

import com.example.minishop.dto.response.ProductResponse;

import java.util.List;

public record ChatConversationContext(
        List<ProductResponse> lastProducts,
        ProductResponse lastSelectedProduct
) {
}