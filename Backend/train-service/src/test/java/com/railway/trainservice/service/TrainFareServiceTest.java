
package com.railway.trainservice.service;

import com.railway.trainservice.dto.TrainFareRequest;
import com.railway.trainservice.dto.TrainFareResponse;
import com.railway.trainservice.entity.Train;
import com.railway.trainservice.entity.TrainFare;
import com.railway.trainservice.exception.DuplicateResourceException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.TrainFareRepository;
import com.railway.trainservice.repository.TrainRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainFareServiceTest {

    @Mock
    private TrainFareRepository trainFareRepository;

    @Mock
    private TrainRepository trainRepository;

    @InjectMocks
    private TrainFareService trainFareService;

    private Train createTrain() {
        Train train = new Train();
        train.setId(1L);
        train.setTrainNumber("12675");
        train.setTrainName("Chennai Express");
        train.setTrainType("EXPRESS");
        return train;
    }

    private TrainFare createFare(Train train) {
        TrainFare fare = new TrainFare();
        fare.setId(10L);
        fare.setTrain(train);
        fare.setTravelClass("AC");
        fare.setFareAmount(new BigDecimal("750.00"));
        return fare;
    }

    private TrainFareRequest createRequest() {
        TrainFareRequest request = new TrainFareRequest();
        request.setTravelClass(" ac ");
        request.setFareAmount(new BigDecimal("750.00"));
        return request;
    }

    @Test
    void addFare_Success() {
        Train train = createTrain();
        TrainFare savedFare = createFare(train);

        when(trainRepository.findById(1L))
                .thenReturn(Optional.of(train));

        when(trainFareRepository.findByTrain_IdAndTravelClass(1L, "AC"))
                .thenReturn(Optional.empty());

        when(trainFareRepository.save(any(TrainFare.class)))
                .thenReturn(savedFare);

        TrainFareResponse response =
                trainFareService.addFare(1L, createRequest());

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getTrainId());
        assertEquals("12675", response.getTrainNumber());
        assertEquals("Chennai Express", response.getTrainName());
        assertEquals("AC", response.getTravelClass());
        assertEquals(new BigDecimal("750.00"), response.getFareAmount());

        verify(trainFareRepository).save(any(TrainFare.class));
    }

    @Test
    void addFare_TrainNotFound() {
        when(trainRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainFareService.addFare(1L, createRequest())
        );

        verify(trainFareRepository, never()).save(any(TrainFare.class));
    }

    @Test
    void addFare_DuplicateFare() {
        Train train = createTrain();

        when(trainRepository.findById(1L))
                .thenReturn(Optional.of(train));

        when(trainFareRepository.findByTrain_IdAndTravelClass(1L, "AC"))
                .thenReturn(Optional.of(createFare(train)));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> trainFareService.addFare(1L, createRequest())
        );

        assertEquals(
                "Fare already exists for train 12675 and class AC",
                exception.getMessage()
        );

        verify(trainFareRepository, never()).save(any(TrainFare.class));
    }

    @Test
    void getTrainFares_Success() {
        Train train = createTrain();
        TrainFare fare = createFare(train);

        when(trainRepository.findById(1L))
                .thenReturn(Optional.of(train));

        when(trainFareRepository.findByTrain_Id(1L))
                .thenReturn(List.of(fare));

        List<TrainFareResponse> responses =
                trainFareService.getTrainFares(1L);

        assertEquals(1, responses.size());
        assertEquals("AC", responses.get(0).getTravelClass());
        assertEquals(new BigDecimal("750.00"),
                responses.get(0).getFareAmount());
    }

    @Test
    void getTrainFares_EmptyList() {
        when(trainRepository.findById(1L))
                .thenReturn(Optional.of(createTrain()));

        when(trainFareRepository.findByTrain_Id(1L))
                .thenReturn(List.of());

        List<TrainFareResponse> responses =
                trainFareService.getTrainFares(1L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getTrainFares_TrainNotFound() {
        when(trainRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainFareService.getTrainFares(1L)
        );

        verify(trainFareRepository, never()).findByTrain_Id(1L);
    }
}
