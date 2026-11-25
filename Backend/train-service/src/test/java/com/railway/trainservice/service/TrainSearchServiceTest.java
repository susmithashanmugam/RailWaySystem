
package com.railway.trainservice.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.railway.trainservice.dto.TrainSearchResponse;
import com.railway.trainservice.entity.Station;
import com.railway.trainservice.entity.Train;
import com.railway.trainservice.entity.TrainStop;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.TrainStopRepository;

@ExtendWith(MockitoExtension.class)
class TrainSearchServiceTest {

    @Mock
    private TrainStopRepository trainStopRepository;

    @InjectMocks
    private TrainSearchService trainSearchService;

    private Train train;
    private TrainStop sourceStop;
    private TrainStop destinationStop;
    private Station sourceStation;
    private Station destinationStation;

    @BeforeEach
    void setUp() {

        // Create mocks only. Keep stubbing inside individual tests.
        train = mock(Train.class);
        sourceStop = mock(TrainStop.class);
        destinationStop = mock(TrainStop.class);
        sourceStation = mock(Station.class);
        destinationStation = mock(Station.class);
    }

    // TEST 1: Matching train found successfully
    @Test
    void searchTrains_ShouldReturnMatchingTrain() {

        when(train.getId()).thenReturn(1L);
        when(train.getTrainNumber()).thenReturn("12632");
        when(train.getTrainName()).thenReturn("Express Train");
        when(train.getTrainType()).thenReturn("EXPRESS");

        when(sourceStation.getId()).thenReturn(10L);
        when(sourceStation.getStationName()).thenReturn("Chennai");

        when(destinationStation.getId()).thenReturn(20L);
        when(destinationStation.getStationName()).thenReturn("Madurai");

        when(sourceStop.getStation()).thenReturn(sourceStation);
        when(destinationStop.getStation()).thenReturn(destinationStation);

        when(trainStopRepository.findTrainsBetweenStations(10L, 20L))
                .thenReturn(List.of(train));

        when(trainStopRepository.findByTrain_IdOrderByStopNumberAsc(1L))
                .thenReturn(List.of(sourceStop, destinationStop));

        List<TrainSearchResponse> result =
                trainSearchService.searchTrains(10L, 20L);

        assertEquals(1, result.size());

        verify(trainStopRepository)
                .findTrainsBetweenStations(10L, 20L);

        verify(trainStopRepository)
                .findByTrain_IdOrderByStopNumberAsc(1L);
    }

    // TEST 2: No trains found
    @Test
    void searchTrains_ShouldReturnEmptyList_WhenNoTrainsFound() {

        when(trainStopRepository.findTrainsBetweenStations(10L, 20L))
                .thenReturn(List.of());

        List<TrainSearchResponse> result =
                trainSearchService.searchTrains(10L, 20L);

        assertTrue(result.isEmpty());

        verify(trainStopRepository)
                .findTrainsBetweenStations(10L, 20L);

        verify(trainStopRepository, never())
                .findByTrain_IdOrderByStopNumberAsc(anyLong());
    }

    // TEST 3: Source station is null
    @Test
    void searchTrains_ShouldThrowException_WhenSourceStationIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> trainSearchService.searchTrains(null, 20L)
        );

        verifyNoInteractions(trainStopRepository);
    }

    // TEST 4: Destination station is null
    @Test
    void searchTrains_ShouldThrowException_WhenDestinationStationIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> trainSearchService.searchTrains(10L, null)
        );

        verifyNoInteractions(trainStopRepository);
    }

    // TEST 5: Both stations are null
    @Test
    void searchTrains_ShouldThrowException_WhenBothStationsAreNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> trainSearchService.searchTrains(null, null)
        );

        verifyNoInteractions(trainStopRepository);
    }

    // TEST 6: Same source and destination
    @Test
    void searchTrains_ShouldThrowException_WhenSourceAndDestinationAreSame() {

        assertThrows(
                IllegalArgumentException.class,
                () -> trainSearchService.searchTrains(10L, 10L)
        );

        verifyNoInteractions(trainStopRepository);
    }

    // TEST 7: Source station not found in route
    @Test
    void searchTrains_ShouldThrowException_WhenSourceNotFoundInRoute() {

        when(train.getId()).thenReturn(1L);

        when(destinationStation.getId()).thenReturn(20L);
        when(destinationStop.getStation()).thenReturn(destinationStation);

        when(trainStopRepository.findTrainsBetweenStations(10L, 20L))
                .thenReturn(List.of(train));

        when(trainStopRepository.findByTrain_IdOrderByStopNumberAsc(1L))
                .thenReturn(List.of(destinationStop));

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainSearchService.searchTrains(10L, 20L)
        );
    }

    // TEST 8: Destination station not found in route
    @Test
    void searchTrains_ShouldThrowException_WhenDestinationNotFoundInRoute() {

        when(train.getId()).thenReturn(1L);

        when(sourceStation.getId()).thenReturn(10L);
        when(sourceStop.getStation()).thenReturn(sourceStation);

        when(trainStopRepository.findTrainsBetweenStations(10L, 20L))
                .thenReturn(List.of(train));

        when(trainStopRepository.findByTrain_IdOrderByStopNumberAsc(1L))
                .thenReturn(List.of(sourceStop));

        assertThrows(
                ResourceNotFoundException.class,
                () -> trainSearchService.searchTrains(10L, 20L)
        );
    }
}
