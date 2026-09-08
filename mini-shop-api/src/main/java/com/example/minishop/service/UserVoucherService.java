package com.example.minishop.service;

import com.example.minishop.dto.response.BestVoucherResult;
import com.example.minishop.dto.response.UserVoucherResponse;
import com.example.minishop.entity.Order;
import com.example.minishop.entity.User;
import com.example.minishop.entity.UserVoucher;
import com.example.minishop.entity.Voucher;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.mapper.UserVoucherMapper;
import com.example.minishop.repository.UserVoucherRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserVoucherService {

    private final UserVoucherRepository userVoucherRepository;
    private final VoucherService voucherService;
    private final UserVoucherMapper userVoucherMapper;

    public UserVoucherService(
            UserVoucherRepository userVoucherRepository,
            VoucherService voucherService,
            UserVoucherMapper userVoucherMapper
    ) {
        this.userVoucherRepository = userVoucherRepository;
        this.voucherService = voucherService;
        this.userVoucherMapper = userVoucherMapper;
    }

    @Transactional
    public UserVoucherResponse collect(
            String code
    ) {
        User user = SecurityUtils.getCurrentUser().getUser();
        Voucher existingVoucher = voucherService.getEntityByCode(code);

        if (
                userVoucherRepository
                        .existsByUser_IdAndVoucher_Id(
                                user.getId(),
                                existingVoucher.getId()
                        )
        ) {
            throw new BadRequestException(
                    "Bạn đã lưu voucher này rồi"
            );
        }

        Voucher voucher = voucherService.claimOne(code
        );

        UserVoucher userVoucher = new UserVoucher();

        userVoucher.setUser(user);
        userVoucher.setVoucher(voucher);
        userVoucher.setUsed(false);
        userVoucher.setCollectedAt(
                LocalDateTime.now()
        );

        return userVoucherMapper
                .toResponse(
                        userVoucherRepository
                                .save(
                                        userVoucher
                                )
                );
    }

    @Transactional(readOnly = true)
    public List<UserVoucherResponse> getMyVouchers() {
        Long userId = SecurityUtils.getCurrentUserId();

        return userVoucherRepository.findByUser_IdOrderByCollectedAtDesc(userId)
                .stream()
                .map(userVoucherMapper::toResponse)
                .toList();
    }

    @Transactional
    public void markUsed(
            Long userId,
            String voucherCode
    ) {
        if (
                voucherCode == null || voucherCode.isBlank()
        ) {
            return;
        }

        UserVoucher userVoucher =
                userVoucherRepository
                        .findByUserIdAndVoucherCodeForUpdate(userId, voucherCode)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Bạn chưa lưu voucher này"
                                )
                        );

        if (
                Boolean.TRUE.equals(userVoucher.getUsed())
        ) {
            throw new BadRequestException(
                    "Voucher này đã được sử dụng"
            );
        }

        userVoucher.setUsed(true);
        userVoucher.setUsedAt(
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public List<UserVoucherResponse>
    getAvailableVouchers() {

        Long userId = SecurityUtils.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        return userVoucherRepository
                .findByUser_IdAndUsedFalseOrderByCollectedAtDesc(userId)
                .stream()
                .filter(userVoucher -> {
                    Voucher voucher = userVoucher.getVoucher();

                    return Boolean.TRUE.equals(voucher.getActive())
                            &&
                            !now.isBefore(voucher.getStartDate())
                            &&
                            !now.isAfter(voucher.getEndDate());
                })
                .map(userVoucherMapper::toResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public BestVoucherResult findBestVoucher(
            Long userId,
            Order order
    ) {
        List<UserVoucher> vouchers = userVoucherRepository
                        .findByUser_IdAndUsedFalseOrderByCollectedAtDesc(userId);

        String bestCode = null;
        BigDecimal bestDiscount =
                BigDecimal.ZERO;

        for (
                UserVoucher userVoucher :
                vouchers
        ) {
            String code = userVoucher.getVoucher().getCode();

            try {
                BigDecimal discount =
                        voucherService.previewDiscountForOrder(code, order);

                if (discount.compareTo(bestDiscount) > 0
                ) {
                    bestDiscount = discount;
                    bestCode = code;
                }

            } catch (
                    BadRequestException ignored
            ) {
            }
        }

        return new BestVoucherResult(
                bestCode,
                bestDiscount
        );
    }

    @Transactional(readOnly = true)
    public List<UserVoucherResponse>
    getUsedVouchers() {

        Long userId = SecurityUtils.getCurrentUserId();

        return userVoucherRepository
                .findByUser_IdAndUsedTrueOrderByUsedAtDesc(userId)
                .stream()
                .map(userVoucherMapper::toResponse)
                .toList();
    }
}
