package com.example.minishop.qdrant.dto;

import java.util.List;

public class SearchRequest {

    private List<Float> vector;

    private Integer limit;

    public List<Float> getVector() {
        return vector;
    }

    public void setVector(List<Float> vector) {
        this.vector = vector;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}