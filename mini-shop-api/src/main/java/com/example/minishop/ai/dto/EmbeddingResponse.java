package com.example.minishop.ai.dto;

import java.util.List;

public class EmbeddingResponse {

    private Integer dimension;

    private List<Float> embedding;

    public EmbeddingResponse() {
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public List<Float> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }
}