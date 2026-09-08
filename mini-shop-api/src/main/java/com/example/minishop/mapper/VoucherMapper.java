package com.example.minishop.mapper;

import com.example.minishop.dto.request.VoucherRequest;
import com.example.minishop.dto.response.VoucherResponse;
import com.example.minishop.entity.Voucher;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapper {

    public Voucher toEntity(VoucherRequest request) {
        Voucher voucher = new Voucher();

        voucher.setCode(request.getCode().trim().toUpperCase());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderAmount(request.getMinOrderAmount());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setQuantity(request.getQuantity());
        voucher.setActive(Boolean.TRUE.equals(request.getActive()));

        return voucher;
    }

    public void updateEntity(Voucher voucher, VoucherRequest request) {
        voucher.setCode(request.getCode().trim().toUpperCase());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderAmount(request.getMinOrderAmount());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setQuantity(request.getQuantity());
        voucher.setActive(Boolean.TRUE.equals(request.getActive()));
    }

    public VoucherResponse toResponse(Voucher voucher) {
        VoucherResponse response = new VoucherResponse();

        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setDiscountType(voucher.getDiscountType());
        response.setDiscountValue(voucher.getDiscountValue());
        response.setMinOrderAmount(voucher.getMinOrderAmount());
        response.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        response.setStartDate(voucher.getStartDate());
        response.setEndDate(voucher.getEndDate());
        response.setQuantity(voucher.getQuantity());
        response.setActive(voucher.getActive());
        response.setScope(voucher.getScope());

        if (voucher.getScope() != null) {
            response.setScopeName(voucher.getScope().getDisplayName());
        }

        if (voucher.getShop() != null) {
            response.setShopId(voucher.getShop().getId());
            response.setShopName(voucher.getShop().getName());
        }

        return response;
    }
}