package com.example.minishop.repository;

import com.example.minishop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
    SELECT i.product.id, i
    FROM ProductImage i
    WHERE i.product.id IN :productIds
    ORDER BY i.product.id ASC,
             i.displayOrder ASC,
             i.id ASC
""")
    List<Object[]> findImagesByProductIds(
            @Param("productIds") List<Long> productIds
    );
}
