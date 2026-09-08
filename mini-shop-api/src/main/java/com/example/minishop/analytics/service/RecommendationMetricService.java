package com.example.minishop.analytics.service;

import com.example.minishop.analytics.RecommendationEventType;

public interface RecommendationMetricService {

    void track(
            Long userId,
            Long productId,
            RecommendationEventType type
    );

}
