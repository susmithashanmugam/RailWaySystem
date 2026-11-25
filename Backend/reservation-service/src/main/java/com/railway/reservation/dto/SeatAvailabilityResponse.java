
package com.railway.reservation.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatAvailabilityResponse {

    private Long trainId;
    private LocalDate travelDate;
    private String travelClass;

    private Long sourceStationId;
    private Long destinationStationId;

    private int totalSeats;
    private int bookedSeats;
    private int availableSeats;
}
