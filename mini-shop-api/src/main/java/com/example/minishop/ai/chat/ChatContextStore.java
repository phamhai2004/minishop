package com.example.minishop.ai.chat;

import com.example.minishop.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatContextStore {

    private final Map<Long, PendingRecommendationContext>
            pendingRecommendations =
            new ConcurrentHashMap<>();

    public void save(
            Long userId,
            PendingRecommendationContext context
    ) {

        if (userId == null || context == null) {
            return;
        }

        pendingRecommendations.put(
                userId,
                context
        );
    }

    public PendingRecommendationContext get(
            Long userId
    ) {

        if (userId == null) {
            return null;
        }

        return pendingRecommendations.get(
                userId
        );
    }

    public PendingRecommendationContext remove(
            Long userId
    ) {

        if (userId == null) {
            return null;
        }

        return pendingRecommendations.remove(
                userId
        );
    }

    public void clear(
            Long userId
    ) {

        if (userId != null) {
            pendingRecommendations.remove(
                    userId
            );
        }
    }

    private final Map<Long, ChatConversationContext>
            conversationContexts =
            new ConcurrentHashMap<>();

    public void saveLastProducts(
            Long userId,
            List<ProductResponse> products
    ) {

        if (userId == null
                || products == null
                || products.isEmpty()) {
            return;
        }

        ChatConversationContext current =
                conversationContexts.get(
                        userId
                );

        ProductResponse selected =
                current != null
                        ? current.lastSelectedProduct()
                        : null;

        conversationContexts.put(
                userId,
                new ChatConversationContext(
                        List.copyOf(products),
                        selected
                )
        );
    }

    public void saveLastSelectedProduct(
            Long userId,
            ProductResponse product
    ) {

        if (userId == null
                || product == null) {
            return;
        }

        ChatConversationContext current =
                conversationContexts.get(
                        userId
                );

        List<ProductResponse> products =
                current != null
                        && current.lastProducts() != null
                        ? current.lastProducts()
                        : List.of();

        conversationContexts.put(
                userId,
                new ChatConversationContext(
                        products,
                        product
                )
        );
    }

    public ProductResponse getLastSelectedProduct(
            Long userId
    ) {

        if (userId == null) {
            return null;
        }

        ChatConversationContext context =
                conversationContexts.get(
                        userId
                );

        if (context == null) {
            return null;
        }

        return context.lastSelectedProduct();
    }

    public List<ProductResponse> getLastProducts(
            Long userId
    ) {

        if (userId == null) {
            return List.of();
        }

        ChatConversationContext context =
                conversationContexts.get(
                        userId
                );

        if (context == null
                || context.lastProducts() == null) {
            return List.of();
        }

        return context.lastProducts();
    }

    public void clearConversation(
            Long userId
    ) {

        if (userId != null) {
            conversationContexts.remove(
                    userId
            );
        }
    }
}