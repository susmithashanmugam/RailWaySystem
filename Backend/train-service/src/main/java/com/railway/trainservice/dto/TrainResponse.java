
package com.railway.trainservice.dto;

public class TrainResponse {

    private Long id;
    private String trainNumber;
    private String trainName;
    private String trainType;
    private boolean active;

    // Constructor
    public TrainResponse(
            Long id,
            String trainNumber,
            String trainName,
            String trainType,
            boolean active) {

        this.id = id;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.trainType = trainType;
        this.active = active;
    }

    public Long getId() {
        return id;
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

    public boolean isActive() {
        return active;
    }
}
