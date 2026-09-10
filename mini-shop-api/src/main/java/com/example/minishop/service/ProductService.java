package com.example.minishop.service;

import com.example.minishop.ai.client.AIClient;
import com.example.minishop.ai.dto.EmbeddingResponse;
import com.example.minishop.ai.qdrant.QdrantService;
import com.example.minishop.constant.CacheNames;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.dto.request.ProductRequest;
import com.example.minishop.dto.request.ProductSearchRequest;
import com.example.minishop.entity.FlashSale;
import com.example.minishop.entity.Shop;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.entity.Category;
import com.example.minishop.entity.Product;
import com.example.minishop.history.service.ProductViewHistoryService;
import com.example.minishop.mapper.ProductMapper;
import com.example.minishop.projection.ProductSummary;
import com.example.minishop.qdrant.dto.SearchResult;
import com.example.minishop.repository.FlashSaleRepository;
import com.example.minishop.repository.ProductRepository;
import com.example.minishop.security.SecurityUtils;
import com.example.minishop.specification.ProductSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final PricingService pricingService;
    private final ShopService shopService;
    private final AIClient aiClient;
    private final QdrantService qdrantService;
    private final ProductViewHistoryService productViewHistoryService;
    private final FlashSaleRepository flashSaleRepository;
    public ProductService(
            ProductMapper productMapper,
            CategoryService categoryService,
            ProductRepository productRepository,
            PricingService pricingService,
            ShopService shopService,
            AIClient aiClient,
            QdrantService qdrantService,
            ProductViewHistoryService productViewHistoryService,
            FlashSaleRepository flashSaleRepository
    ) {
        this.productMapper = productMapper;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.pricingService = pricingService;
        this.shopService = shopService;
        this.aiClient = aiClient;
        this.qdrantService = qdrantService;
        this.productViewHistoryService = productViewHistoryService;
        this.flashSaleRepository = flashSaleRepository;
    }

    private Product getEntityById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với id: "+id));
    }

        public ProductResponse toProductResponse(Product product) {
        Category category = product.getCategory();

        ProductResponse response = productMapper.toResponse(product, category);

        FlashSale flashSale = pricingService.getActiveFlashSale(product);

        if (flashSale != null) {
            response.setFlashSale(true);
            response.setSalePrice(flashSale.getSalePrice());
            response.setFlashSaleQuantity(flashSale.getQuantity());
            response.setFlashSaleSold(flashSale.getSold());
            response.setRemain(flashSale.getQuantity() - flashSale.getSold());
        } else {
            response.setFlashSale(false);
            response.setSalePrice(null);
        }

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

    private String validateSortField(String sort) {
        return switch (sort) {
            case "id", "name", "price", "quantity" -> sort;
            default -> throw new BadRequestException("Trường sort không hợp lệ: " + sort);
        };
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            int page,
            int size,
            String sort,
            String direction
    ) {
        String sortField =
                validateSortField(sort);

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        if ("quantity".equals(sortField)) {

            Pageable pageable = PageRequest.of(page, size);
            Specification<Product> specification =
                    Specification.allOf(
                            ProductSpecification.hasStatus(ProductStatus.ACTIVE),
                            ProductSpecification.shopHasStatus(ShopStatus.ACTIVE),
                            ProductSpecification
                                    .orderByEffectiveQuantity(sortDirection == Sort.Direction.DESC)
                    );

            Page<Product> productPage =
                    productRepository.findAll(
                            specification,
                            pageable
                    );

            return mapProductsWithFlashSales(productPage);
        }

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortDirection, sortField)
                );

        Specification<Product> specification =
                Specification.allOf(
                        ProductSpecification
                                .hasStatus(ProductStatus.ACTIVE),

                        ProductSpecification
                                .shopHasStatus(ShopStatus.ACTIVE)
                );

        Page<Product> productPage =
                productRepository.findAll(
                        specification,
                        pageable
                );

        return mapProductsWithFlashSales(productPage);
    }

    @Transactional(readOnly = true)
    public List<ProductSummary> getSummary(){

        return productRepository.findAllSummary();

    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PRODUCT_DETAIL,
            key = "#id",
            unless = "#result == null"
    )
    public ProductResponse getById(Long id) {
        Product product = getEntityById(id);

        return toProductResponse(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PUBLIC_PRODUCT_DETAIL,
            key = "#id",
            unless = "#result == null"
    )
    public ProductResponse getPublicProductById(Long id) {

        Product product = productRepository
                .findByIdAndStatusAndShop_Status(
                        id,
                        ProductStatus.ACTIVE,
                        ShopStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm"
                ));

        try {
            Long userId = SecurityUtils.getCurrentUserId();

            if (userId != null) {
                productViewHistoryService.save(
                        userId,
                        product.getId()
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return toProductResponse(product);
    }
    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        Category category =
                categoryService.getEntityById(
                        request.getCategoryId()
                );

        Shop shop =
                shopService.getVerifiedActiveCurrentSellerShop();

        Product product = productMapper.toEntity(request);

        product.setCategory(category);
        product.setShop(shop);
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        return toProductResponse(
                productRepository.save(product)
        );
    }
    @Transactional
    @CacheEvict(
            cacheNames = CacheNames.PRODUCT_DETAIL,
            key = "#id"
    )
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request
    ) {
        Product product = getMyProductEntityById(id);

        Category category =
                categoryService.getEntityById(
                        request.getCategoryId()
                );

        productMapper.updateEntity(product, request);
        product.setCategory(category);

        return toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("Không được để trống");
        }

        List<Product> products =
                productRepository
                        .findByNameContainingIgnoreCase(keyword);

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByPriceGreaterThan(
            BigDecimal price
    ) {
        List<Product> products =
                productRepository
                        .findByPriceGreaterThan(price);

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByPriceBetween(
            BigDecimal min,
            BigDecimal max
    ) {
        if (min.compareTo(max) > 0) {
            throw new BadRequestException(
                    "Giá min không được lớn hơn giá max"
            );
        }

        List<Product> products =
                productRepository
                        .findByPriceBetween(min, max);

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByQuantityGreaterThan(
            Integer quantity
    ) {
        if (quantity == null || quantity < 0) {
            throw new BadRequestException(
                    "Số lượng phải lớn hơn hoặc bằng 0"
            );
        }

        List<Product> products =
                productRepository
                        .findByEffectiveQuantityGreaterThan(quantity);

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public long countProductsByCategory(Long categoryId) {
        categoryService.getEntityById(categoryId);
        return productRepository.countByCategory_Id(categoryId);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse>
    getProductsByCategoryOrderByPriceDesc(
            Long categoryId
    ) {
        categoryService.getEntityById(categoryId);

        List<Product> products =
                productRepository
                        .findByCategory_IdOrderByPriceDesc(
                                categoryId
                        );

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findProductsByCategoryName(
            String name
    ) {
        List<Product> products =
                productRepository
                        .findProductsByCategoryName(name);

        return toProductResponses(products);
    }

    private Product getMyProductEntityById(Long productId) {
        Shop shop = shopService.getActiveCurrentSellerShop();

        return productRepository
                .findByIdAndShop_Id(productId, shop.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm thuộc shop của bạn"
                ));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getMyShopProducts(
            int page,
            int size
    ) {
        Shop shop =
                shopService.getCurrentSellerShop();

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        Page<Product> productPage =
                productRepository
                        .findByShop_Id(
                                shop.getId(),
                                pageable
                        );

        return mapProductsWithFlashSales(productPage);
    }

    @Transactional(readOnly = true)
    public Integer getStock(Long id) {
        Product product = getEntityById(id);

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

    @Transactional(readOnly = true)
    public Product getProductEntityById(Long id) {
        return getEntityById(id);
    }

    @Transactional
    @CacheEvict(
            cacheNames = CacheNames.PRODUCT_DETAIL,
            key = "#id"
    )
    public void deleteProduct(Long id) {
        Product product = getMyProductEntityById(id);

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            ProductSearchRequest request,
            int page,
            int size,
            String sort,
            String direction
    ) {
        validateSearchRequest(request);

        String sortField =
                validateSortField(sort);

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Specification<Product> specification =
                Specification.allOf(
                        ProductSpecification.nameContains(
                                request.getKeyword()
                        ),
                        ProductSpecification.hasCategory(
                                request.getCategoryId()
                        ),
                        ProductSpecification.belongsToShop(
                                request.getShopId()
                        ),
                        ProductSpecification.priceFrom(
                                request.getMinPrice()
                        ),
                        ProductSpecification.priceTo(
                                request.getMaxPrice()
                        ),
                        ProductSpecification.hasStatus(
                                ProductStatus.ACTIVE
                        ),
                        ProductSpecification.shopHasStatus(
                                ShopStatus.ACTIVE
                        )
                );

        Pageable pageable;

        if ("quantity".equals(sortField)) {

            pageable =
                    PageRequest.of(
                            page,
                            size
                    );

            specification =
                    specification.and(
                            ProductSpecification
                                    .orderByEffectiveQuantity(
                                            sortDirection
                                                    == Sort.Direction.DESC
                                    )
                    );

        } else {

            pageable =
                    PageRequest.of(
                            page,
                            size,
                            Sort.by(
                                    sortDirection,
                                    sortField
                            )
                    );
        }
        Page<Product> productPage =
                productRepository.findAll(
                        specification,
                        pageable
                );

        return mapProductsWithFlashSales(productPage);
    }

    private void validateSearchRequest(
            ProductSearchRequest request
    ) {

        if (request.getMinPrice() != null
                && request.getMinPrice().signum() < 0) {
            throw new BadRequestException(
                    "Giá tối thiểu không được âm"
            );
        }

        if (request.getMaxPrice() != null
                && request.getMaxPrice().signum() < 0) {
            throw new BadRequestException(
                    "Giá tối đa không được âm"
            );
        }

        if (request.getMinPrice() != null
                && request.getMaxPrice() != null
                && request.getMinPrice()
                .compareTo(request.getMaxPrice()) > 0) {
            throw new BadRequestException(
                    "Giá tối thiểu không được lớn hơn giá tối đa"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<String> getProductSuggestions(
            String keyword,
            int limit
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        int safeLimit = Math.min(
                Math.max(limit, 1),
                20
        );

        String normalizedKeyword =
                keyword.trim();

        return productRepository
                .findProductNameSuggestions(
                        normalizedKeyword,
                        ProductStatus.ACTIVE,
                        ShopStatus.ACTIVE,
                        PageRequest.of(0, safeLimit)
                );
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchByImage(
            MultipartFile file
    ) throws IOException {

        EmbeddingResponse embedding =
                aiClient.generateEmbedding(file);

        List<SearchResult> results =
                qdrantService.search(
                        embedding.getEmbedding(),
                        20
                );

        float threshold = 0.65f;

        List<Long> ids = results.stream()
                .filter(r -> r.getScore() >= threshold)
                .map(SearchResult::getId)
                .toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        List<Product> products =
                productRepository.findByIdIn(ids);

        Map<Long, Product> map =
                products.stream()
                        .collect(Collectors.toMap(
                                Product::getId,
                                p -> p
                        ));

        List<Product> orderedProducts =
                ids.stream()
                        .map(map::get)
                        .filter(Objects::nonNull)
                        .toList();

        return toProductResponses(orderedProducts);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getSimilarProducts(
            Long productId
    ) {

        Product product = getEntityById(productId);

        List<Float> embedding =
                qdrantService.getEmbedding(product.getId());

        if (embedding.isEmpty()) {
            return List.of();
        }

        List<SearchResult> results =
                qdrantService.search(
                        embedding,
                        10
                );

        float threshold = 0.65f;

        List<Long> ids = results.stream()
                .filter(r -> !r.getId().equals(productId))
                .filter(r -> r.getScore() >= threshold)
                .map(SearchResult::getId)
                .toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        List<Product> products =
                productRepository.findByIdIn(ids);

        Map<Long, Product> map =
                products.stream()
                        .collect(Collectors.toMap(
                                Product::getId,
                                p -> p
                        ));

        List<Product> orderedProducts =
                ids.stream()
                        .map(map::get)
                        .filter(Objects::nonNull)
                        .toList();

        return toProductResponses(orderedProducts);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse>
    getProductsByCategoryAndMaxPrice(
            String categoryName,
            BigDecimal maxPrice
    ) {
        List<Product> products =
                productRepository
                        .findByCategory_NameIgnoreCaseAndPriceLessThanEqualAndStatusAndShop_Status(
                                categoryName,
                                maxPrice,
                                ProductStatus.ACTIVE,
                                ShopStatus.ACTIVE
                        );

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProductsForChat(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        String normalizedKeyword =
                keyword == null
                        ? ""
                        : keyword.trim();

        List<Product> products =
                productRepository.searchProductsForChat(
                        normalizedKeyword,
                        categoryId,
                        minPrice,
                        maxPrice,
                        ProductStatus.ACTIVE,
                        ShopStatus.ACTIVE,
                        PageRequest.of(0, 10)
                );

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProductsForChat() {

        List<Product> products =
                productRepository
                        .findActiveProductsForChat(
                                ProductStatus.ACTIVE,
                                ShopStatus.ACTIVE
                        );

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public Product getPublicProductEntityById(
            Long id
    ) {

        return productRepository
                .findByIdAndStatusAndShop_Status(
                        id,
                        ProductStatus.ACTIVE,
                        ShopStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sản phẩm hiện không khả dụng"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse>
    getPublicProductsByCategory(
            Long categoryId,
            int page,
            int size
    ) {
        categoryService.getEntityById(categoryId);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "id"
                        )
                );

        Page<Product> productPage =
                productRepository
                        .findByCategory_IdAndStatusAndShop_Status(
                                categoryId,
                                ProductStatus.ACTIVE,
                                ShopStatus.ACTIVE,
                                pageable
                        );

        return mapProductsWithFlashSales(productPage);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse>
    getTopSellingProductsForChat() {

        List<Product> products =
                productRepository
                        .findTopSellingProducts(
                                ProductStatus.ACTIVE,
                                ShopStatus.ACTIVE,
                                PaymentStatus.PAID,
                                PageRequest.of(0, 5)
                        );

        return toProductResponses(products);
    }

    private ProductResponse toProductResponse(
            Product product,
            FlashSale flashSale
    ) {
        Category category = product.getCategory();

        ProductResponse response =
                productMapper.toResponse(product, category);

        if (flashSale != null) {
            response.setFlashSale(true);
            response.setSalePrice(flashSale.getSalePrice());
            response.setFlashSaleQuantity(flashSale.getQuantity());
            response.setFlashSaleSold(flashSale.getSold());
            response.setRemain(
                    flashSale.getQuantity() - flashSale.getSold()
            );
        } else {
            response.setFlashSale(false);
            response.setSalePrice(null);
        }

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

    public List<ProductResponse> toProductResponses(
            List<Product> products
    ) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        List<Long> productIds =
                products.stream()
                        .map(Product::getId)
                        .toList();

        LocalDateTime now = LocalDateTime.now();

        Map<Long, FlashSale> flashSaleMap =
                flashSaleRepository
                        .findActiveAvailableFlashSales(
                                productIds,
                                now
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        flashSale ->
                                                flashSale.getProduct().getId(),
                                        Function.identity(),
                                        (existing, replacement) ->
                                                existing
                                )
                        );

        return products.stream()
                .map(product ->
                        toProductResponse(
                                product,
                                flashSaleMap.get(product.getId())
                        )
                )
                .toList();
    }

    private Page<ProductResponse> mapProductsWithFlashSales(
            Page<Product> productPage
    ) {
        if (productPage.isEmpty()) {
            return productPage.map(
                    product -> toProductResponse(product, null)
            );
        }

        List<ProductResponse> responses =
                toProductResponses(
                        productPage.getContent()
                );

        Map<Long, ProductResponse> responseMap =
                responses.stream()
                        .collect(
                                Collectors.toMap(
                                        ProductResponse::getId,
                                        Function.identity()
                                )
                        );

        return productPage.map(
                product ->
                        responseMap.get(product.getId())
        );
    }
}
