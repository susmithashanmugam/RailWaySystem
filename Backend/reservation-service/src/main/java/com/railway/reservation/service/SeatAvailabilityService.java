
package com.railway.reservation.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railway.reservation.client.TrainServiceClient;
import com.railway.reservation.dto.SeatAvailabilityResponse;
import com.railway.reservation.dto.TrainResponse;
import com.railway.reservation.dto.TrainStopResponse;
import com.railway.reservation.entity.SeatAllocation;
import com.railway.reservation.repository.SeatAllocationRepository;

@Service
public class SeatAvailabilityService {

    private final SeatAllocationRepository seatAllocationRepository;
    private final TrainServiceClient trainServiceClient;

    @Value("${reservation.default-seat-capacity:72}")
    private int seatCapacity;

    public SeatAvailabilityService(
            SeatAllocationRepository seatAllocationRepository,
            TrainServiceClient trainServiceClient) {

        this.seatAllocationRepository = seatAllocationRepository;
        this.trainServiceClient = trainServiceClient;
    }

    @Transactional(readOnly = true)
    public SeatAvailabilityResponse checkAvailability(
            Long trainId,
            Long sourceStationId,
            Long destinationStationId,
            LocalDate travelDate,
            String travelClass) {

        // Validate required inputs
        if (trainId == null || sourceStationId == null
                || destinationStationId == null
                || travelDate == null
                || travelClass == null
                || travelClass.isBlank()) {

            throw new IllegalArgumentException(
                    "All journey details are required");
        }

        // Validate travel date
        if (travelDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Travel date cannot be in the past");
        }

        // Validate source and destination
        if (sourceStationId.equals(destinationStationId)) {
            throw new IllegalArgumentException(
                    "Source and destination cannot be the same");
        }

        // Validate seat capacity
        if (seatCapacity <= 0) {
            throw new IllegalStateException(
                    "Seat capacity must be greater than zero");
        }

        String normalizedClass = travelClass.trim().toUpperCase();

        // Fetch train details
        TrainResponse train =
                trainServiceClient.getTrainById(trainId);

        if (train == null || !train.isActive()) {
            throw new IllegalArgumentException(
                    "Train does not exist or is inactive");
        }

        // Fetch train route
        List<TrainStopResponse> route =
                trainServiceClient.getTrainRoute(trainId);

        if (route == null || route.isEmpty()) {
            throw new IllegalArgumentException(
                    "Train route is unavailable");
        }

        // Find source station
        TrainStopResponse source = route.stream()
                .filter(stop ->
                        sourceStationId.equals(stop.getStationId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Source station is not on this train route"));

        // Find destination station
        TrainStopResponse destination = route.stream()
                .filter(stop ->
                        destinationStationId.equals(stop.getStationId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination station is not on this train route"));

        // Validate journey direction
        if (source.getStopNumber() == null
                || destination.getStopNumber() == null
                || source.getStopNumber() >= destination.getStopNumber()) {

            throw new IllegalArgumentException(
                    "Destination must come after source");
        }

        // Generate journey segments
        List<Integer> segments = IntStream.range(
                source.getStopNumber(),
                destination.getStopNumber()
        ).boxed().toList();

        // Fetch existing seat allocations for journey segments
        List<SeatAllocation> allocations =
                seatAllocationRepository
                        .findByTrainIdAndTravelDateAndTravelClassAndSegmentNumberIn(
                                train.getId(),
                                travelDate,
                                normalizedClass,
                                segments
                        );

        // Initialize all seats as available
        Set<String> availableOnEverySegment = new HashSet<>();

        for (int seat = 1; seat <= seatCapacity; seat++) {
            availableOnEverySegment.add("S" + seat);
        }

        // Remove seats occupied on any segment of the journey
        for (Integer segment : segments) {

            Set<String> occupiedOnSegment = new HashSet<>();

            for (SeatAllocation allocation : allocations) {

                if (segment.equals(allocation.getSegmentNumber())) {

                    occupiedOnSegment.add(
                            allocation.getSeatNumber()
                    );
                }
            }

            availableOnEverySegment.removeAll(occupiedOnSegment);
        }

        // Calculate final availability
        int availableSeats = availableOnEverySegment.size();

        int bookedSeats = seatCapacity - availableSeats;

        // Return availability response
        return new SeatAvailabilityResponse(
                train.getId(),
                travelDate,
                normalizedClass,
                sourceStationId,
                destinationStationId,
                seatCapacity,
                bookedSeats,
                availableSeats
        );
    }
}
