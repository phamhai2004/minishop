package com.example.minishop.dto.response;

import com.example.minishop.constant.VoucherScope;

import java.math.BigDecimal;

public class VoucherApplicationResult {

    private String voucherCode;
    private VoucherScope scope;
    private Long shopId;
    private BigDecimal discountAmount;

    public VoucherApplicationResult(
            String voucherCode,
            VoucherScope scope,
            Long shopId,
            BigDecimal discountAmount
    ) {
        this.voucherCode = voucherCode;
        this.scope = scope;
        this.shopId = shopId;
        this.discountAmount = discountAmount;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public VoucherScope getScope() {
        return scope;
    }

    public void setScope(VoucherScope scope) {
        this.scope = scope;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}