package com.example.minishop.specification;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.entity.Product;
import com.example.minishop.entity.ProductVariant;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> nameContains(
            String keyword
    ) {
        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + keyword.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<Product> hasCategory(
            Long categoryId
    ) {
        return (root, query, cb) -> {

            if (categoryId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("category").get("id"),
                    categoryId
            );
        };
    }

    public static Specification<Product> belongsToShop(
            Long shopId
    ) {
        return (root, query, cb) -> {

            if (shopId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("shop").get("id"),
                    shopId
            );
        };
    }

    public static Specification<Product> priceFrom(
            BigDecimal minPrice
    ) {
        return (root, query, cb) -> {

            if (minPrice == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(
                    root.get("price"),
                    minPrice
            );
        };
    }

    public static Specification<Product> priceTo(
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {

            if (maxPrice == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(
                    root.get("price"),
                    maxPrice
            );
        };
    }

    public static Specification<Product> hasStatus(
            ProductStatus status
    ) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Product> shopHasStatus(
            ShopStatus status
    ) {
        return (root, query, cb) ->
                cb.equal(
                        root.get("shop").get("status"),
                        status
                );
    }

    public static Specification<Product> orderByEffectiveQuantity(
            boolean descending
    ) {
        return (root, query, cb) -> {
            if (Long.class.equals(query.getResultType())
                    || long.class.equals(query.getResultType())) {

                return cb.conjunction();
            }

            Subquery<Long> variantQuantitySum =
                    query.subquery(Long.class);

            Root<ProductVariant> variantRoot =
                    variantQuantitySum.from(ProductVariant.class);

            variantQuantitySum.select(
                    cb.sumAsLong(
                            variantRoot.<Integer>get("quantity")
                    )
            );

            variantQuantitySum.where(
                    cb.equal(
                            variantRoot.get("product"),
                            root
                    )
            );

            CriteriaBuilder.Coalesce<Long> effectiveQuantity =
                    cb.coalesce();

            effectiveQuantity.value(variantQuantitySum);

            effectiveQuantity.value(
                    root.<Integer>get("quantity")
                            .as(Long.class)
            );

            effectiveQuantity.value(0L);

            if (descending) {
                query.orderBy(
                        cb.desc(effectiveQuantity),
                        cb.desc(root.get("id"))
                );
            } else {
                query.orderBy(
                        cb.asc(effectiveQuantity),
                        cb.asc(root.get("id"))
                );
            }

            return cb.conjunction();
        };
    }
}