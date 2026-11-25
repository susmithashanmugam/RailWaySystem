
package com.railway.trainservice.service;

import com.railway.trainservice.dto.CreateStationRequest;
import com.railway.trainservice.dto.StationResponse;
import com.railway.trainservice.entity.Station;
import com.railway.trainservice.exception.DuplicateResourceException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.StationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private StationService stationService;

    private CreateStationRequest createRequest() {
        CreateStationRequest request = new CreateStationRequest();
        request.setStationCode(" chn ");
        request.setStationName(" Chennai Central ");
        request.setCity(" Chennai ");
        request.setState(" Tamil Nadu ");
        return request;
    }

    private Station createStation() {
        Station station = new Station();
        station.setId(1L);
        station.setStationCode("CHN");
        station.setStationName("Chennai Central");
        station.setCity("Chennai");
        station.setState("Tamil Nadu");
        station.setActive(true);
        return station;
    }

    @Test
    void createStation_Success() {
        CreateStationRequest request = createRequest();
        Station savedStation = createStation();

        when(stationRepository.existsByStationCode("CHN"))
                .thenReturn(false);

        when(stationRepository.save(any(Station.class)))
                .thenReturn(savedStation);

        StationResponse response = stationService.createStation(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("CHN", response.getStationCode());
        assertEquals("Chennai Central", response.getStationName());
        assertEquals("Chennai", response.getCity());
        assertEquals("Tamil Nadu", response.getState());
        assertTrue(response.isActive());

        verify(stationRepository).existsByStationCode("CHN");
        verify(stationRepository).save(any(Station.class));
    }

    @Test
    void createStation_DuplicateCode() {
        CreateStationRequest request = createRequest();

        when(stationRepository.existsByStationCode("CHN"))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> stationService.createStation(request)
        );

        assertEquals(
                "Station already exists with code: CHN",
                exception.getMessage()
        );

        verify(stationRepository, never()).save(any(Station.class));
    }

    @Test
    void getAllStations_Success() {
        Station station1 = createStation();

        Station station2 = new Station();
        station2.setId(2L);
        station2.setStationCode("CBE");
        station2.setStationName("Coimbatore Junction");
        station2.setCity("Coimbatore");
        station2.setState("Tamil Nadu");
        station2.setActive(false);

        when(stationRepository.findAll())
                .thenReturn(List.of(station1, station2));

        List<StationResponse> responses =
                stationService.getAllStations();

        assertEquals(2, responses.size());

        assertEquals("CHN", responses.get(0).getStationCode());
        assertEquals("Chennai Central", responses.get(0).getStationName());
        assertTrue(responses.get(0).isActive());

        assertEquals("CBE", responses.get(1).getStationCode());
        assertEquals("Coimbatore Junction", responses.get(1).getStationName());
        assertFalse(responses.get(1).isActive());

        verify(stationRepository).findAll();
    }

    @Test
    void getAllStations_EmptyList() {
        when(stationRepository.findAll())
                .thenReturn(List.of());

        List<StationResponse> responses =
                stationService.getAllStations();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(stationRepository).findAll();
    }

    @Test
    void getStationByCode_Success() {
        Station station = createStation();

        when(stationRepository.findByStationCode("CHN"))
                .thenReturn(Optional.of(station));

        StationResponse response =
                stationService.getStationByCode(" chn ");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("CHN", response.getStationCode());
        assertEquals("Chennai Central", response.getStationName());
        assertEquals("Chennai", response.getCity());
        assertEquals("Tamil Nadu", response.getState());
        assertTrue(response.isActive());

        verify(stationRepository).findByStationCode("CHN");
    }

    @Test
    void getStationByCode_NotFound() {
        when(stationRepository.findByStationCode("XYZ"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> stationService.getStationByCode(" xyz ")
        );

        assertEquals(
                "Station not found with code:  xyz ",
                exception.getMessage()
        );

        verify(stationRepository).findByStationCode("XYZ");
    }
}
