package com.example.minishop.repository;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.entity.Product;
import com.example.minishop.entity.Wishlist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository
        extends JpaRepository<
        Wishlist,
        Long
        > {

    @EntityGraph(
            attributePaths = {
                    "product",
                    "product.images",
                    "product.shop"
            }
    )
    List<Wishlist>
    findByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    boolean
    existsByUser_IdAndProduct_Id(
            Long userId,
            Long productId
    );

    Optional<Wishlist>
    findByUser_IdAndProduct_Id(
            Long userId,
            Long productId
    );

    @Query("""
    SELECT w.product
    FROM Wishlist w
    JOIN w.product p
    JOIN p.shop s
    WHERE p.status = :productStatus
      AND s.status = :shopStatus
    GROUP BY w.product
    ORDER BY COUNT(w.id) DESC
""")
    List<Product> findTopFavoriteProducts(
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            Pageable pageable
    );

    long countByProduct_Id(
            Long productId
    );
}