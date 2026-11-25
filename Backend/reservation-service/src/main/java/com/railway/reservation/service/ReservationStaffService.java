
package com.railway.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railway.reservation.dto.PassengerTicketResponse;
import com.railway.reservation.dto.StaffBookingResponse;
import com.railway.reservation.entity.BoardingStatus;
import com.railway.reservation.entity.PassengerTicket;
import com.railway.reservation.entity.Reservation;
import com.railway.reservation.entity.ReservationStatus;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.repository.PassengerTicketRepository;
import com.railway.reservation.repository.ReservationRepository;
import com.railway.reservation.client.TrainServiceClient;
import com.railway.reservation.dto.TrainResponse;

@Service
public class ReservationStaffService {

    private final ReservationRepository reservationRepository;
    private final PassengerTicketRepository ticketRepository;
    private final TrainServiceClient trainServiceClient;

    public ReservationStaffService(
            ReservationRepository reservationRepository,
            PassengerTicketRepository ticketRepository,
            TrainServiceClient trainServiceClient) {

        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.trainServiceClient = trainServiceClient;
    }

    @Transactional(readOnly = true)
    public StaffBookingResponse getBookingForStaff(String pnr) {

        Reservation reservation = reservationRepository.findByPnr(pnr)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        return buildStaffBookingResponse(reservation);
    }

    @Transactional
    public StaffBookingResponse markPassengerBoarded(
            String pnr,
            Long ticketId) {

        Reservation reservation = reservationRepository.findByPnr(pnr)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Only confirmed bookings can be boarded");
        }

        PassengerTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Passenger ticket not found"));

        if (ticket.getReservation() == null
                || !ticket.getReservation().getId()
                        .equals(reservation.getId())) {

            throw new ResourceNotFoundException(
                    "Passenger ticket does not belong to this booking");
        }

        if (ticket.getBoardingStatus() == BoardingStatus.BOARDED) {
            throw new IllegalArgumentException(
                    "Passenger is already marked as boarded");
        }

        ticket.setBoardingStatus(BoardingStatus.BOARDED);
        ticketRepository.save(ticket);

        return buildStaffBookingResponse(reservation);
    }

    private StaffBookingResponse buildStaffBookingResponse(
            Reservation reservation) {

        TrainResponse train =
                trainServiceClient.getTrainById(reservation.getTrainId());

        var passengers = reservation.getTickets().stream()
                .map(ticket -> new PassengerTicketResponse(
                        ticket.getId(),
                        ticket.getPassengerName(),
                        ticket.getAge(),
                        ticket.getGender(),
                        ticket.getSeatNumber(),
                        ticket.getBoardingStatus()
                ))
                .toList();

        return new StaffBookingResponse(
                reservation.getPnr(),
                reservation.getPassengerEmail(),
                train.getTrainNumber(),
                train.getTrainName(),
                reservation.getTravelDate(),
                reservation.getTravelClass(),
                reservation.getStatus(),
                passengers
        );
    }
}
