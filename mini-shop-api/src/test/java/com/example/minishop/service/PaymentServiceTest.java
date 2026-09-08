package com.example.minishop.service;

import com.example.minishop.config.VnPayProperties;
import com.example.minishop.constant.OrderStatus;
import com.example.minishop.constant.OutboxEventType;
import com.example.minishop.constant.PaymentStatus;
import com.example.minishop.constant.PaymentTransactionStatus;
import com.example.minishop.entity.Order;
import com.example.minishop.entity.Payment;
import com.example.minishop.event.payload.PaymentPaidOutboxPayload;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.PaymentMapper;
import com.example.minishop.repository.OrderRepository;
import com.example.minishop.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private VnPayProperties vnPayProperties;

    @Mock
    private OutboxService outboxService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                orderRepository,
                paymentRepository,
                paymentMapper,
                vnPayProperties,
                outboxService
        );
    }

    @Test
    void processIpn_shouldMarkPaymentAndOrderAsPaidWhenIpnIsSuccessful() {
        Order order = createPendingOrder();

        Payment payment = createPendingPayment(order);

        when(paymentRepository.findByTransactionRefForUpdate("TXN-001"))
                .thenReturn(Optional.of(payment));

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        Map<String, String> params =
                createSuccessfulIpnParams();

        paymentService.processIpn(params);

        assertThat(payment.getStatus())
                .isEqualTo(PaymentTransactionStatus.SUCCESS);

        assertThat(payment.getProviderTransactionId())
                .isEqualTo("VNPAY-999");

        assertThat(payment.getResponseCode())
                .isEqualTo("00");

        assertThat(payment.getPaidAt())
                .isNotNull();

        assertThat(payment.getResponseMessage())
                .isEqualTo("Thanh toán thành công");

        assertThat(order.getPaymentStatus())
                .isEqualTo(PaymentStatus.PAID);

        assertThat(order.getPaidAt())
                .isNotNull();

        assertThat(order.getPaidAt())
                .isEqualTo(payment.getPaidAt());

        verify(outboxService).addEvent(
                eq(OutboxEventType.PAYMENT_PAID),
                eq("PAYMENT"),
                eq(10L),
                any(PaymentPaidOutboxPayload.class)
        );
    }

    @Test
    void processIpn_shouldFailPaymentWhenOrderIsCancelled() {
        Order order = createPendingOrder();

        order.setStatus(OrderStatus.CANCELLED);

        Payment payment = createPendingPayment(order);

        when(paymentRepository.findByTransactionRefForUpdate("TXN-001"))
                .thenReturn(Optional.of(payment));

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        paymentService.processIpn(
                createSuccessfulIpnParams()
        );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentTransactionStatus.FAILED);

        assertThat(payment.getResponseMessage())
                .isEqualTo(
                        "Đơn hàng đã hết hạn hoặc đã bị hủy"
                );

        assertThat(order.getPaymentStatus())
                .isEqualTo(PaymentStatus.PENDING);

        verifyNoInteractions(outboxService);
    }

    @Test
    void processIpn_shouldFailPaymentWhenOrderPaymentIsExpired() {
        Order order = createPendingOrder();

        order.setPaymentStatus(PaymentStatus.EXPIRED);

        Payment payment = createPendingPayment(order);

        when(paymentRepository.findByTransactionRefForUpdate("TXN-001"))
                .thenReturn(Optional.of(payment));

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        paymentService.processIpn(
                createSuccessfulIpnParams()
        );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentTransactionStatus.FAILED);

        assertThat(payment.getResponseMessage())
                .isEqualTo(
                        "Đơn hàng đã hết hạn hoặc đã bị hủy"
                );

        assertThat(order.getPaymentStatus())
                .isEqualTo(PaymentStatus.EXPIRED);

        verifyNoInteractions(outboxService);
    }

    @Test
    void processIpn_shouldFailPaymentWhenOrderWasPaidByAnotherTransaction() {
        Order order = createPendingOrder();

        order.setPaymentStatus(PaymentStatus.PAID);

        Payment payment = createPendingPayment(order);

        when(paymentRepository.findByTransactionRefForUpdate("TXN-001"))
                .thenReturn(Optional.of(payment));

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        paymentService.processIpn(
                createSuccessfulIpnParams()
        );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentTransactionStatus.FAILED);

        assertThat(payment.getResponseMessage())
                .isEqualTo(
                        "Đơn hàng đã được thanh toán bằng giao dịch khác"
                );

        verifyNoInteractions(outboxService);
    }

    @Test
    void processIpn_shouldIgnoreDuplicateCallbackForSuccessfulPayment() {
        Order order = createPendingOrder();

        Payment payment = createPendingPayment(order);

        payment.setStatus(
                PaymentTransactionStatus.SUCCESS
        );

        when(paymentRepository.findByTransactionRefForUpdate("TXN-001"))
                .thenReturn(Optional.of(payment));

        paymentService.processIpn(
                createSuccessfulIpnParams()
        );

        assertThat(payment.getStatus())
                .isEqualTo(PaymentTransactionStatus.SUCCESS);

        verify(orderRepository, never())
                .findByIdForUpdate(anyLong());

        verifyNoInteractions(outboxService);
    }

    @Test
    void processIpn_shouldMarkPaymentFailedWhenVnPayResponseFails() {
        Order order = createPendingOrder();

        Payment payment = createPendingPayment(order);

        when(paymentRepository.findByTransactionRefForUpdate("TXN-001"))
                .thenReturn(Optional.of(payment));

        Map<String, String> params = new HashMap<>();

        params.put("vnp_TxnRef", "TXN-001");
        params.put("vnp_ResponseCode", "24");
        params.put("vnp_TransactionStatus", "02");
        params.put("vnp_TransactionNo", "VNPAY-999");

        paymentService.processIpn(params);

        assertThat(payment.getStatus())
                .isEqualTo(PaymentTransactionStatus.FAILED);

        assertThat(payment.getProviderTransactionId())
                .isEqualTo("VNPAY-999");

        assertThat(payment.getResponseCode())
                .isEqualTo("24");

        assertThat(payment.getResponseMessage())
                .isEqualTo("Thanh toán thất bại");

        verify(orderRepository, never())
                .findByIdForUpdate(anyLong());

        verifyNoInteractions(outboxService);
    }

    @Test
    void processIpn_shouldThrowWhenPaymentDoesNotExist() {
        when(paymentRepository.findByTransactionRefForUpdate("UNKNOWN"))
                .thenReturn(Optional.empty());

        Map<String, String> params = new HashMap<>();

        params.put("vnp_TxnRef", "UNKNOWN");

        assertThatThrownBy(
                () -> paymentService.processIpn(params)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Không tìm thấy giao dịch");

        verifyNoInteractions(
                orderRepository,
                outboxService
        );
    }

    @Test
    void processIpn_shouldThrowWhenOrderDoesNotExist() {
        Order order = createPendingOrder();

        Payment payment = createPendingPayment(order);

        when(paymentRepository.findByTransactionRefForUpdate("TXN-001"))
                .thenReturn(Optional.of(payment));

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> paymentService.processIpn(
                        createSuccessfulIpnParams()
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Không tìm thấy đơn hàng");

        verifyNoInteractions(outboxService);
    }

    private Order createPendingOrder() {
        Order order = new Order();

        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        return order;
    }

    private Payment createPendingPayment(
            Order order
    ) {
        Payment payment = new Payment();

        payment.setId(10L);
        payment.setOrder(order);
        payment.setTransactionRef("TXN-001");
        payment.setStatus(
                PaymentTransactionStatus.PENDING
        );

        return payment;
    }

    private Map<String, String>
    createSuccessfulIpnParams() {

        Map<String, String> params = new HashMap<>();

        params.put("vnp_TxnRef", "TXN-001");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TransactionNo", "VNPAY-999");

        return params;
    }
}