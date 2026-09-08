package com.example.minishop.ai.recommendation;

import java.util.List;

public interface CollaborativeFilteringService {

    List<Long> recommend(Long userId);

}