package com.example.minishop.repository;

import com.example.minishop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_IdAndProduct_IdAndVariant_Id(
            Long cartId,
            Long productId,
            Long variantId
    );

    Optional<CartItem> findByCart_IdAndProduct_IdAndVariantIsNull(
            Long cartId,
            Long productId
    );
}