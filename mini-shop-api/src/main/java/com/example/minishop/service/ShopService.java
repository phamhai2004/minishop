package com.example.minishop.service;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.Role;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.dto.request.CreateShopRequest;
import com.example.minishop.dto.request.UpdateShopRegistrationRequest;
import com.example.minishop.dto.request.UpdateShopRequest;
import com.example.minishop.dto.response.ShopCategoryResponse;
import com.example.minishop.dto.response.ShopResponse;
import com.example.minishop.dto.response.UploadImageResponse;
import com.example.minishop.entity.Shop;
import com.example.minishop.entity.User;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.ShopMapper;
import com.example.minishop.repository.*;
import com.example.minishop.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.minishop.security.SecurityUtils.getCurrentUser;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    private final ProductRepository productRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final ReviewRepository reviewRepository;
    private final ImageStorageService imageStorageService;
    private final ShopFollowRepository shopFollowRepository;
    private final CategoryService categoryService;

    public ShopService(
            ShopRepository shopRepository,
            ShopMapper shopMapper,
            ProductRepository productRepository,
            ShopOrderRepository shopOrderRepository,
            ReviewRepository reviewRepository,
            ImageStorageService imageStorageService,
            ShopFollowRepository shopFollowRepository,
            CategoryService categoryService
    ) {
        this.shopRepository = shopRepository;
        this.shopMapper = shopMapper;
        this.productRepository = productRepository;
        this.shopOrderRepository = shopOrderRepository;
        this.reviewRepository = reviewRepository;
        this.imageStorageService = imageStorageService;
        this.shopFollowRepository = shopFollowRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public ShopResponse registerShop(CreateShopRequest request) {
        User currentUser = getCurrentUser()
                .getUser();

        if (currentUser.getRole() != Role.CUSTOMER) {
            throw new BadRequestException(
                    "Chỉ tài khoản CUSTOMER mới có thể đăng ký shop"
            );
        }

        if (!Boolean.TRUE.equals(currentUser.getActive())) {
            throw new BadRequestException(
                    "Tài khoản đã bị khóa"
            );
        }

        if (shopRepository.existsByOwner_Id(currentUser.getId())) {
            throw new BadRequestException(
                    "Bạn đã sở hữu một shop"
            );
        }

        String shopName = request.getName().trim();

        if (shopRepository.existsByNameIgnoreCase(shopName)) {
            throw new BadRequestException(
                    "Tên shop đã tồn tại"
            );
        }

        Shop shop = shopMapper.toEntity(request);

        shop.setOwner(currentUser);
        shop.setStatus(ShopStatus.PENDING);
        shop.setRating(0.0);
        shop.setTotalFollowers(0);
        shop.setVerified(false);
        shop.setCreatedAt(LocalDateTime.now());

        return shopMapper.toResponse(
                shopRepository.save(shop)
        );
    }

    @Transactional(readOnly = true)
    public ShopResponse getMyShop() {
        return toDetailedResponse(getCurrentSellerShop());
    }

    @Transactional(readOnly = true)
    public Shop getCurrentSellerShop() {
        Long sellerId = SecurityUtils.getCurrentUserId();

        return shopRepository.findByOwner_Id(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bạn chưa đăng ký shop"
                ));
    }

    @Transactional(readOnly = true)
    public Shop getActiveCurrentSellerShop() {
        Shop shop = getCurrentSellerShop();

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new BadRequestException(
                    "Shop chưa được phép hoạt động"
            );
        }

        return shop;
    }

    private Shop getEntityById(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy shop với id: " + id
                ));
    }

    @Transactional
    public ShopResponse approveShop(Long shopId) {
        Shop shop = getEntityById(shopId);

        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new BadRequestException(
                    "Chỉ shop đang chờ xét duyệt mới có thể được duyệt"
            );
        }

        shop.setStatus(ShopStatus.ACTIVE);
        shop.setApprovedAt(LocalDateTime.now());

        User owner = shop.getOwner();
        owner.setRole(Role.SELLER);

        shop.setVerified(false);

        return shopMapper.toResponse(shop);
    }

    @Transactional
    public ShopResponse verifyShop(Long shopId) {
        Shop shop = getEntityById(shopId);

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new BadRequestException(
                    "Chỉ shop đang hoạt động mới có thể được xác minh"
            );
        }

        if (Boolean.TRUE.equals(shop.getVerified())) {
            throw new BadRequestException(
                    "Shop đã được xác minh"
            );
        }

        shop.setVerified(true);

        return shopMapper.toResponse(shop);
    }

    @Transactional(readOnly = true)
    public ShopResponse getById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy shop với id: " + id
                ));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new ResourceNotFoundException(
                    "Shop hiện không hoạt động"
            );
        }

        return toDetailedResponse(shop);
    }

    @Transactional(readOnly = true)
    public Shop getVerifiedActiveCurrentSellerShop() {
        Shop shop = getActiveCurrentSellerShop();

        if (!Boolean.TRUE.equals(shop.getVerified())) {
            throw new BadRequestException(
                    "Shop chưa được xác minh"
            );
        }

        return shop;
    }
    private ShopResponse toDetailedResponse(Shop shop) {
        ShopResponse response =
                shopMapper.toResponse(shop);

        response.setTotalProducts(
                Math.toIntExact(
                        productRepository
                                .countByShop_Id(
                                        shop.getId()
                                )
                )
        );

        response.setTotalOrders(
                Math.toIntExact(
                        shopOrderRepository
                                .countByShop_Id(
                                        shop.getId()
                                )
                )
        );

        response.setTotalReviews(
                Math.toIntExact(
                        reviewRepository
                                .countByProduct_Shop_Id(
                                        shop.getId()
                                )
                )
        );

        Double averageRating =
                reviewRepository
                        .getAverageRatingByShopId(
                                shop.getId()
                        );

        response.setRating(
                averageRating != null
                        ? averageRating
                        : 0.0
        );

        response.setTotalFollowers(
                Math.toIntExact(
                        shopFollowRepository
                                .countByShop_Id(
                                        shop.getId()
                                )
                )
        );

        return response;
    }

    @Transactional
    public ShopResponse updateMyShop(
            UpdateShopRequest request
    ) {
        Shop shop = getCurrentSellerShop();

        String name = request.getName().trim();

        if (shopRepository
                .existsByNameIgnoreCaseAndIdNot(
                        name,
                        shop.getId()
                )) {
            throw new BadRequestException(
                    "Tên shop đã tồn tại"
            );
        }

        shop.setName(name);
        shop.setDescription(
                request.getDescription()
        );
        shop.setPhone(
                request.getPhone()
        );
        shop.setEmail(
                request.getEmail()
        );
        shop.setPickupAddress(
                request.getPickupAddress()
        );

        return toDetailedResponse(shop);
    }

    @Transactional
    public ShopResponse uploadMyLogo(MultipartFile file) {
        Shop shop = getCurrentSellerShop();

        UploadImageResponse uploaded =
                imageStorageService.uploadImage(
                        file,
                        "mini-shop/shops/" + shop.getId() + "/logo"
                );

        String oldPublicId = shop.getLogoPublicId();

        shop.setLogoUrl(uploaded.getUrl());
        shop.setLogoPublicId(uploaded.getPublicId());

        if (oldPublicId != null && !oldPublicId.isBlank()) {
            try {
                imageStorageService.deleteImage(oldPublicId);
            } catch (Exception ignored) {
            }
        }

        return toDetailedResponse(shop);
    }

    @Transactional
    public ShopResponse uploadMyCover(MultipartFile file) {
        Shop shop = getCurrentSellerShop();

        UploadImageResponse uploaded =
                imageStorageService.uploadImage(
                        file,
                        "mini-shop/shops/" + shop.getId() + "/cover"
                );

        String oldPublicId = shop.getCoverPublicId();

        shop.setCoverUrl(uploaded.getUrl());
        shop.setCoverPublicId(uploaded.getPublicId());

        if (oldPublicId != null && !oldPublicId.isBlank()) {
            try {
                imageStorageService.deleteImage(oldPublicId);
            } catch (Exception ignored) {
            }
        }

        return toDetailedResponse(shop);
    }

    @Transactional(readOnly = true)
    public List<ShopCategoryResponse> getShopCategories(
            Long shopId
    ) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy shop với id: " + shopId
                        )
                );

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new ResourceNotFoundException(
                    "Shop hiện không hoạt động"
            );
        }

        return productRepository.findCategoriesByShop(
                shopId,
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE
        );
    }

    @Transactional(readOnly = true)
    public ShopResponse getMyRegisteredShop() {

        Long userId = SecurityUtils.getCurrentUserId();

        Shop shop = shopRepository
                .findByOwner_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bạn chưa đăng ký shop"
                        )
                );

        return toDetailedResponse(shop);
    }

    @Transactional(readOnly = true)
    public Page<ShopResponse> getAdminShops(
            ShopStatus status,
            Pageable pageable
    ) {

        Page<Shop> shops;

        if (status == null) {
            shops = shopRepository.findAll(pageable);
        } else {
            shops = shopRepository.findByStatus(
                    status,
                    pageable
            );
        }

        return shops.map(this::toDetailedResponse);
    }

    @Transactional(readOnly = true)
    public ShopResponse getAdminShopById(
            Long shopId
    ) {

        Shop shop = shopRepository
                .findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy shop"
                        )
                );

        return toDetailedResponse(shop);
    }

    @Transactional
    public ShopResponse rejectShop(
            Long shopId,
            String reason
    ) {

        Shop shop = shopRepository
                .findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy shop"
                        )
                );

        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new IllegalStateException(
                    "Chỉ có thể từ chối shop đang chờ xét duyệt"
            );
        }

        shop.setStatus(ShopStatus.REJECTED);
        shop.setRejectionReason(reason.trim());

        Shop savedShop = shopRepository.save(shop);

        return toDetailedResponse(savedShop);
    }

    @Transactional
    public ShopResponse updateMyRegistration(
            UpdateShopRegistrationRequest request
    ) {

        Long userId =
                SecurityUtils.getCurrentUserId();

        Shop shop = shopRepository
                .findByOwner_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bạn chưa đăng ký shop"
                        )
                );

        if (shop.getStatus() != ShopStatus.REJECTED) {
            throw new IllegalStateException(
                    "Chỉ có thể sửa hồ sơ shop khi yêu cầu đã bị từ chối"
            );
        }

        String normalizedName =
                request.getName().trim();

        if (
                shopRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                normalizedName,
                                shop.getId()
                        )
        ) {
            throw new IllegalStateException(
                    "Tên shop đã được sử dụng"
            );
        }

        shop.setName(normalizedName);
        shop.setDescription(
                request.getDescription().trim()
        );
        shop.setPhone(
                request.getPhone().trim()
        );
        shop.setPickupAddress(
                request.getAddress().trim()
        );

        Shop savedShop =
                shopRepository.save(shop);

        return toDetailedResponse(savedShop);
    }

    @Transactional
    public ShopResponse resubmitMyShop() {

        Long userId =
                SecurityUtils.getCurrentUserId();

        Shop shop = shopRepository
                .findByOwner_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bạn chưa đăng ký shop"
                        )
                );

        if (shop.getStatus() != ShopStatus.REJECTED) {
            throw new IllegalStateException(
                    "Chỉ có thể gửi lại shop đã bị từ chối"
            );
        }

        shop.setStatus(ShopStatus.PENDING);
        shop.setRejectionReason(null);

        Shop savedShop =
                shopRepository.save(shop);

        return toDetailedResponse(savedShop);
    }

    @Transactional(readOnly = true)
    public Page<ShopResponse>
    getPublicShopsByCategory(
            Long categoryId,
            int page,
            int size
    ) {
        categoryService.getEntityById(
                categoryId
        );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        return shopRepository
                .findPublicShopsByCategory(
                        categoryId,
                        ShopStatus.ACTIVE,
                        ProductStatus.ACTIVE,
                        pageable
                )
                .map(this::toDetailedResponse);
    }
}