package com.example.minishop.mapper;

import com.example.minishop.dto.response.ReviewResponse;
import com.example.minishop.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();

        response.setId(review.getId());
        response.setProductId(review.getProduct().getId());
        response.setProductName(review.getProduct().getName());
        response.setUserId(review.getUser().getId());
        response.setFullName(review.getUser().getFullName());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());

        return response;
    }
}