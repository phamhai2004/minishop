package com.example.minishop.service;

import com.example.minishop.entity.FlashSale;
import com.example.minishop.entity.Product;
import com.example.minishop.repository.FlashSaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PricingService {

    private final FlashSaleRepository flashSaleRepository;

    public PricingService(FlashSaleRepository flashSaleRepository) {
        this.flashSaleRepository = flashSaleRepository;
    }

    public BigDecimal getCurrentPrice(Product product) {
        FlashSale flashSale = getActiveFlashSale(product);

        if (flashSale == null) {
            return product.getPrice();
        }

        return flashSale.getSalePrice();
    }

    public FlashSale getActiveFlashSale(Product product) {
        LocalDateTime now = LocalDateTime.now();

        return flashSaleRepository
                .findActiveAvailableFlashSale(
                        product.getId(),
                        now
                )
                .orElse(null);
    }
}