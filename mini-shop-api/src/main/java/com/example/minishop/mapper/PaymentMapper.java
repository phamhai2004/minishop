package com.example.minishop.mapper;

import com.example.minishop.dto.response.PaymentResponse;
import com.example.minishop.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentResponse response =
                new PaymentResponse();

        response.setId(payment.getId());

        if (payment.getOrder() != null) {
            response.setOrderId(
                    payment.getOrder().getId()
            );
        }

        response.setPaymentMethod(
                payment.getPaymentMethod()
        );

        response.setStatus(
                payment.getStatus()
        );

        response.setTransactionRef(
                payment.getTransactionRef()
        );

        response.setAmount(
                payment.getAmount()
        );

        response.setPaymentUrl(
                payment.getPaymentUrl()
        );

        response.setResponseCode(
                payment.getResponseCode()
        );

        response.setResponseMessage(
                payment.getResponseMessage()
        );

        response.setCreatedAt(
                payment.getCreatedAt()
        );

        response.setPaidAt(
                payment.getPaidAt()
        );

        response.setExpiredAt(
                payment.getExpiredAt()
        );

        return response;
    }
}