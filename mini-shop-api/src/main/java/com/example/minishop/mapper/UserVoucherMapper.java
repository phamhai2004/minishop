package com.example.minishop.mapper;

import com.example.minishop.dto.response.UserVoucherResponse;
import com.example.minishop.entity.UserVoucher;
import com.example.minishop.entity.Voucher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserVoucherMapper {

    public UserVoucherResponse toResponse(
            UserVoucher userVoucher
    ) {
        Voucher voucher = userVoucher.getVoucher();
        UserVoucherResponse response = new UserVoucherResponse();

        response.setId(userVoucher.getId());
        response.setVoucherId(voucher.getId());
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

        response.setUsed(userVoucher.getUsed());
        response.setCollectedAt(userVoucher.getCollectedAt());
        response.setUsedAt(userVoucher.getUsedAt());
        fillStatus(response, userVoucher, voucher);

        return response;
    }

    private void fillStatus(
            UserVoucherResponse response,
            UserVoucher userVoucher,
            Voucher voucher
    ) {
        LocalDateTime now =
                LocalDateTime.now();

        if (
                Boolean.TRUE.equals(userVoucher.getUsed())
        ) {
            response.setUsableNow(false);
            response.setStatus("USED");
            return;
        }

        if (
                !Boolean.TRUE.equals(voucher.getActive())
        ) {
            response.setUsableNow(false);
            response.setStatus("INACTIVE");
            return;
        }

        if (
                voucher.getEndDate() != null
                        && now.isAfter(voucher.getEndDate())
        ) {
            response.setUsableNow(false);
            response.setStatus("EXPIRED");
            return;
        }

        if (
                voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())
        ) {
            response.setUsableNow(false);
            response.setStatus("UPCOMING");
            return;
        }

        response.setUsableNow(true);
        response.setStatus("AVAILABLE");
    }
}