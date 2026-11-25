
package com.railway.trainservice.dto;

public class StationResponse {

    private Long id;
    private String stationCode;
    private String stationName;
    private String city;
    private String state;
    private boolean active;

    public StationResponse(
            Long id,
            String stationCode,
            String stationName,
            String city,
            String state,
            boolean active
    ) {
        this.id = id;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.city = city;
        this.state = state;
        this.active = active;
    }

    public Long getId() {
        return id;
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

    public String getState() {
        return state;
    }

    public boolean isActive() {
        return active;
    }
}
