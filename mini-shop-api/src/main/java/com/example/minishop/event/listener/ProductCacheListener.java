package com.example.minishop.event.listener;

import com.example.minishop.event.ProductStockChangedEvent;
import com.example.minishop.service.ProductCacheService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProductCacheListener {

    private final ProductCacheService productCacheService;

    public ProductCacheListener(
            ProductCacheService productCacheService
    ) {
        this.productCacheService = productCacheService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(ProductStockChangedEvent event) {
        event.productIds().forEach(
                productCacheService::evictProduct
        );
    }
}