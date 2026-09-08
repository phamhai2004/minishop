package com.example.minishop.service;

import com.example.minishop.constant.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class ProductCacheService {

    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.PRODUCT_DETAIL,
                    key = "#productId"
            ),
            @CacheEvict(
                    cacheNames = CacheNames.PUBLIC_PRODUCT_DETAIL,
                    key = "#productId"
            ),
            @CacheEvict(
                    cacheNames = CacheNames.ACTIVE_FLASH_SALE,
                    key = "#productId"
            )
    })
    public void evictProduct(Long productId) {
    }
}