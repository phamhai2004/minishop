package com.example.minishop.ai.recommendation;

import com.example.minishop.entity.Product;

import java.util.List;

public interface PurchaseHistoryService {

    List<Product> getPurchasedProducts(Long userId);

}