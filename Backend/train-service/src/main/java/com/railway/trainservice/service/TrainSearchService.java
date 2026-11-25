
package com.railway.trainservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railway.trainservice.dto.TrainSearchResponse;
import com.railway.trainservice.entity.Train;
import com.railway.trainservice.entity.TrainStop;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.TrainStopRepository;

@Service
public class TrainSearchService {

    private static final Logger log =
            LoggerFactory.getLogger(TrainSearchService.class);

    private final TrainStopRepository trainStopRepository;

    public TrainSearchService(
            TrainStopRepository trainStopRepository) {
        this.trainStopRepository = trainStopRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainSearchResponse> searchTrains(
            Long sourceStationId,
            Long destinationStationId) {

        log.info(
                "Train search started: source={}, destination={}",
                sourceStationId,
                destinationStationId
        );

        if (sourceStationId == null || destinationStationId == null) {
            throw new IllegalArgumentException(
                    "Source and destination stations are required"
            );
        }

        if (sourceStationId.equals(destinationStationId)) {
            throw new IllegalArgumentException(
                    "Source and destination stations cannot be the same"
            );
        }

        List<Train> trains =
                trainStopRepository.findTrainsBetweenStations(
                        sourceStationId,
                        destinationStationId
                );

        log.info("Found {} matching trains", trains.size());

        return trains.stream()
                .map(train -> buildSearchResponse(
                        train,
                        sourceStationId,
                        destinationStationId
                ))
                .toList();
    }

    private TrainSearchResponse buildSearchResponse(
            Train train,
            Long sourceStationId,
            Long destinationStationId) {

        List<TrainStop> stops =
                trainStopRepository.findByTrain_IdOrderByStopNumberAsc(
                        train.getId()
                );

        TrainStop source = stops.stream()
                .filter(stop -> stop.getStation().getId()
                        .equals(sourceStationId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Source station not found in train route"
                ));

        TrainStop destination = stops.stream()
                .filter(stop -> stop.getStation().getId()
                        .equals(destinationStationId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination station not found in train route"
                ));

        return new TrainSearchResponse(
                train.getId(),
                train.getTrainNumber(),
                train.getTrainName(),
                train.getTrainType(),
                source.getStation().getId(),
                source.getStation().getStationName(),
                source.getDepartureTime(),
                destination.getStation().getId(),
                destination.getStation().getStationName(),
                destination.getArrivalTime()
        );
    }
}
