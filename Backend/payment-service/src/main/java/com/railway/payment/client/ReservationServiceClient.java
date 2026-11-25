
package com.railway.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.railway.payment.dto.PaymentResultRequest;
import com.railway.payment.dto.ReservationPaymentDetails;

// Connects Payment Service with Reservation Service
@FeignClient(
        name = "reservation-service",
        configuration = FeignClientConfig.class
)
public interface ReservationServiceClient {

    // Get reservation details before processing payment
    @GetMapping("/api/reservations/payment/{reservationId}")
    ReservationPaymentDetails getReservationPaymentDetails(
            @PathVariable("reservationId") Long reservationId
    );

    // Send the payment result back to Reservation Service
    @PostMapping("/internal/reservations/{reservationId}/payment-result")
    void updatePaymentResult(
            @PathVariable("reservationId") Long reservationId,
            @RequestHeader("X-INTERNAL-API-KEY") String apiKey,
            @RequestBody PaymentResultRequest request
    );
}
