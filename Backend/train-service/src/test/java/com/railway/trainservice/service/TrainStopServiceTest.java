
package com.railway.trainservice.service;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainStopServiceTest {

    @Mock
    private TrainStopRepository trainStopRepository;

    @Mock
    private TrainRepository trainRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private TrainStopService trainStopService;

    private Train createTrain() {
        Train train = new Train();
        train.setId(1L);
        train.setTrainNumber("12675");
        train.setTrainName("Chennai Express");
        train.setTrainType("EXPRESS");
        return train;
    }

    private Station createStation() {
        Station station = new Station();
        station.setId(2L);
        station.setStationCode("CBE");
        station.setStationName("Coimbatore Junction");
        station.setCity("Coimbatore");
        station.setState("Tamil Nadu");
        return station;
    }

    private TrainStop createStop() {
        TrainStop stop = new TrainStop();
        stop.setId(10L);
        stop.setTrain(createTrain());
        stop.setStation(createStation());
        stop.setStopNumber(2);
        stop.setArrivalTime(LocalTime.of(12, 0));
        stop.setDepartureTime(LocalTime.of(12, 5));
        stop.setDistanceFromSource(500.0);
        return stop;
    }

    private TrainStopRequest createRequest() {
        TrainStopRequest request = new TrainStopRequest();
        request.setStationId(2L);
        request.setStopNumber(2);
        request.setArrivalTime(LocalTime.of(12, 0));
        request.setDepartureTime(LocalTime.of(12, 5));
        request.setDistanceFromSource(new BigDecimal("500.0"));
        return request;
    }

    @Test
    void addStop_Success() {
        Train train = createTrain();
        Station station = createStation();
        TrainStop savedStop = createStop();

        when(trainRepository.findById(1L))
                .thenReturn(Optional.of(train));

        when(stationRepository.findById(2L))
                .thenReturn(Optional.of(station));

        when(trainStopRepository.findByTrain_IdOrderByStopNumberAsc(1L))
                .thenReturn(List.of());

        when(trainStopRepository.save(any(TrainStop.class)))
                .thenReturn(savedStop);

        TrainStopResponse response =
                trainStopService.addStop(1L, createRequest());

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(2L, response.getStationId());
        assertEquals("CBE", response.getStationCode());
        assertEquals("Coimbatore Junction", response.getStationName());
        assertEquals("Coimbatore", response.getCity());
        assertEquals(2, response.getStopNumber());
        assertEquals(LocalTime.of(12, 0), response.getArrivalTime());
        assertEquals(LocalTime.of(12, 5), response.getDepartureTime());
        assertEquals(500.0, response.getDistanceFromSource());

        verify(trainStopRepository).save(any(TrainStop.class));
    }

    @Test
    void addStop_TrainNotFound() {
        when(trainRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainStopService.addStop(1L, createRequest())
        );

        verifyNoInteractions(stationRepository);
        verify(trainStopRepository, never()).save(any(TrainStop.class));
    }

    @Test
    void addStop_StationNotFound() {
        when(trainRepository.findById(1L))
                .thenReturn(Optional.of(createTrain()));

        when(stationRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainStopService.addStop(1L, createRequest())
        );

        verify(trainStopRepository, never()).save(any(TrainStop.class));
    }

    @Test
    void addStop_DuplicateStopNumber() {
        TrainStop existingStop = createStop();

        when(trainRepository.findById(1L))
                .thenReturn(Optional.of(createTrain()));

        when(stationRepository.findById(2L))
                .thenReturn(Optional.of(createStation()));

        when(trainStopRepository.findByTrain_IdOrderByStopNumberAsc(1L))
                .thenReturn(List.of(existingStop));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> trainStopService.addStop(1L, createRequest())
        );

        assertEquals(
                "Stop number already exists for this train: 2",
                exception.getMessage()
        );

        verify(trainStopRepository, never()).save(any(TrainStop.class));
    }

    @Test
    void deleteStop_Success() {
        TrainStop stop = createStop();

        when(trainStopRepository.findById(10L))
                .thenReturn(Optional.of(stop));

        assertDoesNotThrow(
                () -> trainStopService.deleteStop(1L, 10L)
        );

        verify(trainStopRepository).delete(stop);
    }

    @Test
    void deleteStop_StopNotFound() {
        when(trainStopRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainStopService.deleteStop(1L, 10L)
        );

        verify(trainStopRepository, never()).delete(any(TrainStop.class));
    }

    @Test
    void deleteStop_StopBelongsToDifferentTrain() {
        TrainStop stop = createStop();

        when(trainStopRepository.findById(10L))
                .thenReturn(Optional.of(stop));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> trainStopService.deleteStop(99L, 10L)
        );

        assertEquals(
                "Train stop does not belong to train ID: 99",
                exception.getMessage()
        );

        verify(trainStopRepository, never()).delete(any(TrainStop.class));
    }

    @Test
    void getTrainRoute_Success() {
        TrainStop stop = createStop();

        when(trainRepository.existsById(1L))
                .thenReturn(true);

        when(trainStopRepository.findByTrain_IdOrderByStopNumberAsc(1L))
                .thenReturn(List.of(stop));

        List<TrainStopResponse> responses =
                trainStopService.getTrainRoute(1L);

        assertEquals(1, responses.size());
        assertEquals("CBE", responses.get(0).getStationCode());
        assertEquals(2, responses.get(0).getStopNumber());
        assertEquals(500.0, responses.get(0).getDistanceFromSource());
    }

    @Test
    void getTrainRoute_EmptyRoute() {
        when(trainRepository.existsById(1L))
                .thenReturn(true);

        when(trainStopRepository.findByTrain_IdOrderByStopNumberAsc(1L))
                .thenReturn(List.of());

        List<TrainStopResponse> responses =
                trainStopService.getTrainRoute(1L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getTrainRoute_TrainNotFound() {
        when(trainRepository.existsById(1L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainStopService.getTrainRoute(1L)
        );

        verify(trainStopRepository, never())
                .findByTrain_IdOrderByStopNumberAsc(1L);
    }
}
