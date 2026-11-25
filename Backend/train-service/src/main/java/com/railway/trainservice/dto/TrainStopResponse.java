
package com.railway.trainservice.dto;

import java.time.LocalTime;

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

    public TrainStopResponse(
            Long id,
            Long stationId,
            String stationCode,
            String stationName,
            String city,
            Integer stopNumber,
            LocalTime arrivalTime,
            LocalTime departureTime,
            Double distanceFromSource) {

        this.id = id;
        this.stationId = stationId;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.city = city;
        this.stopNumber = stopNumber;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.distanceFromSource = distanceFromSource;
    }

    public Long getId() {
        return id;
    }

    public Long getStationId() {
        return stationId;
    }

    public String getStationCode() {
        return stationCode;
    }

    public String getStationName() {
        return stationName;
    }

    public String getCity() {
        return city;
    }

    public Integer getStopNumber() {
        return stopNumber;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public Double getDistanceFromSource() {
        return distanceFromSource;
    }
}
