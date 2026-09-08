package com.example.minishop.ai.qdrant;

import com.example.minishop.config.QdrantProperties;
import com.example.minishop.qdrant.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class QdrantService {

    private final RestClient restClient;
    private final QdrantProperties properties;

    public QdrantService(RestClient restClient,
                         QdrantProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    private String baseUrl() {
        return "http://" +
                properties.getHost() +
                ":" +
                properties.getPort();
    }

    private static final Logger log =
            LoggerFactory.getLogger(QdrantService.class);

    public void createCollection() {

        Map<String, Object> vectors = Map.of(
                "size", 512,
                "distance", "Cosine"
        );

        Map<String, Object> request = Map.of(
                "vectors", vectors
        );

        try {

            restClient.put()
                    .uri(baseUrl() + "/collections/products")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Qdrant collection created.");

        } catch (HttpClientErrorException.Conflict ex) {

            log.info("Qdrant collection already exists.");

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Cannot create Qdrant collection",
                    ex
            );
        }
    }

    public void upsertProduct(
            Long productId,
            List<Float> embedding
    ) {
        QdrantPoint point = new QdrantPoint();

        point.setId(productId);

        point.setVector(embedding);

        point.setPayload(
                Map.of(
                        "productId",
                        productId
                )
        );

        UpsertRequest request = new UpsertRequest();

        request.setPoints(
                List.of(point)
        );

       try {
           restClient.put()
                   .uri(baseUrl()
                           + "/collections/products/points")
                   .body(request)
                   .retrieve()
                   .toBodilessEntity();
       } catch (Exception ex) {

           throw new RuntimeException(
                   "Cannot upsert vector to Qdrant",
                   ex
           );

       }
    }

    public List<SearchResult> search(
            List<Float> embedding,
            int limit
    ) {

        SearchRequest request = new SearchRequest();

        request.setVector(embedding);
        request.setLimit(limit);

        SearchResponse response =
                restClient.post()
                        .uri(baseUrl()
                                + "/collections/products/points/search")
                        .body(request)
                        .retrieve()
                        .body(SearchResponse.class);

        if (response == null || response.getResult() == null) {
            return List.of();
        }

        response.getResult().forEach(r ->
                log.info("Product {} score={}",
                        r.getId(),
                        r.getScore())
        );

        return response.getResult();
    }

    public List<Float> getEmbedding(Long productId) {

        GetPointRequest request = new GetPointRequest();
        request.setIds(List.of(productId));
        request.setWithVector(true);

        GetPointResponse response =
                restClient.post()
                        .uri(baseUrl()
                                + "/collections/products/points")
                        .body(request)
                        .retrieve()
                        .body(GetPointResponse.class);

        if (response == null
                || response.getResult() == null
                || response.getResult().isEmpty()) {

            return List.of();
        }

        return response.getResult()
                .getFirst()
                .getVector();
    }
}