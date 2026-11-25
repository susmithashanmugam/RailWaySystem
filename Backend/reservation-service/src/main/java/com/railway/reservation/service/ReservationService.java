
package com.railway.reservation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railway.reservation.client.TrainServiceClient;
import com.railway.reservation.dto.CreateReservationRequest;
import com.railway.reservation.dto.PassengerDetailRequest;
import com.railway.reservation.dto.PassengerTicketResponse;
import com.railway.reservation.dto.ReservationResponse;
import com.railway.reservation.dto.TrainFareResponse;
import com.railway.reservation.dto.TrainResponse;
import com.railway.reservation.dto.TrainStopResponse;
import com.railway.reservation.entity.PassengerTicket;
import com.railway.reservation.entity.Reservation;
import com.railway.reservation.entity.ReservationStatus;
import com.railway.reservation.entity.SeatAllocation;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.repository.ReservationRepository;
import com.railway.reservation.repository.SeatAllocationRepository;
import com.railway.reservation.repository.PassengerTicketRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PassengerTicketRepository ticketRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final TrainServiceClient trainServiceClient;

    @Value("${reservation.default-seat-capacity:72}")
    private int seatCapacity;

    // 1. Book a ticket
    @Transactional
    public ReservationResponse bookTicket(
            CreateReservationRequest request,
            String passengerEmail) {

        if (request == null) {
            throw new IllegalArgumentException("Booking details are required");
        }

        if (passengerEmail == null || passengerEmail.isBlank()) {
            throw new IllegalArgumentException("Passenger email is required");
        }

        if (seatCapacity <= 0) {
            throw new IllegalStateException("Seat capacity must be greater than zero");
        }

        if (request.getTravelDate() == null
                || request.getTravelDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Invalid travel date");
        }

        if (request.getPassengers() == null
                || request.getPassengers().isEmpty()
                || request.getPassengers().size() > 6) {
            throw new IllegalArgumentException(
                    "Booking must contain between 1 and 6 passengers");
        }

        if (request.getTravelClass() == null
                || request.getTravelClass().isBlank()) {
            throw new IllegalArgumentException("Travel class is required");
        }

        // Validate train
        TrainResponse train;

        try {
            train = trainServiceClient.getTrainById(request.getTrainId());
        } catch (FeignException.NotFound exception) {
            throw new ResourceNotFoundException("Train not found");
        }

        if (train == null || !train.isActive()) {
            throw new IllegalArgumentException("Train does not exist or is inactive");
        }

        // Validate train route
        List<TrainStopResponse> route =
                trainServiceClient.getTrainRoute(request.getTrainId());

        if (route == null || route.isEmpty()) {
            throw new IllegalArgumentException("Train route is unavailable");
        }

        TrainStopResponse source = route.stream()
                .filter(stop -> request.getSourceStationId()
                        .equals(stop.getStationId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Source station is not on this train route"));

        TrainStopResponse destination = route.stream()
                .filter(stop -> request.getDestinationStationId()
                        .equals(stop.getStationId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination station is not on this train route"));

        if (source.getStopNumber() == null
                || destination.getStopNumber() == null
                || source.getStopNumber() >= destination.getStopNumber()) {
            throw new IllegalArgumentException(
                    "Destination must come after source");
        }

        // Validate travel class and retrieve fare
        String travelClass = request.getTravelClass().trim().toUpperCase();

        List<TrainFareResponse> fares =
                trainServiceClient.getTrainFares(request.getTrainId());

        BigDecimal farePerPassenger = fares.stream()
                .filter(fare -> fare.getTravelClass() != null
                        && fare.getTravelClass().equalsIgnoreCase(travelClass))
                .map(TrainFareResponse::getFareAmount)
                .filter(amount -> amount != null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fare not available for class: " + travelClass));

        BigDecimal totalAmount = farePerPassenger.multiply(
                BigDecimal.valueOf(request.getPassengers().size()));

        // Generate PNR
        String pnr = generatePnr();

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setPnr(pnr);
        reservation.setPassengerEmail(passengerEmail);
        reservation.setTrainId(train.getId());
        reservation.setSourceStationId(source.getStationId());
        reservation.setDestinationStationId(destination.getStationId());
        reservation.setTravelDate(request.getTravelDate());
        reservation.setTravelClass(travelClass);
        reservation.setTotalAmount(totalAmount);
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        // Create passenger tickets
        List<PassengerTicket> tickets = new ArrayList<>();

        for (PassengerDetailRequest passenger : request.getPassengers()) {
            PassengerTicket ticket = new PassengerTicket();
            ticket.setReservation(reservation);
            ticket.setPassengerName(passenger.getPassengerName().trim());
            ticket.setAge(passenger.getAge());
            ticket.setGender(passenger.getGender().trim().toUpperCase());
            tickets.add(ticket);
        }

        reservation.setTickets(tickets);

        // Save reservation and generate ticket IDs
        Reservation savedReservation =
                reservationRepository.saveAndFlush(reservation);

        // Find journey segments
        List<Integer> segments = new ArrayList<>();

        for (int i = source.getStopNumber();
                i < destination.getStopNumber(); i++) {
            segments.add(i);
        }

        // Find already occupied seats
        List<SeatAllocation> existingAllocations =
                seatAllocationRepository
                        .findByTrainIdAndTravelDateAndTravelClassAndSegmentNumberIn(
                                train.getId(),
                                request.getTravelDate(),
                                travelClass,
                                segments);

        Map<Integer, Set<String>> occupiedSeats = new HashMap<>();

        for (SeatAllocation allocation : existingAllocations) {
            occupiedSeats.computeIfAbsent(
                    allocation.getSegmentNumber(),
                    key -> new HashSet<>()
            ).add(allocation.getSeatNumber());
        }

        // Allocate one common free seat across all journey segments
        List<SeatAllocation> newAllocations = new ArrayList<>();

        for (PassengerTicket ticket : tickets) {

            String assignedSeat = null;

            for (int seat = 1; seat <= seatCapacity; seat++) {

                String seatNumber = "S" + seat;

                boolean available = segments.stream().allMatch(segment ->
                        !occupiedSeats.getOrDefault(
                                segment,
                                Collections.emptySet()
                        ).contains(seatNumber)
                );

                if (available) {
                    assignedSeat = seatNumber;
                    break;
                }
            }

            if (assignedSeat == null) {
                throw new IllegalArgumentException(
                        "Not enough seats available for this journey");
            }

            ticket.setSeatNumber(assignedSeat);

            // Reserve the seat on every journey segment
            for (Integer segment : segments) {

                SeatAllocation allocation = new SeatAllocation();
                allocation.setTrainId(train.getId());
                allocation.setTravelDate(request.getTravelDate());
                allocation.setTravelClass(travelClass);
                allocation.setSeatNumber(assignedSeat);
                allocation.setSegmentNumber(segment);
                allocation.setReservationId(savedReservation.getId());
                allocation.setTicketId(ticket.getId());

                newAllocations.add(allocation);

                occupiedSeats.computeIfAbsent(
                        segment,
                        key -> new HashSet<>()
                ).add(assignedSeat);
            }
        }

        // Save ticket seat numbers and allocations
        ticketRepository.saveAll(tickets);
        seatAllocationRepository.saveAllAndFlush(newAllocations);

        return convertToResponse(savedReservation, train);
    }

    // 2. Get all bookings made by a passenger
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyBookings(String passengerEmail) {

        List<Reservation> reservations =
                reservationRepository.findByPassengerEmailOrderByBookedAtDesc(
                        passengerEmail);

        return reservations.stream()
                .map(reservation -> {
                    TrainResponse train =
                            trainServiceClient.getTrainById(
                                    reservation.getTrainId());

                    return convertToResponse(reservation, train);
                })
                .toList();
    }

    // 3. Get booking details using PNR
    @Transactional(readOnly = true)
    public ReservationResponse getBookingByPnr(
            String pnr,
            String passengerEmail) {

        Reservation reservation = reservationRepository.findByPnr(pnr)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!reservation.getPassengerEmail().equals(passengerEmail)) {
            throw new ResourceNotFoundException("Booking not found");
        }

        TrainResponse train =
                trainServiceClient.getTrainById(reservation.getTrainId());

        return convertToResponse(reservation, train);
    }

    // 4. Cancel a booking
    @Transactional
    public ReservationResponse cancelBooking(
            String pnr,
            String passengerEmail) {

        Reservation reservation = reservationRepository.findByPnr(pnr)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!reservation.getPassengerEmail().equals(passengerEmail)) {
            throw new ResourceNotFoundException("Booking not found");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        seatAllocationRepository.deleteByReservationId(
                reservation.getId());

        Reservation cancelledReservation =
                reservationRepository.save(reservation);

        TrainResponse train =
                trainServiceClient.getTrainById(reservation.getTrainId());

        return convertToResponse(cancelledReservation, train);
    }

    // Generate PNR
    private String generatePnr() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }

    // Convert reservation entity to response DTO
    private ReservationResponse convertToResponse(
            Reservation reservation,
            TrainResponse train) {

        List<PassengerTicketResponse> ticketResponses =
                reservation.getTickets().stream()
                        .map(ticket -> new PassengerTicketResponse(
                                ticket.getId(),
                                ticket.getPassengerName(),
                                ticket.getAge(),
                                ticket.getGender(),
                                ticket.getSeatNumber(),
                                ticket.getBoardingStatus()
                        ))
                        .collect(Collectors.toList());

        return new ReservationResponse(
                reservation.getId(),
                reservation.getPnr(),
                reservation.getPassengerEmail(),
                reservation.getTrainId(),
                train.getTrainNumber(),
                train.getTrainName(),
                reservation.getSourceStationId(),
                reservation.getDestinationStationId(),
                reservation.getTravelDate(),
                reservation.getTravelClass(),
                reservation.getTotalAmount(),
                reservation.getStatus(),
                reservation.getBookedAt(),
                ticketResponses
        );
    }
}
