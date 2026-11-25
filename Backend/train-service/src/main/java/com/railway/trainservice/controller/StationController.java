package com.railway.trainservice.controller;



import com.railway.trainservice.dto.CreateStationRequest;
import com.railway.trainservice.dto.StationResponse;
import com.railway.trainservice.service.StationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trains/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    // Create a new station
    @PostMapping
    public ResponseEntity<StationResponse> createStation(
            @Valid @RequestBody CreateStationRequest request) {

        StationResponse response = stationService.createStation(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all stations
    @GetMapping
    public ResponseEntity<List<StationResponse>> getAllStations() {

        return ResponseEntity.ok(stationService.getAllStations());
    }

    // Get station by station code
    @GetMapping("/{stationCode}")
    public ResponseEntity<StationResponse> getStationByCode(
            @PathVariable String stationCode) {

        return ResponseEntity.ok(
                stationService.getStationByCode(stationCode)
        );
    }
}
