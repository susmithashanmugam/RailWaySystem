
package com.railway.trainservice.service;

import com.railway.trainservice.dto.CreateStationRequest;
import com.railway.trainservice.dto.StationResponse;
import com.railway.trainservice.entity.Station;
import com.railway.trainservice.exception.DuplicateResourceException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.StationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional
    public StationResponse createStation(CreateStationRequest request) {

        String stationCode = request.getStationCode().trim().toUpperCase();

        if (stationRepository.existsByStationCode(stationCode)) {
            throw new DuplicateResourceException(
                    "Station already exists with code: " + stationCode
            );
        }

        Station station = new Station();

        station.setStationCode(stationCode);
        station.setStationName(request.getStationName().trim());
        station.setCity(request.getCity().trim());
        station.setState(request.getState().trim());
        station.setActive(true);

        Station savedStation = stationRepository.save(station);

        return convertToResponse(savedStation);
    }

    @Transactional(readOnly = true)
    public List<StationResponse> getAllStations() {

        return stationRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StationResponse getStationByCode(String stationCode) {

        Station station = stationRepository.findByStationCode(
                stationCode.trim().toUpperCase()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Station not found with code: " + stationCode
        ));

        return convertToResponse(station);
    }

    private StationResponse convertToResponse(Station station) {

        return new StationResponse(
                station.getId(),
                station.getStationCode(),
                station.getStationName(),
                station.getCity(),
                station.getState(),
                station.isActive()
        );
    }
}
