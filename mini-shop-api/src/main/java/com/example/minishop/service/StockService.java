package com.example.minishop.service;

import com.example.minishop.constant.StockMovementType;
import com.example.minishop.entity.*;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.repository.StockMovementRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class StockService {

    private final StockMovementRepository stockMovementRepository;

    public StockService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    public void decreaseStock(
            Product product,
            ProductVariant variant,
            Integer quantity,
            Order order
    ) {
        if (variant != null) {

            if (variant.getQuantity() < quantity) {
                throw new BadRequestException(
                        "Biến thể sản phẩm không đủ tồn kho"
                );
            }

            Integer before = variant.getQuantity();
            Integer after = before - quantity;

            variant.setQuantity(after);

            createMovement(
                    product,
                    variant,
                    order,
                    StockMovementType.ORDER,
                    quantity,
                    before,
                    after,
                    "Trừ kho do đặt hàng"
            );

            return;
        }

        if (product.getQuantity() < quantity) {
            throw new BadRequestException(
                    "Sản phẩm không đủ tồn kho: " + product.getName()
            );
        }

        Integer before = product.getQuantity();
        Integer after = before - quantity;

        product.setQuantity(after);

        createMovement(
                product,
                null,
                order,
                StockMovementType.ORDER,
                quantity,
                before,
                after,
                "Trừ kho do đặt hàng"
        );
    }

    public void restoreStock(
            Product product,
            ProductVariant variant,
            Integer quantity,
            Order order
    ) {
        if (variant != null) {

            Integer before = variant.getQuantity();
            Integer after = before + quantity;

            variant.setQuantity(after);

            createMovement(
                    product,
                    variant,
                    order,
                    StockMovementType.CANCEL_ORDER,
                    quantity,
                    before,
                    after,
                    "Hoàn kho do hủy đơn hàng"
            );

            return;
        }

        Integer before = product.getQuantity();
        Integer after = before + quantity;

        product.setQuantity(after);

        createMovement(
                product,
                null,
                order,
                StockMovementType.CANCEL_ORDER,
                quantity,
                before,
                after,
                "Hoàn kho do hủy đơn hàng"
        );
    }

    private void createMovement(
            Product product,
            ProductVariant variant,
            Order order,
            StockMovementType type,
            Integer quantity,
            Integer before,
            Integer after,
            String reason
    ) {
        StockMovement movement = new StockMovement();

        movement.setProduct(product);
        movement.setVariant(variant);
        movement.setOrder(order);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setBeforeQuantity(before);
        movement.setAfterQuantity(after);
        movement.setReason(reason);
        movement.setCreatedAt(LocalDateTime.now());

        try {
            User user = SecurityUtils.getCurrentUser().getUser();
            movement.setCreatedBy(user);
        } catch (Exception ignored) {
        }

        stockMovementRepository.save(movement);
    }
}