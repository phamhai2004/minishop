package com.example.minishop.qdrant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetPointRequest {

    private List<Long> ids;

    @JsonProperty("with_vector")
    private boolean withVector;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean isWithVector() {
        return withVector;
    }

    public void setWithVector(boolean withVector) {
        this.withVector = withVector;
    }
}