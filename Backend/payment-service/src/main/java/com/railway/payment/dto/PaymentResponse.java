
package com.railway.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.railway.payment.entity.PaymentMethod;
import com.railway.payment.entity.PaymentStatus;

public class PaymentResponse {

    private Long paymentId;
    private Long reservationId;
    private String pnr;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionReference;
    private LocalDateTime createdAt;
    private String paymentLinkUrl;

    public PaymentResponse() {
    }

    public PaymentResponse(
            Long paymentId,
            Long reservationId,
            String pnr,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            String transactionReference,
            LocalDateTime createdAt,
            String paymentLinkUrl) {

        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.pnr = pnr;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionReference = transactionReference;
        this.createdAt = createdAt;
        this.paymentLinkUrl = paymentLinkUrl;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getPnr() {
        return pnr;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPaymentLinkUrl() {
        return paymentLinkUrl;
    }
}
