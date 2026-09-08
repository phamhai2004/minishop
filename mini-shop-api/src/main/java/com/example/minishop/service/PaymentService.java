package com.example.minishop.service;

import com.example.minishop.config.VnPayProperties;
import com.example.minishop.constant.*;
import com.example.minishop.dto.response.PaymentResponse;
import com.example.minishop.dto.response.VnPayReturnResponse;
import com.example.minishop.entity.Order;
import com.example.minishop.entity.Payment;
import com.example.minishop.event.payload.PaymentPaidOutboxPayload;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.PaymentMapper;
import com.example.minishop.repository.OrderRepository;
import com.example.minishop.repository.PaymentRepository;
import com.example.minishop.security.SecurityUtils;
import com.example.minishop.util.VnPayUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final DateTimeFormatter VNPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final EnumSet<PaymentTransactionStatus>
            ACTIVE_PAYMENT_STATUSES =
            EnumSet.of(
                    PaymentTransactionStatus.CREATED,
                    PaymentTransactionStatus.PENDING
            );

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final VnPayProperties vnPayProperties;
    private final OutboxService outboxService;

    public PaymentService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper,
            VnPayProperties vnPayProperties,
            OutboxService outboxService
    ) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.vnPayProperties = vnPayProperties;
        this.outboxService = outboxService;
    }

    @Transactional
    public PaymentResponse createVnPayPayment(
            Long orderId,
            String clientIp
    ) {
        Long currentUserId =
                SecurityUtils.getCurrentUserId();

        Order order = orderRepository
                .findByIdAndUser_Id(
                        orderId,
                        currentUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy đơn hàng"
                        )
                );

        validateOrderForPayment(order);

        expireOldActivePayments(orderId);

        Payment payment = new Payment();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusMinutes(
                vnPayProperties.getExpireMinutes()
        );

        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.VNPAY);
        payment.setStatus(
                PaymentTransactionStatus.CREATED
        );

        payment.setTransactionRef(
                generateTransactionRef(order.getId())
        );

        payment.setAmount(order.getFinalAmount());
        payment.setExpiredAt(expiredAt);

        Payment savedPayment =
                paymentRepository.save(payment);

        String paymentUrl = buildPaymentUrl(
                savedPayment,
                clientIp,
                now,
                expiredAt
        );

        savedPayment.setPaymentUrl(paymentUrl);
        savedPayment.setStatus(
                PaymentTransactionStatus.PENDING
        );

        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getMyPayment(
            String transactionRef
    ) {
        Long currentUserId =
                SecurityUtils.getCurrentUserId();

        Payment payment = paymentRepository
                .findByTransactionRefAndOrder_User_Id(
                        transactionRef,
                        currentUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy giao dịch thanh toán"
                        )
                );

        return paymentMapper.toResponse(payment);
    }

    private void validateOrderForPayment(Order order) {
        if (order.getStatus()
                == OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Đơn hàng đã bị hủy"
            );
        }

        if (order.getStatus()
                == OrderStatus.COMPLETED) {
            throw new BadRequestException(
                    "Đơn hàng đã hoàn thành"
            );
        }

        if (order.getPaymentStatus()
                == PaymentStatus.PAID) {
            throw new BadRequestException(
                    "Đơn hàng đã được thanh toán"
            );
        }

        if (order.getPaymentMethod()
                != PaymentMethod.VNPAY) {
            throw new BadRequestException(
                    "Đơn hàng không sử dụng phương thức VNPay"
            );
        }

        if (order.getFinalAmount() == null
                || order.getFinalAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Số tiền thanh toán không hợp lệ"
            );
        }
    }

    private void expireOldActivePayments(
            Long orderId
    ) {
        paymentRepository
                .findTopByOrder_IdOrderByCreatedAtDesc(
                        orderId
                )
                .filter(payment ->
                        ACTIVE_PAYMENT_STATUSES.contains(
                                payment.getStatus()
                        )
                )
                .ifPresent(payment -> {
                    payment.setStatus(
                            PaymentTransactionStatus.EXPIRED
                    );

                    payment.setResponseMessage(
                            "Giao dịch cũ đã được thay thế"
                    );
                });
    }

    private String buildPaymentUrl(
            Payment payment,
            String clientIp,
            LocalDateTime createdAt,
            LocalDateTime expiredAt
    ) {
        long vnPayAmount = payment
                .getAmount()
                .movePointRight(2)
                .longValueExact();

        Map<String, String> params =
                new LinkedHashMap<>();

        params.put(
                "vnp_Version",
                vnPayProperties.getVersion()
        );

        params.put(
                "vnp_Command",
                vnPayProperties.getCommand()
        );

        params.put(
                "vnp_TmnCode",
                vnPayProperties.getTmnCode()
        );

        params.put(
                "vnp_Amount",
                String.valueOf(vnPayAmount)
        );

        params.put(
                "vnp_CurrCode",
                vnPayProperties.getCurrCode()
        );

        params.put(
                "vnp_TxnRef",
                payment.getTransactionRef()
        );

        params.put(
                "vnp_OrderInfo",
                "Thanh toan don hang "
                        + payment.getOrder().getId()
        );

        params.put(
                "vnp_OrderType",
                "other"
        );

        params.put(
                "vnp_Locale",
                vnPayProperties.getLocale()
        );

        params.put(
                "vnp_ReturnUrl",
                vnPayProperties.getReturnUrl()
        );

        params.put(
                "vnp_IpAddr",
                normalizeClientIp(clientIp)
        );

        params.put(
                "vnp_CreateDate",
                createdAt.format(VNPAY_DATE_FORMAT)
        );

        params.put(
                "vnp_ExpireDate",
                expiredAt.format(VNPAY_DATE_FORMAT)
        );

        return VnPayUtil.buildPaymentUrl(
                vnPayProperties.getPayUrl(),
                vnPayProperties.getHashSecret(),
                params
        );
    }

    private String generateTransactionRef(
            Long orderId
    ) {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();

        return "ORDER"
                + orderId
                + "-"
                + randomPart;
    }

    private String normalizeClientIp(
            String clientIp
    ) {
        if (clientIp == null
                || clientIp.isBlank()
                || "0:0:0:0:0:0:0:1".equals(clientIp)
                || "::1".equals(clientIp)) {
            return "127.0.0.1";
        }

        return clientIp;
    }

    @Transactional
    public void processIpn(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new BadRequestException(
                    "Dữ liệu IPN VNPay không được để trống"
            );
        }

        String txnRef = params.get("vnp_TxnRef");

        if (txnRef == null || txnRef.isBlank()) {
            throw new BadRequestException(
                    "Thiếu mã giao dịch VNPay"
            );
        }

        boolean validSignature =
                VnPayUtil.verifySignature(
                        params,
                        vnPayProperties.getHashSecret()
                );

        if (!validSignature) {
            throw new BadRequestException(
                    "Checksum VNPay không hợp lệ"
            );
        }

        String tmnCode = params.get("vnp_TmnCode");

        if (!vnPayProperties.getTmnCode().equals(tmnCode)) {
            throw new BadRequestException(
                    "VNPay TmnCode không hợp lệ"
            );
        }

        Payment payment = paymentRepository
                .findByTransactionRefForUpdate(txnRef)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy giao dịch"
                        )
                );

        if (payment.getStatus()
                == PaymentTransactionStatus.SUCCESS) {

            return;
        }

        if (!ACTIVE_PAYMENT_STATUSES.contains(
                payment.getStatus()
        )) {

            payment.setResponseMessage(
                    "Giao dịch không còn ở trạng thái có thể xử lý"
            );

            return;
        }

        if (payment.getPaymentMethod()
                != PaymentMethod.VNPAY) {

            throw new BadRequestException(
                    "Giao dịch không sử dụng VNPay"
            );
        }

        String vnpAmount = params.get("vnp_Amount");

        if (vnpAmount == null || vnpAmount.isBlank()) {
            throw new BadRequestException(
                    "Thiếu số tiền thanh toán VNPay"
            );
        }

        BigDecimal amountFromVnPay;

        try {
            amountFromVnPay =
                    new BigDecimal(vnpAmount)
                            .movePointLeft(2);
        } catch (NumberFormatException exception) {

            throw new BadRequestException(
                    "Số tiền VNPay không hợp lệ"
            );
        }

        if (payment.getAmount() == null
                || payment.getAmount()
                .compareTo(amountFromVnPay) != 0) {

            throw new BadRequestException(
                    "Số tiền thanh toán không khớp"
            );
        }

        String responseCode =
                params.get("vnp_ResponseCode");

        String transactionStatus =
                params.get("vnp_TransactionStatus");

        payment.setProviderTransactionId(
                params.get("vnp_TransactionNo")
        );

        payment.setResponseCode(responseCode);

        if ("00".equals(responseCode)
                && "00".equals(transactionStatus)) {

            Long orderId =
                    payment.getOrder().getId();

            Order order = orderRepository
                    .findByIdForUpdate(orderId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Không tìm thấy đơn hàng"
                            )
                    );

            if (order.getStatus()
                    == OrderStatus.CANCELLED) {

                payment.setStatus(
                        PaymentTransactionStatus.FAILED
                );

                payment.setResponseMessage(
                        "Đơn hàng đã bị hủy"
                );

                return;
            }

            if (order.getPaymentStatus()
                    == PaymentStatus.EXPIRED) {

                payment.setStatus(
                        PaymentTransactionStatus.FAILED
                );

                payment.setResponseMessage(
                        "Đơn hàng đã hết hạn thanh toán"
                );

                return;
            }

            if (order.getPaymentStatus()
                    == PaymentStatus.PAID) {

                payment.setStatus(
                        PaymentTransactionStatus.FAILED
                );

                payment.setResponseMessage(
                        "Đơn hàng đã được thanh toán bằng giao dịch khác"
                );

                return;
            }

            LocalDateTime paidAt =
                    LocalDateTime.now();

            payment.setStatus(
                    PaymentTransactionStatus.SUCCESS
            );

            payment.setPaidAt(paidAt);

            payment.setResponseMessage(
                    "Thanh toán thành công"
            );

            order.setPaymentStatus(
                    PaymentStatus.PAID
            );

            order.setPaymentTransactionId(
                    payment.getProviderTransactionId()
            );

            order.setPaidAt(paidAt);

            outboxService.addEvent(
                    OutboxEventType.PAYMENT_PAID,
                    "PAYMENT",
                    payment.getId(),
                    new PaymentPaidOutboxPayload(
                            payment.getId()
                    )
            );

            return;
        }

        payment.setStatus(
                PaymentTransactionStatus.FAILED
        );

        payment.setResponseMessage(
                buildVnPayFailureMessage(
                        responseCode,
                        transactionStatus
                )
        );
    }

    private String buildVnPayFailureMessage(
            String responseCode,
            String transactionStatus
    ) {

        if (responseCode == null
                || responseCode.isBlank()) {

            return "VNPay không trả về mã kết quả";
        }

        if (transactionStatus == null
                || transactionStatus.isBlank()) {

            return "VNPay không trả về trạng thái giao dịch";
        }

        return "Thanh toán VNPay thất bại"
                + " (ResponseCode="
                + responseCode
                + ", TransactionStatus="
                + transactionStatus
                + ")";
    }

    @Transactional(readOnly = true)
    public VnPayReturnResponse getPaymentResult(
            Map<String, String> params
    ) {
        if (params == null || params.isEmpty()) {
            throw new BadRequestException(
                    "Dữ liệu return không được để trống"
            );
        }

        if (!VnPayUtil.verifySignature(
                params,
                vnPayProperties.getHashSecret()
        )) {
            throw new BadRequestException(
                    "Checksum không hợp lệ"
            );
        }

        String txnRef = params.get("vnp_TxnRef");

        if (txnRef == null || txnRef.isBlank()) {
            throw new BadRequestException(
                    "Thiếu mã giao dịch VNPay"
            );
        }

        Payment payment = paymentRepository
                .findByTransactionRef(txnRef)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy giao dịch"
                        )
                );

        VnPayReturnResponse response =
                new VnPayReturnResponse();

        response.setTransactionRef(
                payment.getTransactionRef()
        );

        if (payment.getStatus()
                == PaymentTransactionStatus.SUCCESS) {

            response.setSuccess(true);
            response.setMessage(
                    "Thanh toán thành công"
            );

            return response;
        }

        if (payment.getStatus()
                == PaymentTransactionStatus.CREATED
                || payment.getStatus()
                == PaymentTransactionStatus.PENDING) {

            response.setSuccess(false);
            response.setMessage(
                    "Giao dịch đang được xử lý"
            );

            return response;
        }

        response.setSuccess(false);

        response.setMessage(
                payment.getResponseMessage() != null
                        ? payment.getResponseMessage()
                        : "Thanh toán không thành công"
        );

        return response;
    }
}