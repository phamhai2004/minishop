package com.example.minishop.repository;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    boolean existsByOwner_Id(Long ownerId);

    boolean existsByNameIgnoreCase(String name);

    Optional<Shop> findByOwner_Id(Long ownerId);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            Long id
    );

    Page<Shop> findByStatus(
            ShopStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT s
    FROM Shop s
    JOIN Product p
        ON p.shop = s
    WHERE s.status = :shopStatus
      AND p.status = :productStatus
      AND p.category.id = :categoryId
    ORDER BY s.id DESC
""")
    Page<Shop> findPublicShopsByCategory(
            @Param("categoryId")
            Long categoryId,

            @Param("shopStatus")
            ShopStatus shopStatus,

            @Param("productStatus")
            ProductStatus productStatus,

            Pageable pageable
    );
}