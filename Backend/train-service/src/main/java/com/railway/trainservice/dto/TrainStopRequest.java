
package com.railway.trainservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalTime;

public class TrainStopRequest {

    @NotNull(message = "Station ID is required")
    @Positive(message = "Station ID must be positive")
    private Long stationId;

    @NotNull(message = "Stop number is required")
    @Positive(message = "Stop number must be positive")
    private Integer stopNumber;

    private LocalTime arrivalTime;
    private LocalTime departureTime;

    @NotNull(message = "Distance from source is required")
    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    private BigDecimal distanceFromSource;

    public TrainStopRequest() {
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public Integer getStopNumber() {
        return stopNumber;
    }

    public void setStopNumber(Integer stopNumber) {
        this.stopNumber = stopNumber;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public BigDecimal getDistanceFromSource() {
        return distanceFromSource;
    }

    public void setDistanceFromSource(BigDecimal distanceFromSource) {
        this.distanceFromSource = distanceFromSource;
    }
}
