package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.CreateReviewRequest;
import com.example.minishop.dto.response.PendingReviewResponse;
import com.example.minishop.dto.response.ReviewResponse;
import com.example.minishop.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Đánh giá sản phẩm thành công", reviewService.create(request))
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByProduct(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewService.getByProduct(productId))
        );
    }

    @GetMapping("/my")
    public ResponseEntity<
            ApiResponse<List<ReviewResponse>>
            > getMyReviews() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        reviewService.getMyReviews()
                )
        );
    }

    @GetMapping("/my/pending")
    public ResponseEntity<
            ApiResponse<List<PendingReviewResponse>>
            > getMyPendingReviews() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        reviewService
                                .getMyPendingReviews()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {

        reviewService.deleteMyReview(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã xóa review",
                        null
                )
        );
    }
}