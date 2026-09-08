package com.example.minishop.mapper;

import com.example.minishop.dto.response.CustomerShopOrderResponse;
import com.example.minishop.dto.response.OrderItemResponse;
import com.example.minishop.entity.Address;
import com.example.minishop.entity.Order;
import com.example.minishop.entity.ShopOrder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CustomerShopOrderMapper {

    private final OrderMapper orderMapper;

    public CustomerShopOrderMapper(
            OrderMapper orderMapper
    ) {
        this.orderMapper = orderMapper;
    }

    public CustomerShopOrderResponse toResponse(
            ShopOrder shopOrder
    ) {

        Order order = shopOrder.getOrder();
        CustomerShopOrderResponse response = new CustomerShopOrderResponse();

        response.setId(shopOrder.getId());
        response.setOrderCode(shopOrder.getOrderCode());
        response.setShopId(shopOrder.getShop().getId());
        response.setShopName(shopOrder.getShop().getName());
        response.setStatus(shopOrder.getStatus().name());
        response.setStatusName(shopOrder.getStatus().getDisplayName());
        response.setPaymentMethod(shopOrder.getPaymentMethod().name());
        response.setPaymentStatus(shopOrder.getPaymentStatus().name());
        BigDecimal discountAmount =
                shopOrder.getDiscountAmount() == null
                        ? BigDecimal.ZERO
                        : shopOrder.getDiscountAmount();
        response.setSubtotal(shopOrder.getSubtotal());
        response.setDiscountAmount(shopOrder.getDiscountAmount());
        response.setShippingFee(shopOrder.getShippingFee());
        response.setFinalAmount(shopOrder.getFinalAmount());
        response.setTotalAmount(shopOrder.getSubtotal());
        if (
                discountAmount.compareTo(
                        BigDecimal.ZERO
                ) > 0
        ) {
            response.setVoucherCode(
                    order.getVoucherCode()
            );
        }
        response.setCreatedAt(shopOrder.getCreatedAt());
        response.setDeliveredAt(shopOrder.getDeliveredAt());
        response.setCompletedAt(shopOrder.getCompletedAt());
        response.setPaidAt(shopOrder.getPaidAt());
        response.setCancelReason(shopOrder.getCancelReason());
        response.setCustomerName(order.getUser().getFullName());
        Address address = order.getAddress();
        if (address != null) {

            response.setReceiverName(address.getReceiverName());
            response.setReceiverPhone(address.getPhone());
            response.setShippingAddress(buildShippingAddress(address));
        }

        List<OrderItemResponse> items =
                shopOrder.getItems()
                        .stream()
                        .map(orderMapper::toItemResponse)
                        .toList();

        response.setItems(items);

        return response;
    }

    private String buildShippingAddress(
            Address address
    ) {

        return String.join(
                ", ",
                address.getDetail(),
                address.getWard(),
                address.getProvince()
        );
    }
}