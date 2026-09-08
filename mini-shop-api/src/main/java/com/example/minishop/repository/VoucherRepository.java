package com.example.minishop.repository;

import com.example.minishop.constant.VoucherScope;
import com.example.minishop.entity.Voucher;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository
        extends JpaRepository<Voucher, Long> {

    boolean existsByCodeIgnoreCase(
            String code
    );

    List<Voucher> findByScopeOrderByIdDesc(
            VoucherScope scope
    );

    Optional<Voucher> findByCodeIgnoreCaseAndScope(
            String code,
            VoucherScope scope
    );

    @Query("""
        SELECT CASE
            WHEN COUNT(uv) > 0 THEN true
            ELSE false
        END
        FROM UserVoucher uv
        WHERE uv.voucher.id = :voucherId
        """)
    boolean hasBeenCollected(
            @Param("voucherId") Long voucherId
    );

    Optional<Voucher> findByCodeIgnoreCase(
            String code
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT v
            FROM Voucher v
            WHERE LOWER(v.code)
                = LOWER(:code)
            """)
    Optional<Voucher> findByCodeForUpdate(
            @Param("code")
            String code
    );

    Page<Voucher> findByShop_Id(
            Long shopId,
            Pageable pageable
    );

    Optional<Voucher> findByIdAndShop_Id(
            Long voucherId,
            Long shopId
    );

    long countByShop_Id(Long shopId);

    @Query("""
        SELECT v
        FROM Voucher v
        LEFT JOIN FETCH v.shop s
        WHERE v.active = true
          AND v.quantity > 0
          AND v.endDate >= :now
          AND (
                :scope IS NULL
                OR v.scope = :scope
          )
          AND (
                :shopId IS NULL
                OR s.id = :shopId
          )
        ORDER BY
            CASE
                WHEN v.startDate <= :now
                THEN 0
                ELSE 1
            END,
            v.endDate ASC,
            v.id DESC
        """)
    List<Voucher> findAvailableCatalog(
            @Param("now")
            LocalDateTime now,
            @Param("scope")
            VoucherScope scope,
            @Param("shopId")
            Long shopId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT v
        FROM Voucher v
        WHERE v.id = :voucherId
          AND v.shop.id = :shopId
        """)
    Optional<Voucher> findByIdAndShopIdForUpdate(
            @Param("voucherId") Long voucherId,
            @Param("shopId") Long shopId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT v
        FROM Voucher v
        WHERE v.id = :voucherId
          AND v.scope = :scope
        """)
    Optional<Voucher> findByIdAndScopeForUpdate(
            @Param("voucherId") Long voucherId,
            @Param("scope") VoucherScope scope
    );
}