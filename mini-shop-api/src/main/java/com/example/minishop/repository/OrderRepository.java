package com.example.minishop.repository;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.entity.Order;
import com.example.minishop.projection.AdminOrderAnalytics;
import com.example.minishop.projection.MonthlySellerRevenue;
import com.example.minishop.projection.TopSellerProduct;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {
            "items",
            "items.product"
    })
    List<Order> findByUser_Id(Long userId);
    Page<Order> findAll(Pageable pageable);

    @Query("""
    SELECT
        o.id AS orderId,
        o.user.id AS customerId,
        o.user.fullName AS customerName,
        o.user.email AS customerEmail,
        o.finalAmount AS finalAmount,
        o.status AS orderStatus,
        o.paymentMethod AS paymentMethod,
        o.paymentStatus AS paymentStatus,
        o.createdAt AS createdAt,
        o.paidAt AS paidAt
    FROM Order o
    WHERE (
        :keyword IS NULL
        OR LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(o.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%')
    )
    AND (
        :orderStatus IS NULL
        OR o.status = :orderStatus
    )
    AND (
        :paymentMethod IS NULL
        OR o.paymentMethod = :paymentMethod
    )
    AND (
        :paymentStatus IS NULL
        OR o.paymentStatus = :paymentStatus
    )
    AND (
        :fromDate IS NULL
        OR o.createdAt >= :fromDate
    )
    AND (
        :toDate IS NULL
        OR o.createdAt <= :toDate
    )
    ORDER BY o.createdAt DESC
""")
    Page<AdminOrderAnalytics> findAdminOrderAnalytics(
            @Param("keyword") String keyword,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
    @Query("""
        SELECT COALESCE(SUM(o.finalAmount), 0)
        FROM Order o
        WHERE o.paymentStatus = :paymentStatus
        """)
    BigDecimal calculateRevenueByPaymentStatus(
            @Param("paymentStatus")
            PaymentStatus paymentStatus
    );
    long countByPaymentStatus(
            PaymentStatus paymentStatus
    );

    long countByPaymentMethod(PaymentMethod paymentMethod);

    long countByStatus(OrderStatus status);

    @Query("""
SELECT COALESCE(SUM(oi.subtotal),0)

FROM OrderItem oi

WHERE oi.product.shop.id = :shopId

AND oi.order.paymentStatus = :paymentStatus
""")
    BigDecimal sellerRevenue(
            @Param("shopId") Long shopId,
            @Param("paymentStatus")
            PaymentStatus paymentStatus
    );
    @Query("""
SELECT COUNT(DISTINCT oi.order.id)

FROM OrderItem oi

WHERE oi.product.shop.id=:shopId
""")
    long countOrdersByShop(
            @Param("shopId") Long shopId
    );
    @Query("""
SELECT COUNT(DISTINCT oi.order.id)

FROM OrderItem oi

WHERE oi.product.shop.id=:shopId

AND oi.order.status=:status
""")
    long countOrdersByShopAndStatus(
            @Param("shopId") Long shopId,
            @Param("status") OrderStatus status
    );

    Optional<Order> findByIdAndUser_Id(
            Long orderId,
            Long userId
    );

    @EntityGraph(attributePaths = {
            "user",
            "address",
            "shopOrders",
            "shopOrders.shop",
            "shopOrders.shop.owner"
    })
    @Query("""
    SELECT o
    FROM Order o
    WHERE o.id = :id
""")
    Optional<Order> findDetailedById(@Param("id") Long id);

    @Query("""
SELECT COUNT(DISTINCT oi.order.id)
FROM OrderItem oi
WHERE oi.product.shop.id = :shopId
AND oi.order.paymentStatus = :paymentStatus
""")
    long countPaidOrdersByShop(
            @Param("shopId") Long shopId,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("""
    SELECT o.id
    FROM Order o
    WHERE o.status = :orderStatus
      AND o.paymentStatus = :paymentStatus
      AND o.paymentMethod = :paymentMethod
      AND o.createdAt < :expiredBefore
    ORDER BY o.createdAt ASC
""")
    List<Long> findExpiredUnpaidOrderIds(
            @Param("orderStatus") OrderStatus orderStatus,

            @Param("paymentStatus")
            PaymentStatus paymentStatus,

            @Param("paymentMethod")
            PaymentMethod paymentMethod,

            @Param("expiredBefore")
            LocalDateTime expiredBefore,

            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT o
    FROM Order o
    WHERE o.id = :orderId
    """)
    Optional<Order> findByIdForUpdate(
            @Param("orderId") Long orderId
    );

    @EntityGraph(attributePaths = {
            "items",
            "items.product"
    })
    List<Order> findByUser_IdAndStatus(
            Long userId,
            OrderStatus status
    );

    @EntityGraph(attributePaths = {
            "user",
            "items",
            "items.product"
    })
    List<Order> findByStatus(OrderStatus status);

    boolean existsByUser_IdAndStatusAndItems_Product_Id(
            Long userId,
            OrderStatus status,
            Long productId
    );

    @EntityGraph(attributePaths = {
            "user",
            "address",
            "items",
            "items.product"
    })
    @Query("""
    SELECT o
    FROM Order o
    WHERE o.id = :orderId
""")
    Optional<Order> findByIdForNotification(
            @Param("orderId") Long orderId
    );

}