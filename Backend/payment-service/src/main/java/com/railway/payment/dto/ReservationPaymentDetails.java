
package com.railway.payment.dto;

import java.math.BigDecimal;

public class ReservationPaymentDetails {

    private Long reservationId;
    private String pnr;
    private String passengerEmail;
    private BigDecimal totalAmount;
    private String status;

    // No-argument constructor
    public ReservationPaymentDetails() {
    }

    // Parameterized constructor
    public ReservationPaymentDetails(
            Long reservationId,
            String pnr,
            String passengerEmail,
            BigDecimal totalAmount,
            String status) {

        this.reservationId = reservationId;
        this.pnr = pnr;
        this.passengerEmail = passengerEmail;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters
    public Long getReservationId() {
        return reservationId;
    }

    public String getPnr() {
        return pnr;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }
}
