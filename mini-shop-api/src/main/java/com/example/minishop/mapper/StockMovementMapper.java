package com.example.minishop.mapper;

import com.example.minishop.dto.response.StockMovementResponse;
import com.example.minishop.entity.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementResponse toResponse(StockMovement movement) {
        StockMovementResponse response = new StockMovementResponse();

        response.setId(movement.getId());
        response.setProductId(movement.getProduct().getId());
        response.setProductName(movement.getProduct().getName());
        response.setVariantId(movement.getVariant() != null ? movement.getVariant().getId() : null);
        response.setType(movement.getType());
        response.setQuantity(movement.getQuantity());
        response.setBeforeQuantity(movement.getBeforeQuantity());
        response.setAfterQuantity(movement.getAfterQuantity());
        response.setReason(movement.getReason());
        response.setCreatedAt(movement.getCreatedAt());

        return response;
    }
}