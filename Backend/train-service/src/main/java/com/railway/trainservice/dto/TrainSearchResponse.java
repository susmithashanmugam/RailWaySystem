
package com.railway.trainservice.dto;

import java.time.LocalTime;

public class TrainSearchResponse {

    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String trainType;

    private Long sourceStationId;
    private String sourceStationName;
    private LocalTime departureTime;

    private Long destinationStationId;
    private String destinationStationName;
    private LocalTime arrivalTime;

    public TrainSearchResponse(
            Long trainId,
            String trainNumber,
            String trainName,
            String trainType,
            Long sourceStationId,
            String sourceStationName,
            LocalTime departureTime,
            Long destinationStationId,
            String destinationStationName,
            LocalTime arrivalTime) {

        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.trainType = trainType;
        this.sourceStationId = sourceStationId;
        this.sourceStationName = sourceStationName;
        this.departureTime = departureTime;
        this.destinationStationId = destinationStationId;
        this.destinationStationName = destinationStationName;
        this.arrivalTime = arrivalTime;
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

    public String getTrainType() {
        return trainType;
    }

    public Long getSourceStationId() {
        return sourceStationId;
    }

    public String getSourceStationName() {
        return sourceStationName;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public Long getDestinationStationId() {
        return destinationStationId;
    }

    public String getDestinationStationName() {
        return destinationStationName;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
}
