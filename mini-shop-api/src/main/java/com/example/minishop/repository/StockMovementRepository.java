package com.example.minishop.repository;

import com.example.minishop.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProduct_IdOrderByCreatedAtDesc(Long productId);
}