package com.example.minishop.service;

import com.example.minishop.ai.client.AIClient;
import com.example.minishop.ai.dto.EmbeddingResponse;
import com.example.minishop.ai.qdrant.QdrantService;
import com.example.minishop.dto.response.ProductImageResponse;
import com.example.minishop.dto.response.UploadImageResponse;
import com.example.minishop.entity.Product;
import com.example.minishop.entity.ProductImage;
import com.example.minishop.entity.Shop;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.repository.ProductImageRepository;
import com.example.minishop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductImageService {

    private static final int MAX_IMAGES_PER_PRODUCT = 8;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageStorageService imageStorageService;
    private final ShopService shopService;
    private final AIClient aiClient;
    private final QdrantService qdrantService;

    public ProductImageService(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ImageStorageService imageStorageService,
            ShopService shopService,
            AIClient aiClient,
            QdrantService qdrantService
    ) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.imageStorageService = imageStorageService;
        this.shopService = shopService;
        this.aiClient = aiClient;
        this.qdrantService = qdrantService;
    }

    @Transactional
    public ProductImageResponse setPrimaryImage(Long imageId) {
        Shop shop =
                shopService.getVerifiedActiveCurrentSellerShop();

        ProductImage selected = productImageRepository
                .findByIdAndProduct_Shop_Id(
                        imageId,
                        shop.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy ảnh thuộc sản phẩm của bạn"
                ));

        List<ProductImage> images =
                productImageRepository.findByProduct_Id(
                        selected.getProduct().getId()
                );

        images.forEach(image ->
                image.setPrimaryImage(
                        image.getId().equals(selected.getId())
                )
        );

        return toResponse(selected);
    }

    private static final Logger log =
            LoggerFactory.getLogger(ProductImageService.class);

    @Transactional
    public List<ProductImageResponse> uploadImages(
            Long productId,
            List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException(
                    "Danh sách ảnh không được để trống"
            );
        }

        Shop shop =
                shopService.getVerifiedActiveCurrentSellerShop();

        Product product = productRepository
                .findByIdAndShop_Id(productId, shop.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm thuộc shop của bạn"
                ));

        long currentCount =
                productImageRepository.countByProduct_Id(productId);

        if (currentCount + files.size() > MAX_IMAGES_PER_PRODUCT) {
            throw new BadRequestException(
                    "Mỗi sản phẩm chỉ được có tối đa "
                            + MAX_IMAGES_PER_PRODUCT + " ảnh"
            );
        }

        List<ProductImageResponse> responses =
                new ArrayList<>();

        int displayOrder = (int) currentCount;

        for (MultipartFile file : files) {
            UploadImageResponse uploaded =
                    imageStorageService.uploadImage(
                            file,
                            "mini-shop/products/" + productId
                    );

            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(uploaded.getUrl());
            image.setPublicId(uploaded.getPublicId());
            image.setDisplayOrder(displayOrder++);
            image.setPrimaryImage(currentCount == 0
                    && responses.isEmpty());

            ProductImage saved =
                    productImageRepository.save(image);

            try {

                log.info("=== Generate embedding ===");

                EmbeddingResponse embedding =
                        aiClient.generateEmbedding(file);

                log.info("Embedding length = {}",
                        embedding.getEmbedding().size());

                log.info("=== Upsert to Qdrant ===");

                qdrantService.upsertProduct(
                        product.getId(),
                        embedding.getEmbedding()
                );

                log.info("=== DONE ===");

            } catch (Exception ex) {

                log.error("Cannot generate embedding", ex);

            }

            responses.add(toResponse(saved));
        }

        return responses;
    }

    private ProductImageResponse toResponse(ProductImage image) {
        ProductImageResponse response =
                new ProductImageResponse();

        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setPrimaryImage(image.getPrimaryImage());
        response.setDisplayOrder(image.getDisplayOrder());

        return response;
    }

    @Transactional
    public void deleteImage(Long imageId) {
        Shop shop =
                shopService.getVerifiedActiveCurrentSellerShop();

        ProductImage image = productImageRepository
                .findByIdAndProduct_Shop_Id(
                        imageId,
                        shop.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy ảnh thuộc sản phẩm của bạn"
                ));

        Long productId = image.getProduct().getId();
        boolean wasPrimary =
                Boolean.TRUE.equals(image.getPrimaryImage());

        String publicId = image.getPublicId();

        productImageRepository.delete(image);
        productImageRepository.flush();

        if (wasPrimary) {
            productImageRepository
                    .findFirstByProduct_IdOrderByDisplayOrderAsc(productId)
                    .ifPresent(nextImage ->
                            nextImage.setPrimaryImage(true)
                    );
        }

        try {
            imageStorageService.deleteImage(publicId);
        } catch (Exception ignored) {
        }
    }
}
