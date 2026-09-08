package com.example.minishop.history.service;

import com.example.minishop.constant.CacheNames;
import com.example.minishop.entity.Product;
import com.example.minishop.entity.User;
import com.example.minishop.history.entity.ProductViewHistory;
import com.example.minishop.history.repository.ProductViewHistoryRepository;
import com.example.minishop.repository.ProductRepository;
import com.example.minishop.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductViewHistoryServiceImpl
        implements ProductViewHistoryService {

    private final ProductViewHistoryRepository repository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ProductViewHistoryServiceImpl(
            ProductViewHistoryRepository repository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }
    @Override
    @CacheEvict(
            cacheNames = CacheNames.USER_RECOMMENDATIONS,
            key = "#userId"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Long userId, Long productId) {

        User user = userRepository.findById(userId)
                .orElseThrow();

        Product product = productRepository.findById(productId)
                .orElseThrow();

        repository.save(
                new ProductViewHistory(user, product)
        );
    }
}