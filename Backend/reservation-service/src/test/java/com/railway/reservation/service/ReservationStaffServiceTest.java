
package com.railway.reservation.service;

import com.railway.reservation.client.TrainServiceClient;
import com.railway.reservation.dto.StaffBookingResponse;
import com.railway.reservation.dto.TrainResponse;
import com.railway.reservation.entity.BoardingStatus;
import com.railway.reservation.entity.PassengerTicket;
import com.railway.reservation.entity.Reservation;
import com.railway.reservation.entity.ReservationStatus;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.repository.PassengerTicketRepository;
import com.railway.reservation.repository.ReservationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationStaffServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PassengerTicketRepository passengerTicketRepository;

    @Mock
    private TrainServiceClient trainServiceClient;

    @InjectMocks
    private ReservationStaffService reservationStaffService;

    private Reservation reservation;
    private PassengerTicket ticket;
    private TrainResponse trainResponse;

    @BeforeEach
    void setUp() {

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setPnr("PNR123");
        reservation.setPassengerEmail("passenger@gmail.com");
        reservation.setTrainId(10L);
        reservation.setTravelDate(LocalDate.now().plusDays(5));
        reservation.setTravelClass("AC");
        reservation.setStatus(ReservationStatus.CONFIRMED);

        ticket = new PassengerTicket();
        ticket.setId(100L);
        ticket.setReservation(reservation);
        ticket.setPassengerName("Arun");
        ticket.setAge(25);
        ticket.setGender("Male");
        ticket.setSeatNumber("S1");
        ticket.setBoardingStatus(BoardingStatus.NOT_BOARDED);

        reservation.setTickets(List.of(ticket));

        trainResponse = new TrainResponse();
        trainResponse.setTrainNumber("12601");
        trainResponse.setTrainName("Express Train");
    }

    @Test
    void getBookingForStaff_Success() {

        when(reservationRepository.findByPnr("PNR123"))
                .thenReturn(Optional.of(reservation));

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(trainResponse);

        StaffBookingResponse response =
                reservationStaffService.getBookingForStaff("PNR123");

        assertNotNull(response);
        assertEquals("PNR123", response.getPnr());
        assertEquals("passenger@gmail.com", response.getPassengerEmail());
        assertEquals("12601", response.getTrainNumber());
        assertEquals("Express Train", response.getTrainName());
        assertEquals(1, response.getPassengers().size());
    }

    @Test
    void getBookingForStaff_BookingNotFound() {

        when(reservationRepository.findByPnr("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationStaffService.getBookingForStaff("INVALID")
        );
    }

    @Test
    void markPassengerBoarded_Success() {

        when(reservationRepository.findByPnr("PNR123"))
                .thenReturn(Optional.of(reservation));

        when(passengerTicketRepository.findById(100L))
                .thenReturn(Optional.of(ticket));

        when(passengerTicketRepository.save(any(PassengerTicket.class)))
                .thenReturn(ticket);

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(trainResponse);

        StaffBookingResponse response =
                reservationStaffService.markPassengerBoarded("PNR123", 100L);

        assertNotNull(response);
        assertEquals(BoardingStatus.BOARDED, ticket.getBoardingStatus());

        verify(passengerTicketRepository).save(ticket);
    }

    @Test
    void markPassengerBoarded_BookingNotFound() {

        when(reservationRepository.findByPnr("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationStaffService.markPassengerBoarded(
                        "INVALID", 100L
                )
        );
    }

    @Test
    void markPassengerBoarded_ReservationNotConfirmed() {

        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findByPnr("PNR123"))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationStaffService.markPassengerBoarded(
                        "PNR123", 100L
                )
        );

        verify(passengerTicketRepository, never()).findById(any());
    }

    @Test
    void markPassengerBoarded_TicketNotFound() {

        when(reservationRepository.findByPnr("PNR123"))
                .thenReturn(Optional.of(reservation));

        when(passengerTicketRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationStaffService.markPassengerBoarded(
                        "PNR123", 100L
                )
        );
    }

    @Test
    void markPassengerBoarded_TicketBelongsToAnotherReservation() {

        Reservation anotherReservation = new Reservation();
        anotherReservation.setId(2L);

        ticket.setReservation(anotherReservation);

        when(reservationRepository.findByPnr("PNR123"))
                .thenReturn(Optional.of(reservation));

        when(passengerTicketRepository.findById(100L))
                .thenReturn(Optional.of(ticket));

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationStaffService.markPassengerBoarded(
                        "PNR123", 100L
                )
        );
    }

    @Test
    void markPassengerBoarded_AlreadyBoarded() {

        ticket.setBoardingStatus(BoardingStatus.BOARDED);

        when(reservationRepository.findByPnr("PNR123"))
                .thenReturn(Optional.of(reservation));

        when(passengerTicketRepository.findById(100L))
                .thenReturn(Optional.of(ticket));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationStaffService.markPassengerBoarded(
                        "PNR123", 100L
                )
        );

        verify(passengerTicketRepository, never()).save(any());
    }
}
