package com.example.minishop.ai.service;

import com.example.minishop.ai.client.AIClient;
import com.example.minishop.ai.dto.EmbeddingResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AIService {

    private final AIClient aiClient;

    public AIService(AIClient aiClient) {
        this.aiClient = aiClient;
    }

    public EmbeddingResponse createEmbedding(MultipartFile file)
            throws IOException {

        return aiClient.generateEmbedding(file);

    }

}