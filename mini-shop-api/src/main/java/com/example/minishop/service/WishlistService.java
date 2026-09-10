package com.example.minishop.service;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.dto.request.AddWishlistRequest;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.dto.response.WishlistResponse;
import com.example.minishop.entity.Product;
import com.example.minishop.entity.Wishlist;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.WishlistMapper;
import com.example.minishop.repository.WishlistRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductService productService;
    private  final WishlistMapper wishlistMapper;
    public WishlistService(WishlistRepository wishlistRepository, ProductService productService, WishlistMapper wishlistMapper){
        this.wishlistRepository = wishlistRepository;
        this.productService = productService;
        this.wishlistMapper = wishlistMapper;
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse>
    getMyWishlist() {

        Long userId = SecurityUtils.getCurrentUserId();

        return wishlistRepository
                .findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(wishlistMapper::toResponse)
                .toList();
    }

    @Transactional
    public WishlistResponse add(
            AddWishlistRequest request
    ){

        Long userId =
                SecurityUtils.getCurrentUserId();

        if(wishlistRepository.existsByUser_IdAndProduct_Id(
                userId,
                request.getProductId()
        )){

            throw new BadRequestException(
                    "Sản phẩm đã có trong Wishlist"
            );

        }

        Product product =
                productService
                        .getPublicProductEntityById(
                                request.getProductId()
                        );

        Wishlist wishlist =
                new Wishlist();

        wishlist.setUser(
                SecurityUtils
                        .getCurrentUser()
                        .getUser()
        );

        wishlist.setProduct(product);

        wishlist.setCreatedAt(
                LocalDateTime.now()
        );

        return wishlistMapper.toResponse(
                wishlistRepository.save(wishlist)
        );

    }

    @Transactional
    public void remove(
            Long productId
    ){

        Long userId =
                SecurityUtils.getCurrentUserId();

        Wishlist wishlist =
                wishlistRepository
                        .findByUser_IdAndProduct_Id(
                                userId,
                                productId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sản phẩm không có trong danh sách yêu thích"
                                )
                        );

        wishlistRepository.delete(wishlist);

    }

    @Transactional(readOnly = true)
    public List<ProductResponse>
    getTopFavoriteProductsForChat() {

        List<Product> products =
                wishlistRepository
                        .findTopFavoriteProducts(
                                ProductStatus.ACTIVE,
                                ShopStatus.ACTIVE,
                                PageRequest.of(0, 5)
                        );

        return productService
                .toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public long countByProductId(
            Long productId
    ) {

        return wishlistRepository
                .countByProduct_Id(
                        productId
                );
    }
}
