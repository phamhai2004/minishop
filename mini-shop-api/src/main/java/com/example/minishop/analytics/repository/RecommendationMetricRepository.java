package com.example.minishop.analytics.repository;

import com.example.minishop.analytics.entity.RecommendationMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationMetricRepository
        extends JpaRepository<
        RecommendationMetric,
        Long> {
}