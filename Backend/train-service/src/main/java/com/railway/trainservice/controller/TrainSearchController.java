
package com.railway.trainservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.railway.trainservice.dto.TrainSearchResponse;
import com.railway.trainservice.service.TrainSearchService;

@RestController
@RequestMapping("/api/trains")
public class TrainSearchController {

    private final TrainSearchService trainSearchService;

    public TrainSearchController(
            TrainSearchService trainSearchService) {
        this.trainSearchService = trainSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<TrainSearchResponse>> searchTrains(
            @RequestParam Long sourceStationId,
            @RequestParam Long destinationStationId) {

        return ResponseEntity.ok(
                trainSearchService.searchTrains(
                        sourceStationId,
                        destinationStationId
                )
        );
    }
}
