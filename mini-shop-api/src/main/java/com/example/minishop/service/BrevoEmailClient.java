package com.example.minishop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class BrevoEmailClient {

    private final RestClient restClient;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.api-url:https://api.brevo.com/v3/smtp/email}")
    private String apiUrl;

    @Value("${app.mail.from-email}")
    private String senderEmail;

    @Value("${app.mail.from-name}")
    private String senderName;

    public BrevoEmailClient() {
        this.restClient = RestClient.create();
    }

    public void sendHtmlEmail(
            String to,
            String subject,
            String htmlContent
    ) {

        Map<String, Object> body = Map.of(
                "sender", Map.of(
                        "name", senderName,
                        "email", senderEmail
                ),
                "to", List.of(
                        Map.of("email", to)
                ),
                "subject", subject,
                "htmlContent", htmlContent
        );

        restClient.post()
                .uri(apiUrl)
                .header("api-key", apiKey)
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}