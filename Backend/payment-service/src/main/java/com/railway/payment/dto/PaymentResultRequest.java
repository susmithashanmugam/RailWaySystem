package com.railway.payment.dto;

public class PaymentResultRequest {

    private Boolean successful;
    private String transactionReference;

    public PaymentResultRequest() {
    }

    public PaymentResultRequest(
            Boolean successful,
            String transactionReference) {
        this.successful = successful;
        this.transactionReference = transactionReference;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}