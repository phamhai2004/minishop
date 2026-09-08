package com.example.minishop.ai.recommendation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserEmbeddingServiceImpl
        implements UserEmbeddingService {

    @Override
    public List<Float> average(
            List<List<Float>> embeddings
    ) {

        if (embeddings == null
                || embeddings.isEmpty()) {

            return List.of();
        }

        int dimension =
                embeddings.getFirst().size();

        List<Float> result =
                new ArrayList<>();

        for (int i = 0; i < dimension; i++) {

            result.add(0F);

        }

        for (List<Float> embedding : embeddings) {

            for (int i = 0; i < dimension; i++) {

                result.set(
                        i,
                        result.get(i)
                                + embedding.get(i)
                );

            }

        }

        for (int i = 0; i < dimension; i++) {

            result.set(
                    i,
                    result.get(i)
                            / embeddings.size()
            );

        }

        return result;

    }

    @Override
    public List<Float> weightedAverage(
            List<List<Float>> embeddings,
            List<Float> weights
    ) {

        if (embeddings.isEmpty()) {
            return List.of();
        }

        if (embeddings.size() != weights.size()) {
            throw new IllegalArgumentException(
                    "Embeddings và weights phải cùng kích thước"
            );
        }

        int dimension = embeddings.get(0).size();

        float[] result = new float[dimension];

        float totalWeight = 0F;

        for (int i = 0; i < embeddings.size(); i++) {

            List<Float> embedding = embeddings.get(i);

            float weight = weights.get(i);

            totalWeight += weight;

            for (int j = 0; j < dimension; j++) {

                result[j] += embedding.get(j) * weight;

            }
        }

        List<Float> average = new ArrayList<>(dimension);

        for (int i = 0; i < dimension; i++) {

            average.add(result[i] / totalWeight);

        }

        return average;
    }

    @Override
    public List<Float> combine(
            List<Float> first,
            float firstWeight,
            List<Float> second,
            float secondWeight
    ) {

        if (first.isEmpty()) {
            return second;
        }

        if (second.isEmpty()) {
            return first;
        }

        if (first.size() != second.size()) {
            throw new IllegalArgumentException(
                    "Embedding dimension mismatch"
            );
        }

        float totalWeight = firstWeight + secondWeight;

        List<Float> result = new ArrayList<>(first.size());

        for (int i = 0; i < first.size(); i++) {

            float value =
                    (first.get(i) * firstWeight
                            + second.get(i) * secondWeight)
                            / totalWeight;

            result.add(value);
        }

        return result;
    }

}