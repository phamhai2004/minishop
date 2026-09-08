package com.example.minishop.controller.customer;

import com.example.minishop.config.VnPayProperties;
import com.example.minishop.dto.response.PaymentResponse;
import com.example.minishop.dto.response.VnPayIpnResponse;
import com.example.minishop.dto.response.VnPayReturnResponse;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final VnPayProperties vnPayProperties;

    public PaymentController(
            PaymentService paymentService,
            VnPayProperties vnPayProperties
    ) {
        this.paymentService = paymentService;
        this.vnPayProperties = vnPayProperties;
    }

    @PostMapping("/vnpay/{orderId}")
    public ResponseEntity<PaymentResponse>
    createVnPayPayment(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {
        String clientIp = getClientIp(request);

        PaymentResponse response =
                paymentService.createVnPayPayment(
                        orderId,
                        clientIp
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionRef}")
    public ResponseEntity<PaymentResponse>
    getMyPayment(
            @PathVariable String transactionRef
    ) {
        return ResponseEntity.ok(
                paymentService.getMyPayment(
                        transactionRef
                )
        );
    }

    private String getClientIp(
            HttpServletRequest request
    ) {
        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {
            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        String realIp =
                request.getHeader("X-Real-IP");

        if (realIp != null
                && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<VnPayIpnResponse> ipn(
            @RequestParam Map<String, String> params
    ) {
        try {
            paymentService.processIpn(params);

            return ResponseEntity.ok(
                    new VnPayIpnResponse(
                            "00",
                            "Confirm Success"
                    )
            );
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.ok(
                    new VnPayIpnResponse(
                            "01",
                            "Order not found"
                    )
            );
        } catch (BadRequestException exception) {
            return ResponseEntity.ok(
                    new VnPayIpnResponse(
                            "99",
                            exception.getMessage()
                    )
            );
        } catch (Exception exception) {
            return ResponseEntity.ok(
                    new VnPayIpnResponse(
                            "99",
                            "Unknown error"
                    )
            );
        }
    }

    @GetMapping("/vnpay/return")
    public RedirectView paymentReturn(
            @RequestParam Map<String, String> params
    ) {

        paymentService.processIpn(params);

        String transactionRef =
                params.get("vnp_TxnRef");

        if (transactionRef == null
                || transactionRef.isBlank()) {

            throw new BadRequestException(
                    "VNPay không trả về mã giao dịch"
            );
        }

        String frontendUrl =
                vnPayProperties.getFrontendReturnUrl();

        String redirectUrl =
                frontendUrl
                        + "?transactionRef="
                        + URLEncoder.encode(
                        transactionRef,
                        StandardCharsets.UTF_8
                );

        return new RedirectView(redirectUrl);
    }
}