package com.example.minishop.repository;

import com.example.minishop.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    boolean existsByUser_IdAndProduct_Id(
            Long userId,
            Long productId
    );

    List<Review> findByProduct_IdOrderByCreatedAtDesc(
            Long productId
    );

    List<Review> findByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<Review> findByIdAndUser_Id(
            Long reviewId,
            Long userId
    );

    long countByProduct_Shop_Id(
            Long shopId
    );

    @Query("""
            SELECT COALESCE(AVG(r.rating), 0.0)
            FROM Review r
            WHERE r.product.shop.id = :shopId
            """)
    Double getAverageRatingByShopId(
            @Param("shopId") Long shopId
    );
}