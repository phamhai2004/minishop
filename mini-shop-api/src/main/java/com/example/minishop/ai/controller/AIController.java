package com.example.minishop.ai.controller;

import com.example.minishop.ai.dto.EmbeddingResponse;
import com.example.minishop.ai.service.AIService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(
            value = "/embedding",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public EmbeddingResponse embedding(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        return aiService.createEmbedding(file);

    }

}