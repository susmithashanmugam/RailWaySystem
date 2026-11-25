
package com.railway.reservation.dto;

import java.time.LocalDate;
import java.util.List;

import com.railway.reservation.entity.ReservationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StaffBookingResponse {

    private String pnr;
    private String passengerEmail;
    private String trainNumber;
    private String trainName;
    private LocalDate travelDate;
    private String travelClass;
    private ReservationStatus reservationStatus;
    private List<PassengerTicketResponse> passengers;
}
