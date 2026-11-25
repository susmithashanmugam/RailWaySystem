
package com.railway.trainservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TrainFareRequest {

    @NotBlank(message = "Travel class is required")
    @Size(max = 20, message = "Travel class cannot exceed 20 characters")
    private String travelClass;

    @NotNull(message = "Fare amount is required")
    @DecimalMin(value = "0.01", message = "Fare must be greater than zero")
    private BigDecimal fareAmount;

    public TrainFareRequest() {
    }

    public String getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public BigDecimal getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(BigDecimal fareAmount) {
        this.fareAmount = fareAmount;
    }
}
