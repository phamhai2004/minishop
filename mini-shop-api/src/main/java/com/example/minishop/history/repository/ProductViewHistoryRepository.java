package com.example.minishop.history.repository;

import com.example.minishop.history.entity.ProductViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductViewHistoryRepository
        extends JpaRepository<ProductViewHistory, Long> {

    List<ProductViewHistory> findTop20ByUser_IdOrderByViewedAtDesc(
            Long userId
    );

}