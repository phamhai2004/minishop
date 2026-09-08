package com.example.minishop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class QdrantConfig {

    @Bean
    public RestClient qdrantRestClient(QdrantProperties properties) {

        return RestClient.builder()
                .baseUrl(
                        "http://" +
                                properties.getHost() +
                                ":" +
                                properties.getPort()
                )
                .build();
    }

}