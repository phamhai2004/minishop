package com.example.minishop.scheduler;

import com.example.minishop.ai.recommendation.RecommendationService;
import com.example.minishop.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecommendationRefreshScheduler {

    private final UserRepository userRepository;
    private final RecommendationService recommendationService;

    public RecommendationRefreshScheduler(
            UserRepository userRepository,
            RecommendationService recommendationService
    ) {
        this.userRepository = userRepository;
        this.recommendationService = recommendationService;
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void refreshRecommendations() {

        userRepository.findAll()
                .forEach(user ->
                        recommendationService.recommend(user.getId())
                );

    }

}