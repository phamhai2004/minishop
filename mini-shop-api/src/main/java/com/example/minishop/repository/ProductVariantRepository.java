package com.example.minishop.repository;

import com.example.minishop.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProduct_Id(Long productId);

    void deleteByProduct_Id(Long productId);

    boolean existsByShop_IdAndSku(
            Long shopId,
            String sku
    );

    boolean existsByShop_IdAndSkuAndProduct_IdNot(
            Long shopId,
            String sku,
            Long productId
    );
}