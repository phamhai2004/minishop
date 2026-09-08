package com.example.minishop.dto.response;

import com.example.minishop.constant.PaymentMethod;
import com.example.minishop.constant.PaymentTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PaymentTransactionStatus status;
    private String transactionRef;
    private BigDecimal amount;
    private String paymentUrl;
    private String responseCode;
    private String responseMessage;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime expiredAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            PaymentMethod paymentMethod
    ) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentTransactionStatus getStatus() {
        return status;
    }

    public void setStatus(
            PaymentTransactionStatus status
    ) {
        this.status = status;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(
            String transactionRef
    ) {
        this.transactionRef = transactionRef;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(
            String responseCode
    ) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(
            String responseMessage
    ) {
        this.responseMessage = responseMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(
            LocalDateTime expiredAt
    ) {
        this.expiredAt = expiredAt;
    }
}