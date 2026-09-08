package com.example.minishop.dto.response;

import java.math.BigDecimal;

public class BestVoucherResult {

    private String voucherCode;
    private BigDecimal discountAmount;

    public BestVoucherResult(String voucherCode, BigDecimal discountAmount) {
        this.voucherCode = voucherCode;
        this.discountAmount = discountAmount;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}