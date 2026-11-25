
package com.railway.reservation.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.railway.reservation.dto.PaymentResultRequest;
import com.railway.reservation.service.ReservationPaymentService;

@RestController
@RequestMapping("/internal/reservations")
public class InternalReservationPaymentController {

    private final ReservationPaymentService reservationPaymentService;

    @Value("${internal.api-key}")
    private String internalApiKey;

    // Constructor injection (replaces Lombok)
    public InternalReservationPaymentController(
            ReservationPaymentService reservationPaymentService) {
        this.reservationPaymentService = reservationPaymentService;
    }

    @PostMapping("/{reservationId}/payment-result")
    @ResponseStatus(HttpStatus.OK)
    public void updatePaymentResult(
            @PathVariable Long reservationId,
            @RequestHeader("X-INTERNAL-API-KEY") String apiKey,
            @RequestBody PaymentResultRequest request) {

        boolean validKey = MessageDigest.isEqual(
                internalApiKey.getBytes(StandardCharsets.UTF_8),
                apiKey.getBytes(StandardCharsets.UTF_8)
        );

        if (!validKey) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid internal API key"
            );
        }

        reservationPaymentService.updatePaymentResult(
                reservationId,
                request
        );
    }
}
