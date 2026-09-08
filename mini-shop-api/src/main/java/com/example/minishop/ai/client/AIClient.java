package com.example.minishop.ai.client;

import com.example.minishop.ai.dto.AIResponse;
import com.example.minishop.ai.dto.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class AIClient {

    private final RestClient restClient;

    @Value("${ai.service.url}")
    private String aiUrl;

    public AIClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public EmbeddingResponse generateEmbedding(MultipartFile file)
            throws IOException {

        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        AIResponse<EmbeddingResponse> response =
                restClient.post()
                        .uri(aiUrl + "/api/v1/embedding")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(body)
                        .retrieve()
                        .body(new ParameterizedTypeReference<AIResponse<EmbeddingResponse>>() {});

        return response.getData();
    }
}
