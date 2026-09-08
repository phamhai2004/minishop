package com.example.minishop.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "product_variant_options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_variant_option_value",
                        columnNames = {"variant_id", "option_value_id"}
                )
        }
)
public class ProductVariantOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "variant_id",
            nullable = false
    )
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "option_value_id",
            nullable = false
    )
    private ProductOptionValue optionValue;

    public ProductVariantOption() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public ProductOptionValue getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(ProductOptionValue optionValue) {
        this.optionValue = optionValue;
    }
}