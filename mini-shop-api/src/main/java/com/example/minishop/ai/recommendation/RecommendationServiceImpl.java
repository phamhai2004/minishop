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

        Set<Long> viewedIds = new HashSet<>();
        Set<Long> purchasedIds = new HashSet<>();

        List<Product> purchasedProducts =
                purchaseHistoryService
                        .getPurchasedProducts(userId);

        List<Long> viewedProductIds =
                history.stream()
                        .map(view -> {
                            Long productId =
                                    view.getProduct().getId();

                            viewedIds.add(productId);

                            return productId;
                        })
                        .distinct()
                        .toList();

        List<Long> purchasedProductIds =
                purchasedProducts.stream()
                        .map(product -> {
                            purchasedIds.add(product.getId());

                            return product.getId();
                        })
                        .distinct()
                        .toList();

        LinkedHashSet<Long> allProductIds =
                new LinkedHashSet<>();

        allProductIds.addAll(viewedProductIds);
        allProductIds.addAll(purchasedProductIds);

        Map<Long, List<Float>> embeddingMap =
                qdrantService.getEmbeddings(
                        new ArrayList<>(allProductIds)
                );

        List<List<Float>> viewEmbeddings =
                new ArrayList<>();

        List<Float> weights =
                new ArrayList<>();

        for (ProductViewHistory view : history) {

            Long productId =
                    view.getProduct().getId();

            List<Float> embedding =
                    embeddingMap.get(productId);

            if (embedding != null
                    && !embedding.isEmpty()) {

                viewEmbeddings.add(embedding);

                weights.add(
                        calculateWeight(
                                view.getViewedAt()
                        )
                );
            }
        }

        if (viewEmbeddings.isEmpty()) {
            return List.of();
        }

        List<Float> viewEmbedding =
                userEmbeddingService.weightedAverage(
                        viewEmbeddings,
                        weights
                );

        List<List<Float>> purchaseEmbeddings =
                purchasedProductIds.stream()
                        .map(embeddingMap::get)
                        .filter(Objects::nonNull)
                        .filter(embedding -> !embedding.isEmpty())
                        .toList();

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