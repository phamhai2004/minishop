package com.example.minishop.ai.recommendation;

import com.example.minishop.dto.response.ProductResponse;

import java.util.List;

public interface RecommendationService {

    List<ProductResponse> recommend(Long userId);

}