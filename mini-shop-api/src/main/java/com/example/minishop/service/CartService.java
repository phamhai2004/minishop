package com.example.minishop.service;

import com.example.minishop.dto.request.AddToCartRequest;
import com.example.minishop.dto.request.UpdateCartItemRequest;
import com.example.minishop.dto.response.CartResponse;
import com.example.minishop.entity.*;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.CartMapper;
import com.example.minishop.repository.CartItemRepository;
import com.example.minishop.repository.CartRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final CartMapper cartMapper;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductService productService,
            CartMapper cartMapper
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.cartMapper = cartMapper;
    }

    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        User user = SecurityUtils.getCurrentUser().getUser();

        Cart cart = getOrCreateCart(user);

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        User user = SecurityUtils.getCurrentUser().getUser();

        Cart cart = getOrCreateCart(user);

        Product product = productService.getProductEntityById(request.getProductId());

        ProductVariant variant = null;

        if (request.getVariantId() != null) {
            variant = product.getVariants()
                    .stream()
                    .filter(v -> v.getId().equals(request.getVariantId()))
                    .findFirst()
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm")
                    );

            if (variant.getQuantity() < request.getQuantity()) {
                throw new BadRequestException("Biến thể sản phẩm không đủ tồn kho");
            }
        } else {
            if (product.getQuantity() < request.getQuantity()) {
                throw new BadRequestException("Sản phẩm không đủ tồn kho");
            }
        }

        CartItem item;

        if (variant != null) {
            item = cartItemRepository
                    .findByCart_IdAndProduct_IdAndVariant_Id(
                            cart.getId(),
                            product.getId(),
                            variant.getId()
                    )
                    .orElse(null);
        } else {
            item = cartItemRepository
                    .findByCart_IdAndProduct_IdAndVariantIsNull(
                            cart.getId(),
                            product.getId()
                    )
                    .orElse(null);
        }

        if (item == null) {
            item = new CartItem();

            item.setProduct(product);
            item.setVariant(variant);
            item.setQuantity(request.getQuantity());

            cart.addItem(item);

        } else {
            int newQuantity =
                    item.getQuantity() + request.getQuantity();

            if (variant != null) {
                if (variant.getQuantity() < newQuantity) {
                    throw new BadRequestException(
                            "Số lượng trong giỏ vượt quá tồn kho của biến thể"
                    );
                }
            } else {
                if (product.getQuantity() < newQuantity) {
                    throw new BadRequestException(
                            "Số lượng trong giỏ vượt quá tồn kho"
                    );
                }
            }

            item.setQuantity(newQuantity);
        }

        Cart savedCart = cartRepository.save(cart);

        return cartMapper.toResponse(savedCart);
    }

    @Transactional
    public CartResponse updateItem(
            Long productId,
            Long variantId,
            UpdateCartItemRequest request
    ) {
        User user = SecurityUtils.getCurrentUser().getUser();

        Cart cart = getOrCreateCart(user);

        Product product = productService.getProductEntityById(productId);

        CartItem item;

        if (variantId != null) {

            ProductVariant variant = product.getVariants()
                    .stream()
                    .filter(v -> v.getId().equals(variantId))
                    .findFirst()
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Không tìm thấy biến thể sản phẩm"
                            )
                    );

            if (variant.getQuantity() < request.getQuantity()) {
                throw new BadRequestException(
                        "Số lượng vượt quá tồn kho của biến thể"
                );
            }

            item = cartItemRepository
                    .findByCart_IdAndProduct_IdAndVariant_Id(
                            cart.getId(),
                            productId,
                            variantId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Biến thể sản phẩm không có trong giỏ"
                            )
                    );

        } else {

            if (product.getQuantity() < request.getQuantity()) {
                throw new BadRequestException(
                        "Số lượng vượt quá tồn kho"
                );
            }

            item = cartItemRepository
                    .findByCart_IdAndProduct_IdAndVariantIsNull(
                            cart.getId(),
                            productId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Sản phẩm không có trong giỏ"
                            )
                    );
        }

        item.setQuantity(request.getQuantity());

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(
            Long productId,
            Long variantId
    ) {
        User user = SecurityUtils.getCurrentUser().getUser();

        Cart cart = getOrCreateCart(user);

        CartItem item;

        if (variantId != null) {

            item = cartItemRepository
                    .findByCart_IdAndProduct_IdAndVariant_Id(
                            cart.getId(),
                            productId,
                            variantId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Biến thể sản phẩm không có trong giỏ"
                            )
                    );

        } else {

            item = cartItemRepository
                    .findByCart_IdAndProduct_IdAndVariantIsNull(
                            cart.getId(),
                            productId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Sản phẩm không có trong giỏ"
                            )
                    );
        }

        cart.removeItem(item);

        return cartMapper.toResponse(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }
}