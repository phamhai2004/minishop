package com.example.minishop.analytics.service;

import com.example.minishop.analytics.RecommendationEventType;
import com.example.minishop.analytics.entity.RecommendationMetric;
import com.example.minishop.analytics.repository.RecommendationMetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecommendationMetricServiceImpl
        implements RecommendationMetricService {

    private final RecommendationMetricRepository repository;

    public RecommendationMetricServiceImpl(
            RecommendationMetricRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public void track(
            Long userId,
            Long productId,
            RecommendationEventType type
    ) {

        repository.save(
                new RecommendationMetric(
                        userId,
                        productId,
                        type
                )
        );

    }

}