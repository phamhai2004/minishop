package com.example.minishop.repository;

import com.example.minishop.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionValueRepository
        extends JpaRepository<ProductOptionValue, Long> {

    List<ProductOptionValue>
    findByOptionType_IdOrderByNameAsc(Long optionTypeId);
}