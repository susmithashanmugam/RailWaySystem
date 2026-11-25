package com.railway.trainservice.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.trainservice.dto.TrainStopRequest;
import com.railway.trainservice.dto.TrainStopResponse;
import com.railway.trainservice.service.TrainStopService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trains/{trainId}")
@RequiredArgsConstructor
public class TrainStopController {

    private final TrainStopService trainStopService;

    // Add a station stop to a train
    @PostMapping("/stops")
    public ResponseEntity<TrainStopResponse> addStop(
            @PathVariable Long trainId,
            @Valid @RequestBody TrainStopRequest request) {

        TrainStopResponse response =
                trainStopService.addStop(trainId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get complete train route
    @GetMapping("/route")
    public ResponseEntity<List<TrainStopResponse>> getTrainRoute(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                trainStopService.getTrainRoute(trainId)
        );
    }
   @DeleteMapping("/stops/{stopId}")
public ResponseEntity<Void> deleteStop(
        @PathVariable Long trainId,
        @PathVariable Long stopId) {

    trainStopService.deleteStop(trainId, stopId);

    return ResponseEntity.noContent().build();
}
}
