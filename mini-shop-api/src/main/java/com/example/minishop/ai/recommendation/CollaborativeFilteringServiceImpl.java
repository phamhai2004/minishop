package com.example.minishop.ai.recommendation;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.entity.Order;
import com.example.minishop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class CollaborativeFilteringServiceImpl
        implements CollaborativeFilteringService {

    private final OrderRepository orderRepository;

    public CollaborativeFilteringServiceImpl(
            OrderRepository orderRepository
    ) {
        this.orderRepository = orderRepository;
    }

    private double jaccard(
            Set<Long> first,
            Set<Long> second
    ) {

        Set<Long> intersection =
                new HashSet<>(first);

        intersection.retainAll(second);

        Set<Long> union =
                new HashSet<>(first);

        union.addAll(second);

        if (union.isEmpty()) {
            return 0;
        }

        return (double) intersection.size()
                / union.size();
    }

    @Override
    public List<Long> recommend(Long userId) {

        List<Order> orders =
                orderRepository.findByStatus(
                        OrderStatus.COMPLETED
                );

        Map<Long, Set<Long>> userProducts =
                new HashMap<>();

        for (Order order : orders) {

            Long uid =
                    order.getUser().getId();

            Set<Long> products =
                    userProducts.computeIfAbsent(
                            uid,
                            k -> new HashSet<>()
                    );

            order.getItems()
                    .forEach(item ->
                            products.add(
                                    item.getProduct().getId()
                            ));
        }

        Set<Long> currentProducts =
                userProducts.get(userId);

        if (currentProducts == null) {
            return List.of();
        }
        Map<Long, Double> similarity =
                new HashMap<>();

        for (Map.Entry<Long, Set<Long>> entry
                : userProducts.entrySet()) {

            Long otherUser =
                    entry.getKey();

            if (otherUser.equals(userId)) {
                continue;
            }

            double score =
                    jaccard(
                            currentProducts,
                            entry.getValue()
                    );

            similarity.put(
                    otherUser,
                    score
            );
        }

        List<Long> similarUsers = similarity.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<Long, Double>comparingByValue()
                                .reversed()
                )
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        Set<Long> recommendations = new LinkedHashSet<>();

        for (Long similarUser : similarUsers) {

            Set<Long> products =
                    userProducts.get(similarUser);

            if (products == null) {
                continue;
            }

            for (Long productId : products) {

                if (!currentProducts.contains(productId)) {

                    recommendations.add(productId);

                }

            }

        }
        return recommendations.stream()
                .limit(20)
                .toList();
    }

}