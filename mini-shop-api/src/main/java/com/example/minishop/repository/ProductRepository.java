package com.example.minishop.repository;

import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.dto.response.ShopCategoryResponse;
import com.example.minishop.entity.Product;
import com.example.minishop.projection.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
                JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByCategory_Id(Long categoryId);

    boolean existsByCategory_Id(Long categoryId);

    boolean existsByNameIgnoreCase(String name);

    long countByCategory_Id(Long categoryId);

    List<Product> findByPriceGreaterThan(BigDecimal price);

    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> findByCategory_IdOrderByPriceDesc(Long categoryId);

    @Query("""
        SELECT p
        FROM Product p
        WHERE (
            p.variants IS EMPTY
            AND COALESCE(p.quantity, 0) > :quantity
        )
        OR (
            p.variants IS NOT EMPTY
            AND (
                SELECT COALESCE(SUM(v.quantity), 0)
                FROM ProductVariant v
                WHERE v.product = p
            ) > :quantity
        )
        """)
    List<Product> findByEffectiveQuantityGreaterThan(
            @Param("quantity") Integer quantity
    );

    @Query("""

            SELECT p
    FROM Product p
    JOIN p.category c
    WHERE c.name=:name
    """)
    List<Product> findProductsByCategoryName(
            @Param("name")
            String name
    );

    @Query("""
     SELECT
     p.id as id,
     p.name as name,
     p.price as price
     FROM Product p
     """)
    List<ProductSummary> findAllSummary();

    @Query("""
    SELECT p
    FROM Product p
    JOIN FETCH p.category
    """)
    List<Product> findAllWithCategory();
    Optional<Product> findByIdAndShop_Id(
            Long productId,
            Long shopId
    );

    Page<Product> findByShop_Id(
            Long shopId,
            Pageable pageable
    );
    Page<Product> findByShop_IdAndNameContainingIgnoreCase(
            Long shopId,
            String keyword,
            Pageable pageable
    );
    long countByShop_Id(Long shopId);

    Optional<Product> findByIdAndStatusAndShop_Status(
            Long id,
            ProductStatus productStatus,
            ShopStatus shopStatus
    );

    @Query("""
        select distinct p.name
        from Product p
        join p.shop s
        where p.status = :productStatus
          and s.status = :shopStatus
          and lower(p.name) like lower(concat(:keyword, '%'))
        order by p.name asc
        """)
    List<String> findProductNameSuggestions(
            @Param("keyword") String keyword,
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            Pageable pageable
    );

    List<Product> findByIdIn(List<Long> ids);

    List<Product> findByCategory_NameIgnoreCaseAndPriceLessThanEqualAndStatusAndShop_Status(
            String categoryName,
            BigDecimal price,
            ProductStatus productStatus,
            ShopStatus shopStatus
    );

    @Query("""
    SELECT p
    FROM Product p
    JOIN FETCH p.category c
    JOIN FETCH p.shop s
    WHERE (:keyword IS NULL OR :keyword = ''
           OR lower(p.name) LIKE lower(concat('%', :keyword, '%')))
      AND (:categoryId IS NULL OR c.id = :categoryId)
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
      AND p.status = :productStatus
      AND s.status = :shopStatus
    ORDER BY p.id DESC
    """)
    List<Product> searchProductsForChat(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Product p
    JOIN FETCH p.category c
    JOIN FETCH p.shop s
    WHERE p.status = :productStatus
      AND s.status = :shopStatus
    ORDER BY p.id DESC
""")
    List<Product> findActiveProductsForChat(
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus
    );

    @Query("""
    SELECT new com.example.minishop.dto.response.ShopCategoryResponse(
        c.id,
        c.name,
        COUNT(p.id)
    )
    FROM Product p
    JOIN p.category c
    JOIN p.shop s
    WHERE s.id = :shopId
      AND p.status = :productStatus
      AND s.status = :shopStatus
    GROUP BY c.id, c.name
    ORDER BY c.name ASC
    """)
    List<ShopCategoryResponse> findCategoriesByShop(
            @Param("shopId") Long shopId,
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus
    );

    @Query("""
    SELECT COUNT(p.id)
    FROM Product p
    WHERE (
        p.variants IS EMPTY
        AND COALESCE(p.quantity, 0) > :threshold
    )
    OR (
        p.variants IS NOT EMPTY
        AND (
            SELECT COALESCE(SUM(v.quantity), 0)
            FROM ProductVariant v
            WHERE v.product = p
        ) > :threshold
    )
""")
    long countInStockProducts(
            @Param("threshold") Integer threshold
    );

    @Query("""
    SELECT COUNT(p.id)
    FROM Product p
    WHERE (
        p.variants IS EMPTY
        AND COALESCE(p.quantity, 0) BETWEEN :minQuantity AND :maxQuantity
    )
    OR (
        p.variants IS NOT EMPTY
        AND (
            SELECT COALESCE(SUM(v.quantity), 0)
            FROM ProductVariant v
            WHERE v.product = p
        ) BETWEEN :minQuantity AND :maxQuantity
    )
""")
    long countLowStockProducts(
            @Param("minQuantity") Integer minQuantity,
            @Param("maxQuantity") Integer maxQuantity
    );

    @Query("""
    SELECT COUNT(p.id)
    FROM Product p
    WHERE (
        p.variants IS EMPTY
        AND COALESCE(p.quantity, 0) <= 0
    )
    OR (
        p.variants IS NOT EMPTY
        AND (
            SELECT COALESCE(SUM(v.quantity), 0)
            FROM ProductVariant v
            WHERE v.product = p
        ) <= 0
    )
""")
    long countOutOfStockProducts();

    @Query("""
    SELECT COUNT(p.id)
    FROM Product p
    WHERE p.shop.id = :shopId
      AND (
            (
                p.variants IS EMPTY
                AND COALESCE(p.quantity, 0) > :threshold
            )
            OR (
                p.variants IS NOT EMPTY
                AND (
                    SELECT COALESCE(SUM(v.quantity), 0)
                    FROM ProductVariant v
                    WHERE v.product = p
                ) > :threshold
            )
      )
""")
    long countInStockProductsByShop(
            @Param("shopId") Long shopId,
            @Param("threshold") Integer threshold
    );

    @Query("""
    SELECT COUNT(p.id)
    FROM Product p
    WHERE p.shop.id = :shopId
      AND (
            (
                p.variants IS EMPTY
                AND COALESCE(p.quantity, 0)
                    BETWEEN :minQuantity AND :maxQuantity
            )
            OR (
                p.variants IS NOT EMPTY
                AND (
                    SELECT COALESCE(SUM(v.quantity), 0)
                    FROM ProductVariant v
                    WHERE v.product = p
                ) BETWEEN :minQuantity AND :maxQuantity
            )
      )
""")
    long countLowStockProductsByShop(
            @Param("shopId") Long shopId,
            @Param("minQuantity") Integer minQuantity,
            @Param("maxQuantity") Integer maxQuantity
    );

    @Query("""
    SELECT COUNT(p.id)
    FROM Product p
    WHERE p.shop.id = :shopId
      AND (
            (
                p.variants IS EMPTY
                AND COALESCE(p.quantity, 0) <= 0
            )
            OR (
                p.variants IS NOT EMPTY
                AND (
                    SELECT COALESCE(SUM(v.quantity), 0)
                    FROM ProductVariant v
                    WHERE v.product = p
                ) <= 0
            )
      )
""")
    long countOutOfStockProductsByShop(
            @Param("shopId") Long shopId
    );

    @Query("""
    SELECT
        p.id AS productId,
        p.name AS productName,
        p.shop.name AS shopName
    FROM Product p
    WHERE NOT EXISTS (
        SELECT oi.id
        FROM ShopOrder so
        JOIN so.items oi
        WHERE oi.product = p
          AND so.paymentStatus = :paymentStatus
    )
    ORDER BY p.id DESC
""")
    List<NeverSoldProduct> findProductsNeverSold(
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("""
    SELECT
        p.id AS productId,
        p.name AS productName,
        p.shop.name AS shopName
    FROM Product p
    WHERE p.shop.id = :shopId
      AND NOT EXISTS (
          SELECT oi.id
          FROM ShopOrder so
          JOIN so.items oi
          WHERE oi.product = p
            AND so.paymentStatus = :paymentStatus
      )
    ORDER BY p.id DESC
""")
    List<NeverSoldProduct> findSellerProductsNeverSold(
            @Param("shopId") Long shopId,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("""
    SELECT
        p.id AS productId,
        p.name AS productName,
        p.shop.name AS shopName,
        SUM(oi.quantity) AS sold,
        SUM(oi.subtotal) AS revenue,
        CASE
            WHEN p.variants IS EMPTY
                THEN COALESCE(p.quantity, 0)
            ELSE (
                SELECT COALESCE(SUM(v.quantity), 0)
                FROM ProductVariant v
                WHERE v.product = p
            )
        END AS effectiveStock
    FROM ShopOrder so
    JOIN so.items oi
    JOIN oi.product p
    WHERE so.paymentStatus = :paymentStatus
    GROUP BY
        p.id,
        p.name,
        p.shop.name,
        p.quantity
    HAVING SUM(oi.quantity) BETWEEN :minSold AND :maxSold
       AND (
            (
                p.variants IS EMPTY
                AND COALESCE(p.quantity, 0) > :stockThreshold
            )
            OR
            (
                p.variants IS NOT EMPTY
                AND (
                    SELECT COALESCE(SUM(v.quantity), 0)
                    FROM ProductVariant v
                    WHERE v.product = p
                ) > :stockThreshold
            )
       )
    ORDER BY SUM(oi.quantity) ASC, effectiveStock DESC
""")
    List<SlowMovingProduct> findSlowMovingProducts(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("minSold") Long minSold,
            @Param("maxSold") Long maxSold,
            @Param("stockThreshold") Integer stockThreshold
    );

    @Query("""
    SELECT
        p.id AS productId,
        p.name AS productName,
        p.shop.name AS shopName,
        COALESCE(SUM(oi.quantity), 0) AS sold,
        COALESCE(SUM(oi.subtotal), 0) AS revenue,
        CASE
            WHEN p.variants IS EMPTY
                THEN COALESCE(p.quantity, 0)
            ELSE (
                SELECT COALESCE(SUM(v.quantity), 0)
                FROM ProductVariant v
                WHERE v.product = p
            )
        END AS effectiveStock
    FROM Product p
    LEFT JOIN OrderItem oi
        ON oi.product = p
        AND oi.shopOrder.paymentStatus = :paymentStatus
    GROUP BY
        p.id,
        p.name,
        p.shop.name,
        p.quantity
    ORDER BY COALESCE(SUM(oi.subtotal), 0) DESC
""")
    Page<AdminProductAnalytics> findAdminProductAnalytics(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );

    @Query(
            value = """
            SELECT
                p.id AS productId,
                p.name AS productName,
                COALESCE(SUM(oi.quantity), 0) AS sold,
                COALESCE(SUM(oi.subtotal), 0) AS revenue,
                CASE
                    WHEN p.variants IS EMPTY
                        THEN COALESCE(p.quantity, 0)
                    ELSE (
                        SELECT COALESCE(SUM(v.quantity), 0)
                        FROM ProductVariant v
                        WHERE v.product = p
                    )
                END AS effectiveStock
            FROM Product p
            LEFT JOIN OrderItem oi
                ON oi.product = p
                AND oi.shopOrder.paymentStatus = :paymentStatus
            WHERE p.shop.id = :shopId
              AND (
                    :keyword IS NULL
                    OR LOWER(p.name)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            GROUP BY
                p.id,
                p.name,
                p.quantity
            ORDER BY
                COALESCE(SUM(oi.subtotal), 0) DESC
        """,
            countQuery = """
            SELECT COUNT(p.id)
            FROM Product p
            WHERE p.shop.id = :shopId
              AND (
                    :keyword IS NULL
                    OR LOWER(p.name)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
        """
    )
    Page<SellerProductAnalytics> findSellerProductAnalytics(
            @Param("shopId") Long shopId,
            @Param("keyword") String keyword,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );

    @Query("""
    SELECT
        p.id AS productId,
        p.name AS productName,
        p.shop.name AS shopName,
        SUM(oi.quantity) AS sold,
        SUM(oi.subtotal) AS revenue,
        CASE
            WHEN p.variants IS EMPTY
                THEN COALESCE(p.quantity, 0)
            ELSE (
                SELECT COALESCE(SUM(v.quantity), 0)
                FROM ProductVariant v
                WHERE v.product = p
            )
        END AS effectiveStock
    FROM Product p
    JOIN OrderItem oi
        ON oi.product = p
        AND oi.shopOrder.paymentStatus = :paymentStatus
    WHERE p.shop.id = :shopId
    GROUP BY
        p.id,
        p.name,
        p.shop.name,
        p.quantity
    HAVING
        SUM(oi.quantity) BETWEEN :minSold AND :maxSold
        AND (
            CASE
                WHEN p.variants IS EMPTY
                    THEN COALESCE(p.quantity, 0)
                ELSE (
                    SELECT COALESCE(SUM(v.quantity), 0)
                    FROM ProductVariant v
                    WHERE v.product = p
                )
            END
        ) > :minStock
    ORDER BY SUM(oi.quantity) ASC,
             SUM(oi.subtotal) ASC
""")
    List<SlowMovingProduct> findSellerSlowMovingProducts(
            @Param("shopId") Long shopId,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("minSold") Long minSold,
            @Param("maxSold") Long maxSold,
            @Param("minStock") Long minStock
    );

    @Query("""
    SELECT
        p.id AS productId,
        p.name AS productName,
        COALESCE(SUM(oi.quantity), 0) AS sold,
        COALESCE(SUM(oi.subtotal), 0) AS revenue,
        CASE
            WHEN p.variants IS EMPTY
                THEN COALESCE(p.quantity, 0)
            ELSE (
                SELECT COALESCE(SUM(v.quantity), 0)
                FROM ProductVariant v
                WHERE v.product = p
            )
        END AS effectiveStock
    FROM Product p
    JOIN OrderItem oi
        ON oi.product = p
        AND oi.shopOrder.paymentStatus = :paymentStatus
    WHERE p.shop.id = :shopId
    GROUP BY
        p.id,
        p.name,
        p.quantity
    ORDER BY
        SUM(oi.subtotal) DESC
""")
    List<SellerProductAnalytics> findSellerTopProductsByRevenue(
            @Param("shopId") Long shopId,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );

    Page<Product>
    findByCategory_IdAndStatusAndShop_Status(
            Long categoryId,
            ProductStatus productStatus,
            ShopStatus shopStatus,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Product p
    JOIN OrderItem oi
        ON oi.product = p
    JOIN p.shop s
    WHERE p.status = :productStatus
      AND s.status = :shopStatus
      AND oi.shopOrder.paymentStatus = :paid
    GROUP BY p
    ORDER BY SUM(oi.quantity) DESC
""")
    List<Product> findTopSellingProducts(
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("paid") PaymentStatus paid,
            Pageable pageable
    );
}

