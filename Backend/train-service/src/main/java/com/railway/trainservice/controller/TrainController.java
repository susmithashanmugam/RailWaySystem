
package com.railway.trainservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.trainservice.dto.CreateTrainRequest;
import com.railway.trainservice.dto.TrainResponse;
import com.railway.trainservice.service.TrainService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trains")
public class TrainController {

    private final TrainService trainService;

    // Constructor injection
    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    // Create a new train
    @PostMapping
    public ResponseEntity<TrainResponse> createTrain(
            @Valid @RequestBody CreateTrainRequest request) {

        TrainResponse response = trainService.createTrain(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all trains
    @GetMapping
    public ResponseEntity<List<TrainResponse>> getAllTrains() {

        return ResponseEntity.ok(trainService.getAllTrains());
    }

    // Get train by train number
    @GetMapping("/{trainNumber}")
    public ResponseEntity<TrainResponse> getTrainByNumber(
            @PathVariable String trainNumber) {

        return ResponseEntity.ok(
                trainService.getTrainByNumber(trainNumber)
        );
    }

    // Get train by ID
    @GetMapping("/id/{trainId}")
    public ResponseEntity<TrainResponse> getTrainById(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                trainService.getTrainById(trainId)
        );
    }
}
