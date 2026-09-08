package com.example.minishop.ai.recommendation;

import java.util.List;

public interface UserEmbeddingService {

    List<Float> average(
            List<List<Float>> embeddings
    );

    List<Float> weightedAverage(
            List<List<Float>> embeddings,
            List<Float> weights
    );

    List<Float> combine(
            List<Float> first,
            float firstWeight,
            List<Float> second,
            float secondWeight
    );
    
}