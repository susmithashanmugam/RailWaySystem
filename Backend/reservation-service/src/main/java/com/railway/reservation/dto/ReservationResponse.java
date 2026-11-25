
package com.railway.reservation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.railway.reservation.entity.ReservationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationResponse {

    private Long reservationId;
    private String pnr;
    private String passengerEmail;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private Long sourceStationId;
    private Long destinationStationId;
    private LocalDate travelDate;
    private String travelClass;
    private BigDecimal totalAmount;
    private ReservationStatus status;
    private LocalDateTime bookedAt;
    private List<PassengerTicketResponse> tickets;
}
