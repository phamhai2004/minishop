package com.example.minishop.dto.request;

import com.example.minishop.constant.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateOrderRequest {

    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    @Valid
    private List<CreateOrderItemRequest> items;
    private Long addressId;
    private String voucherCode;
    private Boolean autoApplyBestVoucher;
    @NotNull
    private PaymentMethod paymentMethod;

    public List<CreateOrderItemRequest> getItems() {
        return items;
    }
    public void setItems(List<CreateOrderItemRequest> items) {
        this.items = items;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public Boolean getAutoApplyBestVoucher() {
        return autoApplyBestVoucher;
    }

    public void setAutoApplyBestVoucher(Boolean autoApplyBestVoucher) {
        this.autoApplyBestVoucher = autoApplyBestVoucher;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}