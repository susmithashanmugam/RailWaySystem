
package com.railway.trainservice.service;

import com.railway.trainservice.dto.TrainFareRequest;
import com.railway.trainservice.dto.TrainFareResponse;
import com.railway.trainservice.entity.Train;
import com.railway.trainservice.entity.TrainFare;
import com.railway.trainservice.exception.DuplicateResourceException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.TrainFareRepository;
import com.railway.trainservice.repository.TrainRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainFareService {

    private final TrainFareRepository trainFareRepository;
    private final TrainRepository trainRepository;

    public TrainFareService(
            TrainFareRepository trainFareRepository,
            TrainRepository trainRepository) {
        this.trainFareRepository = trainFareRepository;
        this.trainRepository = trainRepository;
    }

    @Transactional
    public TrainFareResponse addFare(Long trainId, TrainFareRequest request) {

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Train not found with ID: " + trainId
                ));

        String travelClass = request.getTravelClass()
                .trim()
                .toUpperCase();

        if (trainFareRepository
                .findByTrain_IdAndTravelClass(trainId, travelClass)
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Fare already exists for train "
                            + train.getTrainNumber()
                            + " and class " + travelClass
            );
        }

        TrainFare fare = new TrainFare();

        fare.setTrain(train);
        fare.setTravelClass(travelClass);
        fare.setFareAmount(request.getFareAmount());

        TrainFare savedFare = trainFareRepository.save(fare);

        return convertToResponse(savedFare);
    }

    @Transactional(readOnly = true)
    public List<TrainFareResponse> getTrainFares(Long trainId) {

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Train not found with ID: " + trainId
                ));

        return trainFareRepository.findByTrain_Id(trainId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private TrainFareResponse convertToResponse(TrainFare fare) {

        Train train = fare.getTrain();

        return new TrainFareResponse(
                fare.getId(),
                train.getId(),
                train.getTrainNumber(),
                train.getTrainName(),
                fare.getTravelClass(),
                fare.getFareAmount()
        );
    }
}
