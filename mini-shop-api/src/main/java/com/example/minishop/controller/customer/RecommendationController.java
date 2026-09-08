package com.example.minishop.controller.customer;

import com.example.minishop.ai.recommendation.RecommendationService;
import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService
    ) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recommendations")
    public ApiResponse<List<ProductResponse>> recommend() {

        Long userId = SecurityUtils.getCurrentUserId();

        List<ProductResponse> products =
                recommendationService.recommend(userId);

        return ApiResponse.success(products);
    }
    
}