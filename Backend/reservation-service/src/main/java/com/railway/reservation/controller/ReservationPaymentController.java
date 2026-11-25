
package com.railway.reservation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.reservation.dto.ReservationPaymentDetailsResponse;
import com.railway.reservation.service.ReservationPaymentService;

@RestController
@RequestMapping("/api/reservations/payment")
public class ReservationPaymentController {

    private final ReservationPaymentService reservationPaymentService;

    // Constructor injection (replaces Lombok)
    public ReservationPaymentController(
            ReservationPaymentService reservationPaymentService) {
        this.reservationPaymentService = reservationPaymentService;
    }

    @GetMapping("/{reservationId}")
    public ReservationPaymentDetailsResponse getReservationForPayment(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal Jwt jwt) {

        return reservationPaymentService.getReservationForPayment(
                reservationId,
                jwt.getSubject()
        );
    }
}
