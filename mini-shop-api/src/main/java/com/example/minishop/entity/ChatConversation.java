package com.example.minishop.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_conversations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_conversation_customer_shop",
                        columnNames = {
                                "customer_id",
                                "shop_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_chat_conversation_customer",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_chat_conversation_shop",
                        columnList = "shop_id"
                ),
                @Index(
                        name = "idx_chat_conversation_last_message",
                        columnList = "last_message_at"
                )
        }
)
public class ChatConversation {

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
            name = "customer_id",
            nullable = false
    )
    private User customer;
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
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    private LocalDateTime lastMessageAt;
    @Column(name = "product_context_visible")
    private Boolean productContextVisible = true;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now =
                LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(
            User customer
    ) {
        this.customer = customer;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(
            Shop shop
    ) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(
            LocalDateTime lastMessageAt
    ) {
        this.lastMessageAt = lastMessageAt;
    }

    public Boolean getProductContextVisible() {
        return productContextVisible;
    }

    public void setProductContextVisible(Boolean productContextVisible) {
        this.productContextVisible = productContextVisible;
    }
}