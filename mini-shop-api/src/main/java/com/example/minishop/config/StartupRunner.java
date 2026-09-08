package com.example.minishop.config;

import com.example.minishop.ai.qdrant.QdrantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final QdrantService qdrantService;

    public StartupRunner(QdrantService qdrantService) {
        this.qdrantService = qdrantService;
    }

    @Override
    public void run(String... args) {

        qdrantService.createCollection();

    }

}