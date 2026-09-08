package com.example.minishop.repository;

import com.example.minishop.entity.UserVoucher;
import com.example.minishop.entity.Voucher;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository
        extends JpaRepository<UserVoucher, Long> {

    boolean existsByUser_IdAndVoucher_Id(
            Long userId,
            Long voucherId
    );

    Optional<UserVoucher>
    findByUser_IdAndVoucher_CodeIgnoreCase(
            Long userId,
            String code
    );

    @EntityGraph(
            attributePaths = {
                    "voucher",
                    "voucher.shop"
            }
    )
    List<UserVoucher>
    findByUser_IdOrderByCollectedAtDesc(
            Long userId
    );

    @EntityGraph(
            attributePaths = {
                    "voucher",
                    "voucher.shop"
            }
    )
    List<UserVoucher>
    findByUser_IdAndUsedFalseOrderByCollectedAtDesc(
            Long userId
    );

    @EntityGraph(
            attributePaths = {
                    "voucher",
                    "voucher.shop"
            }
    )
    List<UserVoucher>
    findByUser_IdAndUsedTrueOrderByUsedAtDesc(
            Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT uv
        FROM UserVoucher uv
        JOIN FETCH uv.voucher v
        LEFT JOIN FETCH v.shop
        WHERE uv.user.id = :userId
          AND LOWER(v.code) = LOWER(:code)
        """)
    Optional<UserVoucher> findByUserIdAndVoucherCodeForUpdate(
            @Param("userId") Long userId,
            @Param("code") String code
    );

}