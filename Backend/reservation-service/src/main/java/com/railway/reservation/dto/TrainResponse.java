
package com.railway.reservation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class TrainResponse {

    private Long id;
    private String trainNumber;
    private String trainName;
    private String trainType;
    private boolean active;
}
