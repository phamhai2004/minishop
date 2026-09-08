package com.example.minishop.service;

import com.example.minishop.constant.DiscountType;
import com.example.minishop.constant.VoucherScope;
import com.example.minishop.dto.request.SellerVoucherRequest;
import com.example.minishop.dto.response.VoucherResponse;
import com.example.minishop.entity.Shop;
import com.example.minishop.entity.Voucher;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.VoucherMapper;
import com.example.minishop.repository.VoucherRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SellerVoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;
    private final ShopService shopService;

    public SellerVoucherService(
            VoucherRepository voucherRepository,
            VoucherMapper voucherMapper,
            ShopService shopService
    ) {
        this.voucherRepository = voucherRepository;
        this.voucherMapper = voucherMapper;
        this.shopService = shopService;
    }

    @Transactional
    public VoucherResponse create(SellerVoucherRequest request) {
        Shop shop = shopService.getActiveCurrentSellerShop();

        String code = normalizeCode(request.getCode());

        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Mã voucher đã tồn tại");
        }

        validateRequest(request);

        Voucher voucher = new Voucher();

        voucher.setCode(code);
        voucher.setScope(VoucherScope.SHOP);
        voucher.setShop(shop);
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderAmount(request.getMinOrderAmount());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setQuantity(request.getQuantity());
        voucher.setActive(true);

        return voucherMapper.toResponse(
                voucherRepository.save(voucher)
        );
    }

    @Transactional(readOnly = true)
    public Page<VoucherResponse> getMyVouchers(
            int page,
            int size
    ) {
        Shop shop = shopService.getCurrentSellerShop();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        return voucherRepository
                .findByShop_Id(shop.getId(), pageable)
                .map(voucherMapper::toResponse);
    }

    @Transactional
    public VoucherResponse update(
            Long id,
            SellerVoucherRequest request
    ) {
        Voucher voucher = getMyVoucherForUpdate(id);
        validateRequest(request);
        String code = normalizeCode(request.getCode());

        boolean alreadyCollected =
                voucherRepository
                        .hasBeenCollected(
                                voucher.getId()
                        );

        if (
                alreadyCollected
                        && hasIssuedConditionChanged(
                        voucher,
                        request,
                        code
                )
        ) {
            throw new BadRequestException(
                    "Voucher đã có khách hàng nhận, không thể thay đổi điều kiện đã phát hành"
            );
        }

        if (!voucher.getCode().equalsIgnoreCase(code)
                && voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Mã voucher đã tồn tại");
        }

        voucher.setCode(code);
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderAmount(request.getMinOrderAmount());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setQuantity(request.getQuantity());

        return voucherMapper.toResponse(voucher);
    }

    @Transactional
    public VoucherResponse deactivate(Long id) {
        Voucher voucher = getMyVoucherForUpdate(id);

        if (!Boolean.TRUE.equals(voucher.getActive())) {
            throw new BadRequestException("Voucher đã bị tắt");
        }

        voucher.setActive(false);

        return voucherMapper.toResponse(voucher);
    }

    private Voucher getMyVoucherForUpdate(
            Long id
    ) {
        Shop shop =
                shopService.getActiveCurrentSellerShop();

        return voucherRepository
                .findByIdAndShopIdForUpdate(
                        id,
                        shop.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy voucher thuộc shop của bạn"
                        )
                );
    }

    private void validateRequest(SellerVoucherRequest request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BadRequestException(
                    "Ngày kết thúc phải sau ngày bắt đầu"
            );
        }

        if (request.getDiscountType() == DiscountType.PERCENTAGE
                && request.getDiscountValue()
                .compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException(
                    "Phần trăm giảm không được vượt quá 100"
            );
        }
    }

    private boolean hasIssuedConditionChanged(
            Voucher voucher,
            SellerVoucherRequest request,
            String code
    ) {
        if (
                !voucher.getCode().equalsIgnoreCase(code)
        ) {
            return true;
        }

        if (
                voucher.getDiscountType() != request.getDiscountType()
        ) {
            return true;
        }

        if (
                !sameAmount(voucher.getDiscountValue(), request.getDiscountValue())
        ) {
            return true;
        }

        if (
                !sameAmount(voucher.getMinOrderAmount(), request.getMinOrderAmount())
        ) {
            return true;
        }

        if (
                !sameAmount(voucher.getMaxDiscountAmount(), request.getMaxDiscountAmount())
        ) {
            return true;
        }

        if (
                !java.util.Objects.equals(voucher.getStartDate(), request.getStartDate())
        ) {
            return true;
        }

        return !java.util.Objects.equals(voucher.getEndDate(), request.getEndDate());
    }

    private boolean sameAmount(
            BigDecimal first,
            BigDecimal second
    ) {
        if (
                first == null && second == null
        ) {
            return true;
        }

        if (
                first == null || second == null
        ) {
            return false;
        }

        return first.compareTo(second) == 0;
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}