package com.example.minishop.repository;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.entity.FlashSale;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FlashSaleRepository
        extends JpaRepository<FlashSale,Long> {

    @Query("""
    SELECT f
    FROM FlashSale f
    WHERE f.product.id = :productId
      AND f.active = true
      AND f.startTime <= :now
      AND f.endTime >= :now
      AND f.sold < f.quantity
    ORDER BY f.startTime DESC
""")
    Optional<FlashSale> findActiveAvailableFlashSale(
            @Param("productId") Long productId,
            @Param("now") LocalDateTime now
    );

    Optional<FlashSale> findByIdAndProduct_Shop_Id(
            Long flashSaleId,
            Long shopId
    );

    Page<FlashSale> findByProduct_Shop_Id(
            Long shopId,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(f) > 0
        FROM FlashSale f
        WHERE f.product.id = :productId
          AND f.active = true
          AND f.startTime < :endTime
          AND f.endTime > :startTime
    """)
    boolean existsOverlappingFlashSale(
            @Param("productId") Long productId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
    SELECT COUNT(f) > 0
    FROM FlashSale f
    WHERE f.product.id = :productId
      AND f.id <> :flashSaleId
      AND f.active = true
      AND f.startTime < :endTime
      AND f.endTime > :startTime
""")
    boolean existsOverlappingFlashSaleForUpdate(
            @Param("productId") Long productId,
            @Param("flashSaleId") Long flashSaleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT f
    FROM FlashSale f
    WHERE f.id = :id
""")
    Optional<FlashSale> findByIdForUpdate(
            @Param("id") Long id
    );

    @Query("""
    SELECT f
    FROM FlashSale f
    JOIN f.product p
    JOIN p.shop s
    WHERE f.active = true
      AND f.startTime <= :now
      AND f.endTime >= :now
      AND f.sold < f.quantity
      AND p.status = :productStatus
      AND s.status = :shopStatus
    ORDER BY f.endTime ASC
""")
    Page<FlashSale> findPublicActiveFlashSales(
            @Param("now")
            LocalDateTime now,

            @Param("productStatus")
            ProductStatus productStatus,

            @Param("shopStatus")
            ShopStatus shopStatus,

            Pageable pageable
    );

}
