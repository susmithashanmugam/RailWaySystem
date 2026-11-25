
package com.railway.payment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.railway.payment.dto.CreatePaymentRequest;
import com.railway.payment.dto.PaymentResponse;
import com.railway.payment.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // Constructor injection
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create payment
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse makePayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return paymentService.processPayment(
                request,
                jwt.getSubject());
    }

    // Get passenger payment history
    @GetMapping("/my-payments")
    public List<PaymentResponse> getMyPayments(
            @AuthenticationPrincipal Jwt jwt) {

        return paymentService.getMyPayments(jwt.getSubject());
    }
}
