package com.example.minishop.service;

import com.example.minishop.constant.OrderStatus;
import com.example.minishop.dto.request.CreateReviewRequest;
import com.example.minishop.dto.response.PendingReviewResponse;
import com.example.minishop.dto.response.ReviewResponse;
import com.example.minishop.entity.*;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.mapper.ReviewMapper;
import com.example.minishop.repository.OrderRepository;
import com.example.minishop.repository.ProductImageRepository;
import com.example.minishop.repository.ReviewRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ReviewMapper reviewMapper;
    private final ProductImageRepository productImageRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderRepository orderRepository,
            ProductService productService,
            ReviewMapper reviewMapper,
            ProductImageRepository productImageRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.reviewMapper = reviewMapper;
        this.productImageRepository = productImageRepository;
    }

    @Transactional
    public ReviewResponse create(CreateReviewRequest request) {

        User user = SecurityUtils.getCurrentUser().getUser();

        Product product =
                productService.getProductEntityById(request.getProductId());

        boolean purchased = orderRepository
                .existsByUser_IdAndStatusAndItems_Product_Id(
                        user.getId(),
                        OrderStatus.COMPLETED,
                        product.getId()
                );

        if (!purchased) {
            throw new BadRequestException(
                    "Bạn chỉ có thể đánh giá sản phẩm sau khi đã nhận hàng"
            );
        }

        if (reviewRepository.existsByUser_IdAndProduct_Id(
                user.getId(),
                product.getId()
        )) {
            throw new BadRequestException(
                    "Bạn đã đánh giá sản phẩm này rồi"
            );
        }

        Review review = new Review();

        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        return reviewMapper.toResponse(
                reviewRepository.save(review)
        );
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getByProduct(Long productId) {
        productService.getProductEntityById(productId);

        return reviewRepository.findByProduct_IdOrderByCreatedAtDesc(productId)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews() {

        Long userId =
                SecurityUtils.getCurrentUserId();

        return reviewRepository
                .findByUser_IdOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::toMyReviewResponse)
                .toList();
    }

    private ReviewResponse toMyReviewResponse(
            Review review
    ) {
        ReviewResponse response =
                reviewMapper.toResponse(review);

        Product product =
                review.getProduct();

        if (product.getShop() != null) {

            response.setShopId(
                    product.getShop().getId()
            );

            response.setShopName(
                    product.getShop().getName()
            );
        }

        response.setImageUrl(
                productImageRepository
                        .findFirstByProduct_IdOrderByDisplayOrderAsc(
                                product.getId()
                        )
                        .map(image ->
                                image.getImageUrl()
                        )
                        .orElse(null)
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<PendingReviewResponse>
    getMyPendingReviews() {

        Long userId = SecurityUtils.getCurrentUserId();

        Set<Long> reviewedProductIds =
                reviewRepository
                        .findByUser_IdOrderByCreatedAtDesc(
                                userId
                        )
                        .stream()
                        .map(review ->
                                review
                                        .getProduct()
                                        .getId()
                        )
                        .collect(
                                java.util.stream.Collectors
                                        .toSet()
                        );

        List<Order> completedOrders =
                new ArrayList<>(
                        orderRepository
                                .findByUser_IdAndStatus(
                                        userId,
                                        OrderStatus.COMPLETED
                                )
                );

        completedOrders.sort(
                Comparator.comparing(
                        Order::getCreatedAt,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                ).reversed()
        );

        List<PendingReviewResponse> result =
                new ArrayList<>();

        Set<Long> addedProductIds =
                new HashSet<>();

        for (Order order : completedOrders) {

            for (OrderItem item :
                    order.getItems()) {

                Product product =
                        item.getProduct();

                if (product == null) {
                    continue;
                }

                Long productId =
                        product.getId();

                if (reviewedProductIds.contains(
                        productId
                )) {
                    continue;
                }

                if (!addedProductIds.add(
                        productId
                )) {
                    continue;
                }

                PendingReviewResponse response =
                        new PendingReviewResponse();

                response.setProductId(
                        productId
                );

                response.setProductName(
                        product.getName()
                );

                if (product.getShop() != null) {

                    response.setShopId(
                            product
                                    .getShop()
                                    .getId()
                    );

                    response.setShopName(
                            product
                                    .getShop()
                                    .getName()
                    );
                }

                response.setImageUrl(
                        productImageRepository
                                .findFirstByProduct_IdOrderByDisplayOrderAsc(
                                        productId
                                )
                                .map(image ->
                                        image.getImageUrl()
                                )
                                .orElse(null)
                );

                if (item.getShopOrder() != null) {

                    response.setOrderCode(
                            item
                                    .getShopOrder()
                                    .getOrderCode()
                    );

                    response.setCompletedAt(
                            item
                                    .getShopOrder()
                                    .getCompletedAt()
                    );
                }

                /*
                 * Cho dữ liệu cũ nếu
                 * completedAt chưa có.
                 */
                if (response.getCompletedAt()
                        == null) {

                    response.setCompletedAt(
                            order.getCreatedAt()
                    );
                }

                result.add(response);
            }
        }

        return result;
    }

    @Transactional
    public void deleteMyReview(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Review review = reviewRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đánh giá này"));

        reviewRepository.delete(review);
    }
}