package com.example.minishop.service;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.dto.request.SellerFlashSaleRequest;
import com.example.minishop.dto.response.SellerFlashSaleResponse;
import com.example.minishop.entity.FlashSale;
import com.example.minishop.entity.Product;
import com.example.minishop.entity.ProductVariant;
import com.example.minishop.entity.Shop;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.FlashSaleMapper;
import com.example.minishop.repository.FlashSaleRepository;
import com.example.minishop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SellerFlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final ProductRepository productRepository;
    private final ShopService shopService;
    private final ProductCacheService productCacheService;
    private final FlashSaleMapper flashSaleMapper;

    public SellerFlashSaleService(
            FlashSaleRepository flashSaleRepository,
            ProductRepository productRepository,
            ShopService shopService,
            ProductCacheService productCacheService,
            FlashSaleMapper flashSaleMapper
    ) {
        this.flashSaleRepository = flashSaleRepository;
        this.productRepository = productRepository;
        this.shopService = shopService;
        this.productCacheService = productCacheService;
        this.flashSaleMapper = flashSaleMapper;
    }

    @Transactional
    public SellerFlashSaleResponse create(SellerFlashSaleRequest request) {
        Shop shop = shopService.getActiveCurrentSellerShop();

        Product product = productRepository
                .findByIdAndShop_Id(
                        request.getProductId(),
                        shop.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm thuộc shop của bạn"
                ));

        validateRequest(request, product, 0);

        if (flashSaleRepository.existsOverlappingFlashSale(
                product.getId(),
                request.getStartTime(),
                request.getEndTime()
        )) {
            throw new BadRequestException(
                    "Sản phẩm đã có Flash Sale trong khoảng thời gian này"
            );
        }

        FlashSale flashSale = new FlashSale();

        flashSale.setProduct(product);
        flashSale.setSalePrice(request.getSalePrice());
        flashSale.setQuantity(request.getQuantity());
        flashSale.setSold(0);
        flashSale.setStartTime(request.getStartTime());
        flashSale.setEndTime(request.getEndTime());
        flashSale.setActive(true);

        FlashSale savedFlashSale =
                flashSaleRepository.save(flashSale);

        productCacheService.evictProduct(
                product.getId()
        );

        return flashSaleMapper.toSellerResponse(
                savedFlashSale
        );
    }

    @Transactional(readOnly = true)
    public Page<SellerFlashSaleResponse> getAll(
            Pageable pageable
    ) {
        Shop shop =
                shopService.getActiveCurrentSellerShop();

        return flashSaleRepository
                .findByProduct_Shop_Id(
                        shop.getId(),
                        pageable
                )
                .map(
                        flashSaleMapper::toSellerResponse
                );
    }

    @Transactional
    public SellerFlashSaleResponse update(
            Long flashSaleId,
            SellerFlashSaleRequest request
    ) {
        Shop shop =
                shopService.getActiveCurrentSellerShop();

        FlashSale flashSale =
                flashSaleRepository
                        .findByIdAndProduct_Shop_Id(
                                flashSaleId,
                                shop.getId()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy Flash Sale thuộc shop của bạn"
                                )
                        );

        Product product = flashSale.getProduct();

        if (!product.getId().equals(
                request.getProductId()
        )) {
            throw new BadRequestException(
                    "Không được thay đổi sản phẩm của Flash Sale"
            );
        }

        validateRequest(request, product, flashSale.getSold());

        if (flashSale.getSold() != null
                && flashSale.getSold() > 0) {

            boolean issuedConditionsChanged =
                    flashSale.getSalePrice()
                            .compareTo(
                                    request.getSalePrice()
                            ) != 0
                            ||
                            !flashSale.getStartTime()
                                    .equals(
                                            request.getStartTime()
                                    )
                            ||
                            !flashSale.getEndTime()
                                    .equals(
                                            request.getEndTime()
                                    );

            if (issuedConditionsChanged) {
                throw new BadRequestException(
                        "Flash Sale đã có sản phẩm được bán, không thể thay đổi giá hoặc thời gian"
                );
            }
        }

        if (flashSaleRepository
                .existsOverlappingFlashSaleForUpdate(
                        product.getId(),
                        flashSale.getId(),
                        request.getStartTime(),
                        request.getEndTime()
                )) {

            throw new BadRequestException(
                    "Sản phẩm đã có Flash Sale trong khoảng thời gian này"
            );
        }

        flashSale.setSalePrice(request.getSalePrice());
        flashSale.setQuantity(request.getQuantity());
        flashSale.setStartTime(request.getStartTime());
        flashSale.setEndTime(request.getEndTime());
        FlashSale savedFlashSale = flashSaleRepository.save(flashSale);
        productCacheService.evictProduct(product.getId());

        return flashSaleMapper
                .toSellerResponse(
                        savedFlashSale
                );
    }

    @Transactional
    public SellerFlashSaleResponse deactivate(
            Long flashSaleId
    ) {
        Shop shop =
                shopService.getActiveCurrentSellerShop();

        FlashSale flashSale =
                flashSaleRepository
                        .findByIdAndProduct_Shop_Id(
                                flashSaleId,
                                shop.getId()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy Flash Sale thuộc shop của bạn"
                                )
                        );

        if (!Boolean.TRUE.equals(
                flashSale.getActive()
        )) {
            throw new BadRequestException(
                    "Flash Sale đã được tắt trước đó"
            );
        }

        flashSale.setActive(false);

        FlashSale savedFlashSale =
                flashSaleRepository.save(
                        flashSale
                );

        productCacheService.evictProduct(
                flashSale.getProduct().getId()
        );

        return flashSaleMapper
                .toSellerResponse(
                        savedFlashSale
                );
    }

    private int calculateAvailableStock(
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

    private BigDecimal calculateMinimumNormalPrice(
            Product product
    ) {
        if (product.getVariants() == null
                || product.getVariants().isEmpty()) {

            return product.getPrice();
        }

        return product.getVariants()
                .stream()
                .map(ProductVariant::getPrice)
                .filter(price -> price != null)
                .min(BigDecimal::compareTo)
                .orElse(product.getPrice());
    }

    private void validateRequest(
            SellerFlashSaleRequest request,
            Product product,
            Integer sold
    ) {
        if (!request.getEndTime()
                .isAfter(
                        request.getStartTime()
                )) {

            throw new BadRequestException(
                    "Thời gian kết thúc phải sau thời gian bắt đầu"
            );
        }

        if (product.getStatus()
                != ProductStatus.ACTIVE) {

            throw new BadRequestException(
                    "Sản phẩm hiện không được bán"
            );
        }

        BigDecimal minimumNormalPrice =
                calculateMinimumNormalPrice(
                        product
                );

        if (minimumNormalPrice == null) {
            throw new BadRequestException(
                    "Sản phẩm không có giá hợp lệ"
            );
        }

        if (request.getSalePrice()
                .compareTo(
                        minimumNormalPrice
                ) >= 0) {

            throw new BadRequestException(
                    "Giá Flash Sale phải nhỏ hơn giá bán thấp nhất của sản phẩm"
            );
        }

        int availableStock =
                calculateAvailableStock(
                        product
                );

        int soldQuantity =
                sold != null
                        ? sold
                        : 0;

        int remainingQuota =
                request.getQuantity()
                        - soldQuantity;

        if (remainingQuota < 0) {
            throw new BadRequestException(
                    "Số lượng Flash Sale không được nhỏ hơn số lượng đã bán"
            );
        }

        if (remainingQuota
                > availableStock) {

            throw new BadRequestException(
                    "Số lượng Flash Sale còn lại không được vượt quá tồn kho hiện tại"
            );
        }
    }
}