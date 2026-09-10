package com.example.minishop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class QdrantConfig {

    @Bean
    public RestClient qdrantRestClient(QdrantProperties properties) {

        String protocol = properties.isHttps() ? "https://" : "http://";

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(
                        protocol +
                                properties.getHost() +
                                ":" +
                                properties.getPort()
                );

        if (properties.getApiKey() != null
                && !properties.getApiKey().isBlank()) {
            builder.defaultHeader(
                    "api-key",
                    properties.getApiKey()
            );
        }

        return builder.build();
    }
}