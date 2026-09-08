package com.example.minishop.qdrant.dto;

import java.util.List;

public class SearchResponse {

    private List<SearchResult> result;

    public List<SearchResult> getResult() {
        return result;
    }

    public void setResult(List<SearchResult> result) {
        this.result = result;
    }
}