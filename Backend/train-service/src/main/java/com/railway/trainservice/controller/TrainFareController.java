package com.railway.trainservice.controller;


import com.railway.trainservice.dto.TrainFareRequest;
import com.railway.trainservice.dto.TrainFareResponse;
import com.railway.trainservice.service.TrainFareService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trains/{trainId}/fares")
@RequiredArgsConstructor
public class TrainFareController {

    private final TrainFareService trainFareService;

    // Add fare for a train
    @PostMapping
    public ResponseEntity<TrainFareResponse> addFare(
            @PathVariable Long trainId,
            @Valid @RequestBody TrainFareRequest request) {

        TrainFareResponse response =
                trainFareService.addFare(trainId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all fares for a train
    @GetMapping
    public ResponseEntity<List<TrainFareResponse>> getTrainFares(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                trainFareService.getTrainFares(trainId)
        );
    }
}

