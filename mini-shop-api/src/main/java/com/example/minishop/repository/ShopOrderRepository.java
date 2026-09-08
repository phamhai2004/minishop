package com.example.minishop.repository;

import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.constant.ShopOrderStatus;
import com.example.minishop.entity.ShopOrder;
import com.example.minishop.projection.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShopOrderRepository
        extends JpaRepository<ShopOrder, Long> {

    @EntityGraph(attributePaths = {
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product"
    })
    @Query("""
        SELECT so
        FROM ShopOrder so
        WHERE so.shop.id = :shopId
          AND (
                so.paymentMethod = :cod
                OR (
                    so.paymentMethod = :vnpay
                    AND so.paymentStatus = :paid
                )
          )
    """)
    Page<ShopOrder> findByShop_Id(
            @Param("shopId") Long shopId,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product"
    })
    @Query("""
    SELECT so
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.createdAt >= :fromDate
      AND so.createdAt <= :toDate
      AND (
            so.paymentMethod = :cod
            OR (
                so.paymentMethod = :vnpay
                AND so.paymentStatus = :paid
            )
      )
""")
    Page<ShopOrder> findByShop_IdAndCreatedAtBetween(
            @Param("shopId") Long shopId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product"
    })
    @Query("""
    SELECT so
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.status = :status
      AND so.createdAt >= :fromDate
      AND so.createdAt <= :toDate
      AND (
            so.paymentMethod = :cod
            OR (
                so.paymentMethod = :vnpay
                AND so.paymentStatus = :paid
            )
      )
""")
    Page<ShopOrder> findByShop_IdAndStatusAndCreatedAtBetween(
            @Param("shopId") Long shopId,
            @Param("status") ShopOrderStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product"
    })
    @Query("""
        SELECT so
        FROM ShopOrder so
        WHERE so.shop.id = :shopId
          AND so.status = :status
          AND (
                so.paymentMethod = :cod
                OR (
                    so.paymentMethod = :vnpay
                    AND so.paymentStatus = :paid
                )
          )
    """)
    Page<ShopOrder> findByShop_IdAndStatus(
            @Param("shopId") Long shopId,
            @Param("status") ShopOrderStatus status,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid,
            Pageable pageable
    );


    @EntityGraph(attributePaths = {
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product"
    })
    @Query("""
        SELECT so
        FROM ShopOrder so
        WHERE so.id = :shopOrderId
          AND so.shop.id = :shopId
          AND (
                so.paymentMethod = :cod
                OR (
                    so.paymentMethod = :vnpay
                    AND so.paymentStatus = :paid
                )
          )
    """)
    Optional<ShopOrder> findByIdAndShop_Id(
            @Param("shopOrderId") Long shopOrderId,
            @Param("shopId") Long shopId,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid
    );

    @EntityGraph(attributePaths = {
            "shop",
            "shop.owner",
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product"
    })
    @Query("""
    SELECT so
    FROM ShopOrder so
    WHERE so.order.id = :orderId
""")
    List<ShopOrder> findAllByOrderIdForNotification(
            @Param("orderId") Long orderId
    );


    long countByShop_Id(Long shopId);


    @EntityGraph(attributePaths = {
            "shop",
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product"
    })
    @Query("""
        SELECT so
        FROM ShopOrder so
        WHERE so.id = :id
    """)
    Optional<ShopOrder> findDetailedById(@Param("id") Long id);

    boolean existsByOrderCode(
            String orderCode
    );

    @EntityGraph(attributePaths = {
            "shop",
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product",
            "items.variant"
    })
    List<ShopOrder>
    findByOrder_User_IdOrderByCreatedAtDesc(
            Long userId
    );

    @EntityGraph(attributePaths = {
            "shop",
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product",
            "items.variant"
    })
    Optional<ShopOrder>
    findByOrderCodeAndOrder_User_Id(
            String orderCode,
            Long userId
    );

    @Query("""
    SELECT COUNT(so.id)
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND (
            so.paymentMethod = :cod
            OR (
                so.paymentMethod = :vnpay
                AND so.paymentStatus = :paid
            )
      )
""")
    long countDashboardOrdersByShop(
            @Param("shopId") Long shopId,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid
    );

    @Query("""
    SELECT COUNT(so.id)
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.status = :status
      AND (
            so.paymentMethod = :cod
            OR (
                so.paymentMethod = :vnpay
                AND so.paymentStatus = :paid
            )
      )
""")
    long countDashboardOrdersByShopAndStatus(
            @Param("shopId") Long shopId,
            @Param("status") ShopOrderStatus status,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid
    );

    long countByShop_IdAndPaymentStatus(
            Long shopId,
            PaymentStatus paymentStatus
    );

    @Query("""
    SELECT COALESCE(SUM(so.finalAmount), 0)
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.paymentStatus = :paymentStatus
""")
    BigDecimal calculateDashboardRevenue(
            @Param("shopId") Long shopId,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("""
    SELECT
        so.shop.id AS shopId,
        so.shop.name AS shopName,
        COALESCE(SUM(so.finalAmount), 0) AS revenue,
        COUNT(so.id) AS orderCount
    FROM ShopOrder so
    WHERE so.paymentStatus = :paymentStatus
    GROUP BY so.shop.id, so.shop.name
    ORDER BY revenue DESC
""")
    List<TopAdminSeller> topAdminSellers(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(DISTINCT so.order.user.id)
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.status <> :cancelled
      AND (
            so.paymentMethod = :cod
            OR (
                so.paymentMethod = :vnpay
                AND so.paymentStatus = :paid
            )
      )
""")
    long countCustomersPurchasedByShop(
            @Param("shopId") Long shopId,
            @Param("cancelled") ShopOrderStatus cancelled,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid
    );

    @Query("""
    SELECT COUNT(DISTINCT so.order.user.id)
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.status <> :cancelled
      AND so.createdAt >= :fromDate
      AND so.createdAt <= :toDate
      AND (
            so.paymentMethod = :cod
            OR (
                so.paymentMethod = :vnpay
                AND so.paymentStatus = :paid
            )
      )
      AND NOT EXISTS (
            SELECT so2.id
            FROM ShopOrder so2
            WHERE so2.shop.id = :shopId
              AND so2.order.user.id = so.order.user.id
              AND so2.status <> :cancelled
              AND so2.createdAt < :fromDate
              AND (
                    so2.paymentMethod = :cod
                    OR (
                        so2.paymentMethod = :vnpay
                        AND so2.paymentStatus = :paid
                    )
              )
      )
""")
    long countNewCustomersByShop(
            @Param("shopId") Long shopId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("cancelled") ShopOrderStatus cancelled,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid
    );

    @Query("""
    SELECT so.order.user.id
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.status <> :cancelled
      AND (
            so.paymentMethod = :cod
            OR (
                so.paymentMethod = :vnpay
                AND so.paymentStatus = :paid
            )
      )
    GROUP BY so.order.user.id
    HAVING COUNT(so.id) >= 2
""")
    List<Long> findReturningCustomerIdsByShop(
            @Param("shopId") Long shopId,
            @Param("cancelled") ShopOrderStatus cancelled,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid
    );

    @Query("""
    SELECT COALESCE(SUM(so.finalAmount), 0)
    FROM ShopOrder so
    WHERE so.paymentStatus = :paymentStatus
""")
    BigDecimal calculateAdminRevenue(
            @Param("paymentStatus")
            PaymentStatus paymentStatus
    );

    @Query("""
    SELECT
        MONTH(so.paidAt) AS month,
        SUM(so.finalAmount) AS revenue
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND YEAR(so.paidAt) = :year
      AND so.paymentStatus = :paymentStatus
    GROUP BY MONTH(so.paidAt)
    ORDER BY MONTH(so.paidAt)
""")
    List<MonthlySellerRevenue> monthlyRevenueByShop(
            @Param("shopId") Long shopId,
            @Param("year") Integer year,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("""
    SELECT
        MONTH(so.paidAt) AS month,
        SUM(so.finalAmount) AS revenue
    FROM ShopOrder so
    WHERE YEAR(so.paidAt) = :year
      AND so.paymentStatus = :paymentStatus
    GROUP BY MONTH(so.paidAt)
    ORDER BY MONTH(so.paidAt)
""")
    List<MonthlySellerRevenue> adminMonthlyRevenue(
            @Param("year") Integer year,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("""
    SELECT
        oi.product.id AS productId,
        oi.product.name AS productName,
        SUM(oi.quantity) AS sold
    FROM ShopOrder so
    JOIN so.items oi
    WHERE so.shop.id = :shopId
      AND so.paymentStatus = :paymentStatus
    GROUP BY oi.product.id, oi.product.name
    ORDER BY sold DESC
""")
    List<TopSellerProduct> topProductsByShop(
            @Param("shopId") Long shopId,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );

    @Query("""
    SELECT
        oi.product.id AS productId,
        oi.product.name AS productName,
        SUM(oi.quantity) AS sold
    FROM ShopOrder so
    JOIN so.items oi
    WHERE so.paymentStatus = :paymentStatus
    GROUP BY oi.product.id, oi.product.name
    ORDER BY sold DESC
""")
    List<TopSellerProduct> adminTopProducts(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );

    @Query("""
    SELECT
        oi.product.id AS productId,
        oi.product.name AS productName,
        COALESCE(SUM(oi.subtotal), 0) AS revenue,
        SUM(oi.quantity) AS sold
    FROM ShopOrder so
    JOIN so.items oi
    WHERE so.paymentStatus = :paymentStatus
    GROUP BY oi.product.id, oi.product.name
    ORDER BY revenue DESC
""")
    List<TopProductRevenue> adminTopProductsByRevenue(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );

    @Query("""
    SELECT COALESCE(AVG(so.finalAmount), 0)
    FROM ShopOrder so
    WHERE so.paymentStatus = :paymentStatus
""")
    BigDecimal calculateAdminAverageOrderValue(
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @EntityGraph(attributePaths = {
            "order",
            "order.user",
            "order.address",
            "items",
            "items.product"
    })
    @Query("""
    SELECT so
    FROM ShopOrder so
    WHERE so.shop.id = :shopId

      AND (
            so.paymentMethod = :cod
            OR (
                so.paymentMethod = :vnpay
                AND so.paymentStatus = :paid
            )
      )

      AND (
            :keyword IS NULL
            OR LOWER(so.orderCode)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(so.order.user.fullName)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(so.order.user.email)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
      )

      AND (
            :status IS NULL
            OR so.status = :status
      )

      AND (
            :paymentMethod IS NULL
            OR so.paymentMethod = :paymentMethod
      )

      AND (
            :paymentStatus IS NULL
            OR so.paymentStatus = :paymentStatus
      )

      AND (
            :fromDate IS NULL
            OR so.createdAt >= :fromDate
      )

      AND (
            :toDate IS NULL
            OR so.createdAt <= :toDate
      )
""")
    Page<ShopOrder> findSellerOrderAnalytics(
            @Param("shopId") Long shopId,
            @Param("keyword") String keyword,
            @Param("status") ShopOrderStatus status,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(so.id)
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.paymentMethod = :paymentMethod
      AND (
            so.paymentMethod = :cod
            OR (
                so.paymentMethod = :vnpay
                AND so.paymentStatus = :paid
            )
      )
""")
    long countDashboardOrdersByShopAndPaymentMethod(
            @Param("shopId") Long shopId,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid
    );

    @Query("""
    SELECT COALESCE(AVG(so.finalAmount), 0)
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.paymentStatus = :paymentStatus
""")
    BigDecimal calculateSellerAverageOrderValue(
            @Param("shopId") Long shopId,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query(
            value = """
            SELECT
                so.order.user.id AS customerId,
                so.order.user.fullName AS customerName,
                so.order.user.email AS customerEmail,
                so.order.user.phone AS customerPhone,

                COUNT(so.id) AS orderCount,

                COALESCE(
                    SUM(
                        CASE
                            WHEN so.paymentStatus = :paid
                                THEN so.finalAmount
                            ELSE 0
                        END
                    ),
                    0
                ) AS totalSpent,

                MAX(so.createdAt) AS lastOrderAt,

                CASE
                    WHEN COUNT(so.id) >= 2
                        THEN 'RETURNING'
                    ELSE 'NEW'
                END AS customerType

            FROM ShopOrder so

            WHERE so.shop.id = :shopId

              AND so.status <> :cancelled

              AND (
                    so.paymentMethod = :cod
                    OR (
                        so.paymentMethod = :vnpay
                        AND so.paymentStatus = :paid
                    )
              )

              AND (
                    :keyword IS NULL
                    OR LOWER(so.order.user.fullName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(so.order.user.email)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR so.order.user.phone
                        LIKE CONCAT('%', :keyword, '%')
              )

            GROUP BY
                so.order.user.id,
                so.order.user.fullName,
                so.order.user.email,
                so.order.user.phone

            ORDER BY MAX(so.createdAt) DESC
        """,
            countQuery = """
            SELECT COUNT(DISTINCT so.order.user.id)
            FROM ShopOrder so

            WHERE so.shop.id = :shopId

              AND so.status <> :cancelled

              AND (
                    so.paymentMethod = :cod
                    OR (
                        so.paymentMethod = :vnpay
                        AND so.paymentStatus = :paid
                    )
              )

              AND (
                    :keyword IS NULL
                    OR LOWER(so.order.user.fullName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(so.order.user.email)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR so.order.user.phone
                        LIKE CONCAT('%', :keyword, '%')
              )
        """
    )
    Page<SellerCustomerAnalytics> findSellerCustomerAnalytics(
            @Param("shopId") Long shopId,
            @Param("keyword") String keyword,
            @Param("cancelled") ShopOrderStatus cancelled,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid,
            Pageable pageable
    );

    @Query(
            value = """
            SELECT
                so.order.user.id AS customerId,
                so.order.user.fullName AS customerName,
                so.order.user.email AS customerEmail,
                so.order.user.phone AS customerPhone,

                COUNT(so.id) AS orderCount,

                COALESCE(
                    SUM(
                        CASE
                            WHEN so.paymentStatus = :paid
                                THEN so.finalAmount
                            ELSE 0
                        END
                    ),
                    0
                ) AS totalSpent,

                MAX(so.createdAt) AS lastOrderAt,

                'NEW' AS customerType

            FROM ShopOrder so

            WHERE so.shop.id = :shopId
              AND so.status <> :cancelled

              AND (
                    so.paymentMethod = :cod
                    OR (
                        so.paymentMethod = :vnpay
                        AND so.paymentStatus = :paid
                    )
              )

              AND (
                    :keyword IS NULL
                    OR LOWER(so.order.user.fullName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(so.order.user.email)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR so.order.user.phone
                        LIKE CONCAT('%', :keyword, '%')
              )

              AND (
                    SELECT COUNT(so2.id)
                    FROM ShopOrder so2
                    WHERE so2.shop.id = :shopId
                      AND so2.order.user.id = so.order.user.id
                      AND so2.status <> :cancelled
                      AND (
                            so2.paymentMethod = :cod
                            OR (
                                so2.paymentMethod = :vnpay
                                AND so2.paymentStatus = :paid
                            )
                      )
              ) = 1

            GROUP BY
                so.order.user.id,
                so.order.user.fullName,
                so.order.user.email,
                so.order.user.phone

            ORDER BY MAX(so.createdAt) DESC
        """,
            countQuery = """
            SELECT COUNT(DISTINCT so.order.user.id)
            FROM ShopOrder so
            WHERE so.shop.id = :shopId
              AND so.status <> :cancelled

              AND (
                    so.paymentMethod = :cod
                    OR (
                        so.paymentMethod = :vnpay
                        AND so.paymentStatus = :paid
                    )
              )

              AND (
                    :keyword IS NULL
                    OR LOWER(so.order.user.fullName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(so.order.user.email)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR so.order.user.phone
                        LIKE CONCAT('%', :keyword, '%')
              )

              AND (
                    SELECT COUNT(so2.id)
                    FROM ShopOrder so2
                    WHERE so2.shop.id = :shopId
                      AND so2.order.user.id = so.order.user.id
                      AND so2.status <> :cancelled
                      AND (
                            so2.paymentMethod = :cod
                            OR (
                                so2.paymentMethod = :vnpay
                                AND so2.paymentStatus = :paid
                            )
                      )
              ) = 1
        """
    )
    Page<SellerCustomerAnalytics> findSellerNewCustomerAnalytics(
            @Param("shopId") Long shopId,
            @Param("keyword") String keyword,
            @Param("cancelled") ShopOrderStatus cancelled,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid,
            Pageable pageable
    );

    @Query(
            value = """
            SELECT
                so.order.user.id AS customerId,
                so.order.user.fullName AS customerName,
                so.order.user.email AS customerEmail,
                so.order.user.phone AS customerPhone,

                COUNT(so.id) AS orderCount,

                COALESCE(
                    SUM(
                        CASE
                            WHEN so.paymentStatus = :paid
                                THEN so.finalAmount
                            ELSE 0
                        END
                    ),
                    0
                ) AS totalSpent,

                MAX(so.createdAt) AS lastOrderAt,

                'RETURNING' AS customerType

            FROM ShopOrder so

            WHERE so.shop.id = :shopId
              AND so.status <> :cancelled

              AND (
                    so.paymentMethod = :cod
                    OR (
                        so.paymentMethod = :vnpay
                        AND so.paymentStatus = :paid
                    )
              )

              AND (
                    :keyword IS NULL
                    OR LOWER(so.order.user.fullName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(so.order.user.email)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR so.order.user.phone
                        LIKE CONCAT('%', :keyword, '%')
              )

              AND (
                    SELECT COUNT(so2.id)
                    FROM ShopOrder so2
                    WHERE so2.shop.id = :shopId
                      AND so2.order.user.id = so.order.user.id
                      AND so2.status <> :cancelled
                      AND (
                            so2.paymentMethod = :cod
                            OR (
                                so2.paymentMethod = :vnpay
                                AND so2.paymentStatus = :paid
                            )
                      )
              ) >= 2

            GROUP BY
                so.order.user.id,
                so.order.user.fullName,
                so.order.user.email,
                so.order.user.phone

            ORDER BY MAX(so.createdAt) DESC
        """,
            countQuery = """
            SELECT COUNT(DISTINCT so.order.user.id)
            FROM ShopOrder so
            WHERE so.shop.id = :shopId
              AND so.status <> :cancelled

              AND (
                    so.paymentMethod = :cod
                    OR (
                        so.paymentMethod = :vnpay
                        AND so.paymentStatus = :paid
                    )
              )

              AND (
                    :keyword IS NULL
                    OR LOWER(so.order.user.fullName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(so.order.user.email)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR so.order.user.phone
                        LIKE CONCAT('%', :keyword, '%')
              )

              AND (
                    SELECT COUNT(so2.id)
                    FROM ShopOrder so2
                    WHERE so2.shop.id = :shopId
                      AND so2.order.user.id = so.order.user.id
                      AND so2.status <> :cancelled
                      AND (
                            so2.paymentMethod = :cod
                            OR (
                                so2.paymentMethod = :vnpay
                                AND so2.paymentStatus = :paid
                            )
                      )
              ) >= 2
        """
    )
    Page<SellerCustomerAnalytics> findSellerReturningCustomerAnalytics(
            @Param("shopId") Long shopId,
            @Param("keyword") String keyword,
            @Param("cancelled") ShopOrderStatus cancelled,
            @Param("cod") PaymentMethod cod,
            @Param("vnpay") PaymentMethod vnpay,
            @Param("paid") PaymentStatus paid,
            Pageable pageable
    );

    @Query("""
    SELECT COALESCE(SUM(so.finalAmount), 0)
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.paymentStatus = :paymentStatus
      AND so.paidAt >= :fromDate
      AND so.paidAt < :toDate
""")
    BigDecimal calculateRevenueByShopAndPaidAtBetween(
            @Param("shopId") Long shopId,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
    SELECT COUNT(so.id)
    FROM ShopOrder so
    WHERE so.shop.id = :shopId
      AND so.paymentStatus = :paymentStatus
      AND so.paidAt >= :fromDate
      AND so.paidAt < :toDate
""")
    long countPaidOrdersByShopAndPaidAtBetween(
            @Param("shopId") Long shopId,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
    SELECT COALESCE(SUM(so.finalAmount), 0)
    FROM ShopOrder so
    WHERE so.paymentStatus = :paymentStatus
      AND so.paidAt >= :fromDate
      AND so.paidAt < :toDate
""")
    BigDecimal calculateAdminRevenueByPaidAtBetween(
            @Param("paymentStatus")
            PaymentStatus paymentStatus,

            @Param("fromDate")
            LocalDateTime fromDate,

            @Param("toDate")
            LocalDateTime toDate
    );
}