package com.example.minishop.ai.recommendation;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.entity.OrderItem;
import com.example.minishop.entity.Product;
import com.example.minishop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PurchaseHistoryServiceImpl
        implements PurchaseHistoryService {

    private final OrderRepository orderRepository;

    public PurchaseHistoryServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Product> getPurchasedProducts(Long userId) {

        return orderRepository.findByUser_IdAndStatus(
                        userId,
                        OrderStatus.COMPLETED
                )
                .stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItem::getProduct)
                .distinct()
                .toList();
    }
}