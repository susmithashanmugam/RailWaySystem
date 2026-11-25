package com.railway.reservation.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationPaymentDetailsResponse {

    private Long reservationId;
    private String pnr;
    private String passengerEmail;
    private BigDecimal totalAmount;
    private String status;
}