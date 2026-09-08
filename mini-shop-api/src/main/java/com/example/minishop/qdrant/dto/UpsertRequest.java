package com.example.minishop.qdrant.dto;

import java.util.List;

public class UpsertRequest {

    private List<QdrantPoint> points;

    public List<QdrantPoint> getPoints() {
        return points;
    }

    public void setPoints(List<QdrantPoint> points) {
        this.points = points;
    }
}   