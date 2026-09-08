package com.example.minishop.qdrant.dto;

import java.util.List;

public class GetPointResult {

    private Long id;

    private List<Float> vector;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Float> getVector() {
        return vector;
    }

    public void setVector(List<Float> vector) {
        this.vector = vector;
    }
}