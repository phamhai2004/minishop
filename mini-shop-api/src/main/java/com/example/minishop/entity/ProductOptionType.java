package com.example.minishop.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "product_option_types",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_option_type_name",
                        columnNames = "name"
                )
        }
)
public class ProductOptionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @OneToMany(
            mappedBy = "optionType",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProductOptionValue> values = new ArrayList<>();

    public ProductOptionType() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ProductOptionValue> getValues() {
        return values;
    }

    public void setValues(List<ProductOptionValue> values) {
        this.values = values;
    }
}