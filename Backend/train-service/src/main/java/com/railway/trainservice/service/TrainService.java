
package com.railway.trainservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railway.trainservice.dto.CreateTrainRequest;
import com.railway.trainservice.dto.TrainResponse;
import com.railway.trainservice.entity.Train;
import com.railway.trainservice.exception.DuplicateResourceException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.TrainRepository;

@Service
public class TrainService {

    private final TrainRepository trainRepository;

    // Constructor injection
    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    // Create a new train
    @Transactional
    public TrainResponse createTrain(CreateTrainRequest request) {

        String trainNumber = request.getTrainNumber().trim();

        // Check whether train number already exists
        if (trainRepository.existsByTrainNumber(trainNumber)) {
            throw new DuplicateResourceException(
                    "Train already exists with number: " + trainNumber
            );
        }

        // Create train object
        Train train = new Train();

        train.setTrainNumber(trainNumber);
        train.setTrainName(request.getTrainName().trim());
        train.setTrainType(request.getTrainType().trim().toUpperCase());
        train.setActive(true);

        // Save train in database
        Train savedTrain = trainRepository.save(train);

        return convertToResponse(savedTrain);
    }

    // Get all trains
    @Transactional(readOnly = true)
    public List<TrainResponse> getAllTrains() {

        return trainRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Find train using train number
    @Transactional(readOnly = true)
    public TrainResponse getTrainByNumber(String trainNumber) {

        Train train = trainRepository.findByTrainNumber(
                trainNumber.trim()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Train not found with number: " + trainNumber
        ));

        return convertToResponse(train);
    }

    // Find train using ID
    @Transactional(readOnly = true)
    public TrainResponse getTrainById(Long trainId) {

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Train not found with ID: " + trainId
                ));

        return convertToResponse(train);
    }

    // Convert Train entity into TrainResponse DTO
    private TrainResponse convertToResponse(Train train) {

        return new TrainResponse(
                train.getId(),
                train.getTrainNumber(),
                train.getTrainName(),
                train.getTrainType(),
                train.isActive()
        );
    }
}
