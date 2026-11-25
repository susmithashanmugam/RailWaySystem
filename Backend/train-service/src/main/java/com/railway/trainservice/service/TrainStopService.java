
package com.railway.trainservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railway.trainservice.dto.TrainStopRequest;
import com.railway.trainservice.dto.TrainStopResponse;
import com.railway.trainservice.entity.Station;
import com.railway.trainservice.entity.Train;
import com.railway.trainservice.entity.TrainStop;
import com.railway.trainservice.exception.DuplicateResourceException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.StationRepository;
import com.railway.trainservice.repository.TrainRepository;
import com.railway.trainservice.repository.TrainStopRepository;

@Service
public class TrainStopService {

    private final TrainStopRepository trainStopRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;

    public TrainStopService(
            TrainStopRepository trainStopRepository,
            TrainRepository trainRepository,
            StationRepository stationRepository) {

        this.trainStopRepository = trainStopRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public TrainStopResponse addStop(Long trainId, TrainStopRequest request) {

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Train not found with ID: " + trainId
                ));

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Station not found with ID: " + request.getStationId()
                ));

        boolean stopNumberExists = trainStopRepository
                .findByTrain_IdOrderByStopNumberAsc(trainId)
                .stream()
                .anyMatch(stop -> stop.getStopNumber()
                        .equals(request.getStopNumber()));

        if (stopNumberExists) {
            throw new DuplicateResourceException(
                    "Stop number already exists for this train: "
                            + request.getStopNumber()
            );
        }

        TrainStop trainStop = new TrainStop();

        trainStop.setTrain(train);
        trainStop.setStation(station);
        trainStop.setStopNumber(request.getStopNumber());
        trainStop.setArrivalTime(request.getArrivalTime());
        trainStop.setDepartureTime(request.getDepartureTime());
        trainStop.setDistanceFromSource(
                request.getDistanceFromSource().doubleValue()
        );

        TrainStop savedStop = trainStopRepository.save(trainStop);

        return convertToResponse(savedStop);
    }

    @Transactional
    public void deleteStop(Long trainId, Long stopId) {

        TrainStop stop = trainStopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Train stop not found with ID: " + stopId
                ));

        if (!stop.getTrain().getId().equals(trainId)) {
            throw new ResourceNotFoundException(
                    "Train stop does not belong to train ID: " + trainId
            );
        }

        trainStopRepository.delete(stop);
    }

    @Transactional(readOnly = true)
    public List<TrainStopResponse> getTrainRoute(Long trainId) {

        if (!trainRepository.existsById(trainId)) {
            throw new ResourceNotFoundException(
                    "Train not found with ID: " + trainId
            );
        }

        return trainStopRepository
                .findByTrain_IdOrderByStopNumberAsc(trainId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private TrainStopResponse convertToResponse(TrainStop stop) {

        Station station = stop.getStation();

        return new TrainStopResponse(
                stop.getId(),
                station.getId(),
                station.getStationCode(),
                station.getStationName(),
                station.getCity(),
                stop.getStopNumber(),
                stop.getArrivalTime(),
                stop.getDepartureTime(),
                stop.getDistanceFromSource()
        );
    }
}
