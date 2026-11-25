
package com.railway.trainservice.dto;

import java.math.BigDecimal;

public class TrainFareResponse {

    private Long id;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String travelClass;
    private BigDecimal fareAmount;

    public TrainFareResponse(
            Long id,
            Long trainId,
            String trainNumber,
            String trainName,
            String travelClass,
            BigDecimal fareAmount) {

        this.id = id;
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.travelClass = travelClass;
        this.fareAmount = fareAmount;
    }

    public Long getId() {
        return id;
    }

    public Long getTrainId() {
        return trainId;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public BigDecimal getFareAmount() {
        return fareAmount;
    }
}
