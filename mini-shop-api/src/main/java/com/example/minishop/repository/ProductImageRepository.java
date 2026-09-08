package com.example.minishop.repository;

import com.example.minishop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct_Id(Long productId);

    Optional<ProductImage> findByIdAndProduct_Shop_Id(
            Long imageId,
            Long shopId
    );

    long countByProduct_Id(Long productId);

    Optional<ProductImage>
    findFirstByProduct_IdOrderByDisplayOrderAsc(Long productId);
}
