package com.example.minishop.repository;

import com.example.minishop.entity.ProductOptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionTypeRepository
        extends JpaRepository<ProductOptionType, Long> {

    List<ProductOptionType> findAllByOrderByNameAsc();
}