
package com.railway.trainservice.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.railway.trainservice.dto.CreateTrainRequest;
import com.railway.trainservice.dto.TrainResponse;
import com.railway.trainservice.entity.Train;
import com.railway.trainservice.exception.DuplicateResourceException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.TrainRepository;

@ExtendWith(MockitoExtension.class)
class TrainServiceTest {

    @Mock
    private TrainRepository trainRepository;

    @InjectMocks
    private TrainService trainService;

    private CreateTrainRequest request;
    private Train train;

    @BeforeEach
    void setUp() {

        request = new CreateTrainRequest();

        request.setTrainNumber("12675");
        request.setTrainName("Kovai Express");
        request.setTrainType("Express");

        train = new Train();

        train.setId(1L);
        train.setTrainNumber("12675");
        train.setTrainName("Kovai Express");
        train.setTrainType("EXPRESS");
        train.setActive(true);
    }

    // TEST 1: CREATE TRAIN SUCCESSFULLY
    @Test
    void createTrain_Success() {

        when(trainRepository.existsByTrainNumber("12675"))
                .thenReturn(false);

        when(trainRepository.save(any(Train.class)))
                .thenAnswer(invocation -> {
                    Train savedTrain = invocation.getArgument(0);
                    savedTrain.setId(1L);
                    return savedTrain;
                });

        TrainResponse response = trainService.createTrain(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("12675", response.getTrainNumber());
        assertEquals("Kovai Express", response.getTrainName());
        assertEquals("EXPRESS", response.getTrainType());
        assertTrue(response.isActive());

        verify(trainRepository).existsByTrainNumber("12675");
        verify(trainRepository).save(any(Train.class));
    }

    // TEST 2: DUPLICATE TRAIN NUMBER
    @Test
    void createTrain_DuplicateTrainNumber() {

        when(trainRepository.existsByTrainNumber("12675"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> trainService.createTrain(request)
        );

        verify(trainRepository, never()).save(any(Train.class));
    }

    // TEST 3: TRIM SPACES AND CONVERT TYPE
    @Test
    void createTrain_ShouldTrimSpacesAndConvertType() {

        request.setTrainNumber(" 12675 ");
        request.setTrainName(" Kovai Express ");
        request.setTrainType(" express ");

        when(trainRepository.existsByTrainNumber("12675"))
                .thenReturn(false);

        when(trainRepository.save(any(Train.class)))
                .thenAnswer(invocation -> {
                    Train savedTrain = invocation.getArgument(0);
                    savedTrain.setId(2L);
                    return savedTrain;
                });

        TrainResponse response = trainService.createTrain(request);

        assertEquals("12675", response.getTrainNumber());
        assertEquals("Kovai Express", response.getTrainName());
        assertEquals("EXPRESS", response.getTrainType());

        ArgumentCaptor<Train> captor =
                ArgumentCaptor.forClass(Train.class);

        verify(trainRepository).save(captor.capture());

        Train savedTrain = captor.getValue();

        assertEquals("12675", savedTrain.getTrainNumber());
        assertEquals("Kovai Express", savedTrain.getTrainName());
        assertEquals("EXPRESS", savedTrain.getTrainType());
    }

    // TEST 4: GET ALL TRAINS
    @Test
    void getAllTrains_Success() {

        Train secondTrain = new Train();

        secondTrain.setId(2L);
        secondTrain.setTrainNumber("12678");
        secondTrain.setTrainName("Cheran Express");
        secondTrain.setTrainType("EXPRESS");
        secondTrain.setActive(true);

        when(trainRepository.findAll())
                .thenReturn(List.of(train, secondTrain));

        List<TrainResponse> response = trainService.getAllTrains();

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals("12675", response.get(0).getTrainNumber());
        assertEquals("Kovai Express", response.get(0).getTrainName());

        assertEquals("12678", response.get(1).getTrainNumber());
        assertEquals("Cheran Express", response.get(1).getTrainName());

        verify(trainRepository).findAll();
    }

    // TEST 5: GET ALL TRAINS WHEN EMPTY
    @Test
    void getAllTrains_EmptyList() {

        when(trainRepository.findAll())
                .thenReturn(List.of());

        List<TrainResponse> response = trainService.getAllTrains();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(trainRepository).findAll();
    }

    // TEST 6: GET TRAIN BY NUMBER
    @Test
    void getTrainByNumber_Success() {

        when(trainRepository.findByTrainNumber("12675"))
                .thenReturn(Optional.of(train));

        TrainResponse response =
                trainService.getTrainByNumber("12675");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("12675", response.getTrainNumber());
        assertEquals("Kovai Express", response.getTrainName());
        assertEquals("EXPRESS", response.getTrainType());
        assertTrue(response.isActive());

        verify(trainRepository).findByTrainNumber("12675");
    }

    // TEST 7: TRAIN NOT FOUND BY NUMBER
    @Test
    void getTrainByNumber_NotFound() {

        when(trainRepository.findByTrainNumber("99999"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainService.getTrainByNumber("99999")
        );

        verify(trainRepository).findByTrainNumber("99999");
    }

    // TEST 8: GET TRAIN BY ID
    @Test
    void getTrainById_Success() {

        when(trainRepository.findById(1L))
                .thenReturn(Optional.of(train));

        TrainResponse response = trainService.getTrainById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("12675", response.getTrainNumber());
        assertEquals("Kovai Express", response.getTrainName());
        assertEquals("EXPRESS", response.getTrainType());
        assertTrue(response.isActive());

        verify(trainRepository).findById(1L);
    }

    // TEST 9: TRAIN NOT FOUND BY ID
    @Test
    void getTrainById_NotFound() {

        when(trainRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainService.getTrainById(99L)
        );

        verify(trainRepository).findById(99L);
    }
}
