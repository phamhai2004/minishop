package com.example.minishop.service;

import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.dto.response.PublicFlashSaleResponse;
import com.example.minishop.mapper.FlashSaleMapper;
import com.example.minishop.repository.FlashSaleRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PublicFlashSaleService {

    private final FlashSaleRepository
            flashSaleRepository;

    private final FlashSaleMapper
            flashSaleMapper;

    public PublicFlashSaleService(
            FlashSaleRepository flashSaleRepository,
            FlashSaleMapper flashSaleMapper
    ) {
        this.flashSaleRepository =
                flashSaleRepository;

        this.flashSaleMapper =
                flashSaleMapper;
    }

    @Transactional(readOnly = true)
    public Page<PublicFlashSaleResponse>
    getActiveFlashSales(
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        return flashSaleRepository
                .findPublicActiveFlashSales(
                        LocalDateTime.now(),
                        ProductStatus.ACTIVE,
                        ShopStatus.ACTIVE,
                        pageable
                )
                .map(
                        flashSaleMapper
                                ::toPublicResponse
                );
    }
}