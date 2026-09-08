package com.example.minishop.repository;

import com.example.minishop.constant.PaymentTransactionStatus;
import com.example.minishop.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionRef(
            String transactionRef
    );

    Optional<Payment>
    findByTransactionRefAndOrder_User_Id(
            String transactionRef,
            Long userId
    );

    Optional<Payment>
    findTopByOrder_IdOrderByCreatedAtDesc(
            Long orderId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select payment
            from Payment payment
            join fetch payment.order
            where payment.transactionRef = :transactionRef
            """)
    Optional<Payment> findByTransactionRefForUpdate(
            @Param("transactionRef")
            String transactionRef
    );

    List<Payment>
    findByOrder_IdOrderByCreatedAtDesc(
            Long orderId
    );

    boolean existsByOrder_IdAndStatusIn(
            Long orderId,
            Collection<PaymentTransactionStatus> statuses
    );
}