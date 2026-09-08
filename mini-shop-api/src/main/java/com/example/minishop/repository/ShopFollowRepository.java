package com.example.minishop.repository;

import com.example.minishop.entity.ShopFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopFollowRepository
        extends JpaRepository<ShopFollow, Long> {

    boolean existsByUser_IdAndShop_Id(
            Long userId,
            Long shopId
    );

    Optional<ShopFollow>
    findByUser_IdAndShop_Id(
            Long userId,
            Long shopId
    );

    long countByShop_Id(
            Long shopId
    );

    List<ShopFollow>
    findByUser_IdOrderByCreatedAtDesc(
            Long userId
    );
}