
package com.railway.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResultRequest {

    @NotNull(message = "Payment success status is required")
    private Boolean successful;

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;
}
