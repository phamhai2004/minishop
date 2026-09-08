package com.example.minishop.repository;

import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.entity.Category;
import com.example.minishop.projection.AdminCategoryAnalytics;
import com.example.minishop.projection.TopCategoryAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Category> findByNameContainingIgnoreCase(String keyword);

    @Query(
            value = """
            SELECT
                c.id AS categoryId,
                c.name AS categoryName,
                COUNT(DISTINCT p.id) AS productCount,
                COALESCE(SUM(oi.quantity), 0) AS sold,
                COALESCE(SUM(oi.subtotal), 0) AS revenue
            FROM Category c
            LEFT JOIN Product p
                ON p.category = c
            LEFT JOIN OrderItem oi
                ON oi.product = p
                AND oi.shopOrder.paymentStatus = :paymentStatus
            WHERE (
                :keyword IS NULL
                OR LOWER(c.name)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            GROUP BY
                c.id,
                c.name
            ORDER BY
                COALESCE(SUM(oi.subtotal), 0) DESC,
                c.id DESC
        """,
            countQuery = """
            SELECT COUNT(c.id)
            FROM Category c
            WHERE (
                :keyword IS NULL
                OR LOWER(c.name)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
        """
    )
    Page<AdminCategoryAnalytics> findAdminCategoryAnalytics(
            @Param("keyword") String keyword,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(c.id)
    FROM Category c
    WHERE EXISTS (
        SELECT p.id
        FROM Product p
        WHERE p.category = c
    )
""")
    long countCategoriesWithProducts();

    @Query("""
    SELECT COUNT(c.id)
    FROM Category c
    WHERE NOT EXISTS (
        SELECT p.id
        FROM Product p
        WHERE p.category = c
    )
""")
    long countEmptyCategories();

    @Query("""
    SELECT
        c.id AS categoryId,
        c.name AS categoryName,
        COALESCE(SUM(oi.quantity), 0) AS sold,
        COALESCE(SUM(oi.subtotal), 0) AS revenue
    FROM Category c
    LEFT JOIN Product p
        ON p.category = c
    LEFT JOIN OrderItem oi
        ON oi.product = p
        AND oi.shopOrder.paymentStatus = :paymentStatus
    GROUP BY
        c.id,
        c.name
    ORDER BY
        COALESCE(SUM(oi.subtotal), 0) DESC
""")
    List<TopCategoryAnalytics> findTopCategoriesByRevenue(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );

    @Query("""
    SELECT
        c.id AS categoryId,
        c.name AS categoryName,
        COALESCE(SUM(oi.quantity), 0) AS sold,
        COALESCE(SUM(oi.subtotal), 0) AS revenue
    FROM Category c
    LEFT JOIN Product p
        ON p.category = c
    LEFT JOIN OrderItem oi
        ON oi.product = p
        AND oi.shopOrder.paymentStatus = :paymentStatus
    GROUP BY
        c.id,
        c.name
    ORDER BY
        COALESCE(SUM(oi.quantity), 0) DESC
""")
    List<TopCategoryAnalytics> findTopCategoriesBySold(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );
}