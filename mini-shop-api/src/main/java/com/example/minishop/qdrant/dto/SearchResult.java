package com.example.minishop.qdrant.dto;

import java.util.Map;

public class SearchResult {

    private Long id;

    private Float score;

    private Map<String,Object> payload;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Map<String,Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String,Object> payload) {
        this.payload = payload;
    }
}