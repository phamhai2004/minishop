package com.example.minishop.qdrant.dto;

import java.util.List;

public class GetPointResponse {

    private List<GetPointResult> result;

    public List<GetPointResult> getResult() {
        return result;
    }

    public void setResult(List<GetPointResult> result) {
        this.result = result;
    }
}