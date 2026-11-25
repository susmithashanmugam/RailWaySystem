
package com.railway.reservation.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateReservationRequest {

    @NotNull(message = "Train ID is required")
    @Positive
    private Long trainId;

    @NotNull(message = "Source station ID is required")
    @Positive
    private Long sourceStationId;

    @NotNull(message = "Destination station ID is required")
    @Positive
    private Long destinationStationId;

    @NotNull(message = "Travel date is required")
    @FutureOrPresent(message = "Travel date cannot be in the past")
    private LocalDate travelDate;

    @NotBlank(message = "Travel class is required")
    private String travelClass;

    @NotEmpty(message = "At least one passenger is required")
    @Size(max = 6, message = "Maximum 6 passengers allowed per booking")
    @Valid
    private List<PassengerDetailRequest> passengers;

    public CreateReservationRequest() {
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }

    public Long getSourceStationId() {
        return sourceStationId;
    }

    public void setSourceStationId(Long sourceStationId) {
        this.sourceStationId = sourceStationId;
    }

    public Long getDestinationStationId() {
        return destinationStationId;
    }

    public void setDestinationStationId(Long destinationStationId) {
        this.destinationStationId = destinationStationId;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public List<PassengerDetailRequest> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerDetailRequest> passengers) {
        this.passengers = passengers;
    }
}
