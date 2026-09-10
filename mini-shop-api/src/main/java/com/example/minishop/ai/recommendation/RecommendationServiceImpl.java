package com.example.minishop.ai.recommendation;

import com.example.minishop.ai.qdrant.QdrantService;
import com.example.minishop.constant.CacheNames;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.entity.Product;
import com.example.minishop.history.entity.ProductViewHistory;
import com.example.minishop.history.repository.ProductViewHistoryRepository;
import com.example.minishop.qdrant.dto.SearchResult;
import com.example.minishop.repository.ProductRepository;
import com.example.minishop.service.ProductService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl
        implements RecommendationService {

    private static final double DECAY_RATE = 0.2;

    private final ProductViewHistoryRepository historyRepository;
    private final QdrantService qdrantService;
    private final UserEmbeddingService userEmbeddingService;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PurchaseHistoryService purchaseHistoryService;
    private final CollaborativeFilteringService collaborativeFilteringService;

    public RecommendationServiceImpl(
            ProductViewHistoryRepository historyRepository,
            QdrantService qdrantService,
            UserEmbeddingService userEmbeddingService,
            ProductRepository productRepository,
            ProductService productService,
            PurchaseHistoryService purchaseHistoryService,
            CollaborativeFilteringService collaborativeFilteringService
    ) {
        this.historyRepository = historyRepository;
        this.qdrantService = qdrantService;
        this.userEmbeddingService = userEmbeddingService;
        this.productRepository = productRepository;
        this.productService = productService;
        this.purchaseHistoryService = purchaseHistoryService;
        this.collaborativeFilteringService = collaborativeFilteringService;
    }

    private float calculateWeight(LocalDateTime viewedAt) {

        long days = ChronoUnit.DAYS.between(
                viewedAt,
                LocalDateTime.now()
        );

        return (float) Math.exp(
                -DECAY_RATE * days
        );
    }

    @Cacheable(
            cacheNames = CacheNames.USER_RECOMMENDATIONS,
            key = "#userId",
            unless = "#result.isEmpty()"
    )
    @Override
    public List<ProductResponse> recommend(Long userId) {

        List<ProductViewHistory> history =
                historyRepository
                        .findTop20ByUser_IdOrderByViewedAtDesc(userId);

        if (history.isEmpty()) {
            return List.of();
        }

        List<List<Float>> embeddings = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        Set<Long> viewedIds = new HashSet<>();
        Set<Long> purchasedIds = new HashSet<>();

        for (ProductViewHistory view : history) {

            viewedIds.add(view.getProduct().getId());

            List<Float> embedding =
                    qdrantService.getEmbedding(
                            view.getProduct().getId()
                    );

            if (!embedding.isEmpty()) {

                embeddings.add(embedding);

                weights.add(
                        calculateWeight(
                                view.getViewedAt()
                        )
                );

            }

        }

        if (embeddings.isEmpty()) {
            return List.of();
        }

        List<Float> viewEmbedding =
                userEmbeddingService.weightedAverage(
                        embeddings,
                        weights
                );

        List<Product> purchasedProducts =
                purchaseHistoryService.getPurchasedProducts(userId);

        List<List<Float>> purchaseEmbeddings = new ArrayList<>();

        for (Product product : purchasedProducts) {

            purchasedIds.add(product.getId());

            List<Float> embedding =
                    qdrantService.getEmbedding(product.getId());

            if (!embedding.isEmpty()) {
                purchaseEmbeddings.add(embedding);
            }
        }

        List<Float> purchaseEmbedding =
                userEmbeddingService.average(
                        purchaseEmbeddings
                );

        List<Float> userEmbedding =
                userEmbeddingService.combine(
                        viewEmbedding,
                        0.3F,
                        purchaseEmbedding,
                        0.7F
                );

        if (userEmbedding.isEmpty()) {
            return List.of();
        }

        List<SearchResult> results =
                qdrantService.search(
                        userEmbedding,
                        50
                );

        if (results.isEmpty()) {
            return List.of();
        }

        List<Long> ids = results.stream()
                .filter(r ->
                        !viewedIds.contains(r.getId())
                                &&
                                !purchasedIds.contains(r.getId())
                )
                .map(SearchResult::getId)
                .toList();

        List<Long> collaborativeIds =
                collaborativeFilteringService
                        .recommend(userId);

        LinkedHashSet<Long> finalIds = new LinkedHashSet<>();

        finalIds.addAll(ids);
        finalIds.addAll(collaborativeIds);

        if (finalIds.isEmpty()) {
            return List.of();
        }

        List<Long> recommendationIds = finalIds.stream()
                .limit(20)
                .toList();
        List<Product> products =
                productRepository.findByIdIn(
                        recommendationIds
                );
        Map<Long, Product> map =
                products.stream()
                        .collect(Collectors.toMap(
                                Product::getId,
                                p -> p
                        ));

        List<Product> orderedProducts =
                recommendationIds.stream()
                        .map(map::get)
                        .filter(Objects::nonNull)
                        .toList();

        return productService
                .toProductResponses(
                        orderedProducts
                );
    }
}