
package com.railway.reservation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TrainFareResponse {

    private Long id;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String travelClass;
    private BigDecimal fareAmount;
}
