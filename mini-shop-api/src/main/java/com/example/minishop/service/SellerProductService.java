package com.example.minishop.service;


import com.example.minishop.constant.CacheNames;
import com.example.minishop.constant.ProductStatus;
import com.example.minishop.dto.request.ProductRequest;
import com.example.minishop.dto.request.ProductVariantRequest;
import com.example.minishop.dto.request.UpdateProductStatusRequest;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.entity.*;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.ProductMapper;
import com.example.minishop.repository.ProductOptionValueRepository;
import com.example.minishop.repository.ProductRepository;
import com.example.minishop.repository.ProductVariantRepository;
import com.example.minishop.specification.ProductSpecification;
import jakarta.persistence.EntityManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SellerProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ShopService shopService;
    private final PricingService pricingService;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductVariantRepository productVariantRepository;
    private final EntityManager entityManager;

    public SellerProductService(
            ProductRepository productRepository,
            ProductMapper productMapper,
            CategoryService categoryService,
            ShopService shopService,
            PricingService pricingService,
            ProductOptionValueRepository productOptionValueRepository,
            ProductVariantRepository productVariantRepository,
            EntityManager entityManager
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryService = categoryService;
        this.shopService = shopService;
        this.pricingService = pricingService;
        this.productOptionValueRepository = productOptionValueRepository;
        this.productVariantRepository = productVariantRepository;
        this.entityManager = entityManager;
    }
    private Product getMyProductEntityById(Long productId) {
        Shop shop = shopService.getActiveCurrentSellerShop();

        return productRepository
                .findByIdAndShop_Id(productId, shop.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm thuộc shop của bạn"
                ));
    }

    private ProductResponse toResponse(Product product) {

        Category category = product.getCategory();

        ProductResponse response =
                productMapper.toResponse(product, category);

        if (product.getShop() != null) {
            response.setShopId(product.getShop().getId());
            response.setShopName(product.getShop().getName());
        }

        response.setStatus(product.getStatus());

        if (product.getStatus() != null) {
            response.setStatusName(
                    product.getStatus().getDisplayName()
            );
        }

        return response;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {

        Shop shop =
                shopService.getActiveCurrentSellerShop();

        Category category =
                categoryService.getEntityById(
                        request.getCategoryId()
                );

        Product product =
                productMapper.toEntity(request);

        product.setCategory(category);
        product.setShop(shop);
        product.setStatus(ProductStatus.ACTIVE);

        List<ProductVariantRequest> variants =
                request.getVariants();

        if (variants == null || variants.isEmpty()) {

            createVariants(product, variants);

        } else {
            createVariants(product, variants);

            product.setPrice(
                    variants.stream()
                            .map(ProductVariantRequest::getPrice)
                            .min(java.math.BigDecimal::compareTo)
                            .orElseThrow(() ->
                                    new BadRequestException(
                                            "Không thể xác định giá sản phẩm"
                                    )
                            )
            );
            product.setQuantity(
                    variants.stream()
                            .mapToInt(ProductVariantRequest::getQuantity)
                            .sum()
            );
        }

        Product savedProduct =
                productRepository.save(product);

        return toResponse(savedProduct);
    }
    @Transactional(readOnly = true)
    public Page<ProductResponse> getMyProducts(
            int page,
            int size,
            String sort,
            String direction
    ) {
        Shop shop =
                shopService.getCurrentSellerShop();

        validatePagination(page, size);

        Sort.Direction sortDirection =
                parseDirection(direction);

        String sortField =
                validateSortField(sort);
        if ("quantity".equals(sortField)) {

            Pageable pageable =
                    PageRequest.of(
                            page,
                            size
                    );

            Specification<Product> specification =
                    Specification.allOf(
                            ProductSpecification.belongsToShop(
                                    shop.getId()
                            ),
                            ProductSpecification.orderByEffectiveQuantity(
                                    sortDirection
                                            == Sort.Direction.DESC
                            )
                    );

            return productRepository
                    .findAll(
                            specification,
                            pageable
                    )
                    .map(this::toResponse);
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                sortDirection,
                                sortField
                        )
                );

        return productRepository
                .findByShop_Id(
                        shop.getId(),
                        pageable
                )
                .map(this::toResponse);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new BadRequestException(
                    "Page không được nhỏ hơn 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException(
                    "Size phải nằm trong khoảng 1 đến 100"
            );
        }
    }

    private Sort.Direction parseDirection(String direction) {
        if ("asc".equalsIgnoreCase(direction)) {
            return Sort.Direction.ASC;
        }

        if ("desc".equalsIgnoreCase(direction)) {
            return Sort.Direction.DESC;
        }

        throw new BadRequestException(
                "Direction chỉ chấp nhận asc hoặc desc"
        );
    }
    private String validateSortField(String sort) {
        return switch (sort) {
            case "id", "name", "price", "quantity", "status" -> sort;
            default -> throw new BadRequestException(
                    "Trường sort không hợp lệ: " + sort
            );
        };
    }

    @Transactional(readOnly = true)
    public ProductResponse getMyProductById(Long id) {
        return toResponse(getMyProductEntityById(id));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.PRODUCT_DETAIL,
                    key = "#id"
            ),
            @CacheEvict(
                    cacheNames = CacheNames.PUBLIC_PRODUCT_DETAIL,
                    key = "#id"
            )
    })
    public ProductResponse update(
            Long id,
            ProductRequest request
    ) {
        Product product = getMyProductEntityById(id);

        Category category =
                categoryService.getEntityById(request.getCategoryId());

        productMapper.updateEntity(product, request);
        product.setCategory(category);

        List<ProductVariantRequest> variants =
                request.getVariants();

        if (variants == null || variants.isEmpty()) {

            product.getVariants().clear();

            if (request.getPrice() == null
                    || request.getPrice().compareTo(
                    java.math.BigDecimal.ZERO
            ) <= 0) {

                throw new BadRequestException(
                        "Giá sản phẩm phải lớn hơn 0"
                );
            }

            if (request.getQuantity() == null
                    || request.getQuantity() < 1) {

                throw new BadRequestException(
                        "Số lượng sản phẩm phải lớn hơn 0"
                );
            }

            product.setPrice(request.getPrice());
            product.setQuantity(request.getQuantity());

        }
        else {

            product.getVariants().clear();
            entityManager.flush();

            createVariants(product, variants);

            product.setPrice(
                    variants.stream()
                            .map(ProductVariantRequest::getPrice)
                            .min(java.math.BigDecimal::compareTo)
                            .orElseThrow(() ->
                                    new BadRequestException(
                                            "Không thể xác định giá sản phẩm"
                                    )
                            )
            );

            product.setQuantity(
                    variants.stream()
                            .mapToInt(ProductVariantRequest::getQuantity)
                            .sum()
            );
        }

        return toResponse(product);
    }
    

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.PRODUCT_DETAIL,
                    key = "#id"
            ),
            @CacheEvict(
                    cacheNames = CacheNames.PUBLIC_PRODUCT_DETAIL,
                    key = "#id"
            )
    })
    public ProductResponse updateStatus(
            Long id,
            UpdateProductStatusRequest request
    ) {
        Product product = getMyProductEntityById(id);

        ProductStatus nextStatus = request.getStatus();

        if (product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new BadRequestException(
                    "Không thể thay đổi trạng thái sản phẩm đã ngừng kinh doanh"
            );
        }

        if (nextStatus == ProductStatus.DISCONTINUED) {
            throw new BadRequestException(
                    "Vui lòng sử dụng chức năng ngừng kinh doanh"
            );
        }

        if (nextStatus == ProductStatus.ACTIVE
                && calculateEffectiveQuantity(product) <= 0) {

            throw new BadRequestException(
                    "Không thể mở bán sản phẩm đã hết hàng"
            );
        }

        product.setStatus(nextStatus);

        return toResponse(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.PRODUCT_DETAIL,
                    key = "#id"
            ),
            @CacheEvict(
                    cacheNames = CacheNames.PUBLIC_PRODUCT_DETAIL,
                    key = "#id"
            )
    })
    public ProductResponse discontinue(Long id) {
        Product product = getMyProductEntityById(id);

        if (product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new BadRequestException("Sản phẩm đã ngừng kinh doanh");
        }

        product.setStatus(ProductStatus.DISCONTINUED);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchMyProducts(
            String keyword,
            int page,
            int size,
            String sort,
            String direction
    ) {
        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException(
                    "Từ khóa tìm kiếm không được để trống"
            );
        }

        validatePagination(page, size);

        Shop shop =
                shopService.getCurrentSellerShop();

        String sortField =
                validateSortField(sort);

        Sort.Direction sortDirection =
                parseDirection(direction);

        if ("quantity".equals(sortField)) {

            Pageable pageable =
                    PageRequest.of(
                            page,
                            size
                    );

            Specification<Product> specification =
                    Specification.allOf(
                            ProductSpecification.belongsToShop(
                                    shop.getId()
                            ),
                            ProductSpecification.nameContains(
                                    keyword.trim()
                            ),
                            ProductSpecification.orderByEffectiveQuantity(
                                    sortDirection
                                            == Sort.Direction.DESC
                            )
                    );

            return productRepository
                    .findAll(
                            specification,
                            pageable
                    )
                    .map(this::toResponse);
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                sortDirection,
                                sortField
                        )
                );

        return productRepository
                .findByShop_IdAndNameContainingIgnoreCase(
                        shop.getId(),
                        keyword.trim(),
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long countMyProducts() {
        Shop shop = shopService.getCurrentSellerShop();

        return productRepository.countByShop_Id(shop.getId());
    }

    private void createVariants(
            Product product,
            List<ProductVariantRequest> variantRequests
    ) {
        if (variantRequests == null || variantRequests.isEmpty()) {
            return;
        }

        Shop shop = product.getShop();

        if (shop == null) {
            throw new BadRequestException(
                    "Sản phẩm chưa được gán shop"
            );
        }

        Set<String> skuSet = new HashSet<>();

        for (ProductVariantRequest request : variantRequests) {

            String sku = request.getSku();

            if (sku != null && !sku.isBlank()) {

                sku = sku.trim();

                if (!skuSet.add(sku)) {
                    throw new BadRequestException(
                            "SKU '" + sku
                                    + "' bị trùng trong danh sách biến thể"
                    );
                }

                boolean exists =
                        productVariantRepository
                                .existsByShop_IdAndSku(
                                        shop.getId(),
                                        sku
                                );

                if (exists) {
                    throw new BadRequestException(
                            "SKU '" + sku
                                    + "' đã tồn tại trong shop"
                    );
                }

                request.setSku(sku);
            }

            if (request.getPrice() == null
                    || request.getPrice().compareTo(
                    java.math.BigDecimal.ZERO
            ) <= 0) {

                throw new BadRequestException(
                        "Giá của biến thể phải lớn hơn 0"
                );
            }

            if (request.getQuantity() == null
                    || request.getQuantity() < 1) {

                throw new BadRequestException(
                        "Số lượng của biến thể phải lớn hơn 0"
                );
            }

            ProductVariant variant = new ProductVariant();

            variant.setProduct(product);
            variant.setShop(shop);
            variant.setPrice(request.getPrice());
            variant.setQuantity(request.getQuantity());
            variant.setSku(request.getSku());

            if (request.getOptionValueIds() == null
                    || request.getOptionValueIds().isEmpty()) {

                throw new BadRequestException(
                        "Mỗi biến thể phải có ít nhất một giá trị phân loại"
                );
            }

            for (Long optionValueId : request.getOptionValueIds()) {

                ProductOptionValue optionValue =
                        productOptionValueRepository
                                .findById(optionValueId)
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy giá trị phân loại: "
                                                        + optionValueId
                                        )
                                );

                ProductVariantOption variantOption =
                        new ProductVariantOption();

                variantOption.setVariant(variant);
                variantOption.setOptionValue(optionValue);

                variant.getOptions().add(variantOption);
            }

            product.getVariants().add(variant);
        }
    }

    private int calculateEffectiveQuantity(
            Product product
    ) {
        if (product.getVariants() == null
                || product.getVariants().isEmpty()) {

            return product.getQuantity() != null
                    ? product.getQuantity()
                    : 0;
        }

        return product.getVariants()
                .stream()
                .mapToInt(variant ->
                        variant.getQuantity() != null
                                ? variant.getQuantity()
                                : 0
                )
                .sum();
    }
}