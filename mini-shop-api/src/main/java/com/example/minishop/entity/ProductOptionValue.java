package com.example.minishop.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "product_option_values",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_option_value_type_name",
                        columnNames = {"option_type_id", "name"}
                )
        }
)
public class ProductOptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "option_type_id",
            nullable = false
    )
    private ProductOptionType optionType;

    public ProductOptionValue() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductOptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(ProductOptionType optionType) {
        this.optionType = optionType;
    }

}