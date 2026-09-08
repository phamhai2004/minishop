package com.example.minishop.service;

import com.example.minishop.entity.FlashSale;
import com.example.minishop.entity.Product;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.repository.FlashSaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class FlashSalePurchaseService {

    private final FlashSaleRepository flashSaleRepository;

    public FlashSalePurchaseService(
            FlashSaleRepository flashSaleRepository
    ) {
        this.flashSaleRepository =
                flashSaleRepository;
    }

    public FlashSale findActiveFlashSale(
            Product product
    ) {
        LocalDateTime now =
                LocalDateTime.now();

        return flashSaleRepository
                .findActiveAvailableFlashSale(
                        product.getId(),
                        now
                )
                .orElse(null);
    }

    public BigDecimal resolvePrice(
            Product product,
            BigDecimal normalPrice
    ) {
        FlashSale flashSale =
                findActiveFlashSale(product);

        if (flashSale == null) {
            return normalPrice;
        }

        return flashSale.getSalePrice();
    }

    public FlashSale consume(
            Long flashSaleId,
            Integer quantity
    ) {

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException(
                    "Số lượng mua phải lớn hơn 0"
            );
        }

        FlashSale flashSale =
                flashSaleRepository
                        .findByIdForUpdate(
                                flashSaleId
                        )
                        .orElseThrow(
                                () ->
                                        new BadRequestException(
                                                "Flash Sale không còn tồn tại"
                                        )
                        );

        LocalDateTime now =
                LocalDateTime.now();

        if (!Boolean.TRUE.equals(
                flashSale.getActive()
        )) {
            throw new BadRequestException(
                    "Flash Sale đã bị tắt"
            );
        }

        if (now.isBefore(
                flashSale.getStartTime()
        )) {
            throw new BadRequestException(
                    "Flash Sale chưa bắt đầu"
            );
        }

        if (now.isAfter(
                flashSale.getEndTime()
        )) {
            throw new BadRequestException(
                    "Flash Sale đã kết thúc"
            );
        }

        int sold =
                flashSale.getSold() != null
                        ? flashSale.getSold()
                        : 0;

        int total =
                flashSale.getQuantity() != null
                        ? flashSale.getQuantity()
                        : 0;

        if (sold + quantity > total) {
            throw new BadRequestException(
                    "Số lượng Flash Sale còn lại không đủ"
            );
        }

        flashSale.setSold(
                sold + quantity
        );

        return flashSale;
    }

    public void restore(
            Long flashSaleId,
            Integer quantity
    ) {
        if (flashSaleId == null) {
            return;
        }

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException(
                    "Số lượng hoàn Flash Sale phải lớn hơn 0"
            );
        }

        FlashSale flashSale =
                flashSaleRepository
                        .findByIdForUpdate(
                                flashSaleId
                        )
                        .orElseThrow(
                                () ->
                                        new BadRequestException(
                                                "Flash Sale không còn tồn tại"
                                        )
                        );

        int sold =
                flashSale.getSold() != null
                        ? flashSale.getSold()
                        : 0;

        int restoredSold =
                Math.max(
                        sold - quantity,
                        0
                );

        flashSale.setSold(
                restoredSold
        );
    }
}