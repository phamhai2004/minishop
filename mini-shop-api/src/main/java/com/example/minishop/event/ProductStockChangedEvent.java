package com.example.minishop.event;

import java.util.Set;

public record ProductStockChangedEvent(
        Set<Long> productIds
) {
}