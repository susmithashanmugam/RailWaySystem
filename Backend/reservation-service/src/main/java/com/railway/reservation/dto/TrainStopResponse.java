
package com.railway.reservation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class TrainStopResponse {

    private Long id;
    private Long stationId;
    private String stationCode;
    private String stationName;
    private String city;
    private Integer stopNumber;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private Double distanceFromSource;
}
