package com.example.minishop.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shop_follows",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shop_follow_user_shop",
                        columnNames = {
                                "user_id",
                                "shop_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_shop_follow_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_shop_follow_shop",
                        columnList = "shop_id"
                )
        }
)
public class ShopFollow {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "shop_id",
            nullable = false
    )
    private Shop shop;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }
}