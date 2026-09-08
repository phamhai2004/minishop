package com.example.minishop.analytics.entity;

import com.example.minishop.analytics.RecommendationEventType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_metrics")
public class RecommendationMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long productId;

    @Enumerated(EnumType.STRING)
    private RecommendationEventType eventType;

    private LocalDateTime createdAt = LocalDateTime.now();

    public RecommendationMetric() {
    }

    public RecommendationMetric(
            Long userId,
            Long productId,
            RecommendationEventType eventType
    ) {
        this.userId = userId;
        this.productId = productId;
        this.eventType = eventType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public RecommendationEventType getEventType() {
        return eventType;
    }

    public void setEventType(RecommendationEventType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}