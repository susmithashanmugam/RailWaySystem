
package com.railway.reservation.dto;

import com.railway.reservation.entity.BoardingStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PassengerTicketResponse {

    private Long ticketId;
    private String passengerName;
    private Integer age;
    private String gender;
    private String seatNumber;
    private BoardingStatus boardingStatus;
}
