
package com.railway.reservation.service;

import com.railway.reservation.client.TrainServiceClient;
import com.railway.reservation.dto.CreateReservationRequest;
import com.railway.reservation.dto.PassengerDetailRequest;
import com.railway.reservation.dto.ReservationResponse;
import com.railway.reservation.dto.TrainFareResponse;
import com.railway.reservation.dto.TrainResponse;
import com.railway.reservation.dto.TrainStopResponse;
import com.railway.reservation.entity.BoardingStatus;
import com.railway.reservation.entity.Reservation;
import com.railway.reservation.entity.ReservationStatus;
import com.railway.reservation.entity.SeatAllocation;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.repository.PassengerTicketRepository;
import com.railway.reservation.repository.ReservationRepository;
import com.railway.reservation.repository.SeatAllocationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PassengerTicketRepository ticketRepository;

    @Mock
    private SeatAllocationRepository seatAllocationRepository;

    @Mock
    private TrainServiceClient trainServiceClient;

    @InjectMocks
    private ReservationService reservationService;

    private CreateReservationRequest request;
    private TrainResponse train;
    private List<TrainStopResponse> route;
    private List<TrainFareResponse> fares;

    private LocalDate travelDate;

    private static final String EMAIL = "passenger@gmail.com";

    @BeforeEach
    void setUp() {

        travelDate = LocalDate.now().plusDays(5);

        ReflectionTestUtils.setField(
                reservationService,
                "seatCapacity",
                72
        );

        train = new TrainResponse();
        train.setId(10L);
        train.setTrainNumber("12601");
        train.setTrainName("Express Train");
        train.setActive(true);

        TrainStopResponse source = new TrainStopResponse();
        source.setStationId(1L);
        source.setStopNumber(1);

        TrainStopResponse middle = new TrainStopResponse();
        middle.setStationId(2L);
        middle.setStopNumber(2);

        TrainStopResponse destination = new TrainStopResponse();
        destination.setStationId(3L);
        destination.setStopNumber(3);

        route = List.of(source, middle, destination);

        TrainFareResponse fare = new TrainFareResponse();
        fare.setTrainId(10L);
        fare.setTravelClass("AC");
        fare.setFareAmount(new BigDecimal("500"));

        fares = List.of(fare);

        request = createRequest();
    }

    private CreateReservationRequest createRequest() {

        CreateReservationRequest booking =
                new CreateReservationRequest();

        booking.setTrainId(10L);
        booking.setSourceStationId(1L);
        booking.setDestinationStationId(3L);
        booking.setTravelDate(travelDate);
        booking.setTravelClass("AC");

        PassengerDetailRequest passenger =
                new PassengerDetailRequest();

        passenger.setPassengerName("Arun");
        passenger.setAge(25);
        passenger.setGender("Male");

        booking.setPassengers(List.of(passenger));

        return booking;
    }

    private void mockValidTrainDetails() {

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        when(trainServiceClient.getTrainRoute(10L))
                .thenReturn(route);

        when(trainServiceClient.getTrainFares(10L))
                .thenReturn(fares);
    }

    private Reservation createReservation() {

        Reservation reservation = new Reservation();

        reservation.setId(50L);
        reservation.setPnr("PNR1234567");
        reservation.setPassengerEmail(EMAIL);
        reservation.setTrainId(10L);
        reservation.setSourceStationId(1L);
        reservation.setDestinationStationId(3L);
        reservation.setTravelDate(travelDate);
        reservation.setTravelClass("AC");
        reservation.setTotalAmount(new BigDecimal("500"));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setTickets(new ArrayList<>());

        return reservation;
    }

    // 1. Successful booking

    @Test
    void bookTicket_Success() {

        mockValidTrainDetails();

        when(reservationRepository.saveAndFlush(
                any(Reservation.class)
        )).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            saved.setId(50L);
            return saved;
        });

        when(seatAllocationRepository
                .findByTrainIdAndTravelDateAndTravelClassAndSegmentNumberIn(
                        eq(10L),
                        eq(travelDate),
                        eq("AC"),
                        anyList()
                ))
                .thenReturn(List.of());

        ReservationResponse response =
                reservationService.bookTicket(request, EMAIL);

        assertNotNull(response);
        assertEquals(50L, response.getReservationId());
        assertEquals(EMAIL, response.getPassengerEmail());
        assertEquals("12601", response.getTrainNumber());
        assertEquals("Express Train", response.getTrainName());
        assertEquals("AC", response.getTravelClass());
        assertEquals(new BigDecimal("500"), response.getTotalAmount());
        assertEquals(ReservationStatus.PENDING_PAYMENT, response.getStatus());

        assertEquals(1, response.getTickets().size());
        assertEquals("S1", response.getTickets().get(0).getSeatNumber());

        verify(reservationRepository).saveAndFlush(any(Reservation.class));
        verify(ticketRepository).saveAll(anyList());
        verify(seatAllocationRepository).saveAllAndFlush(anyList());
    }

    // 2. Null booking request

    @Test
    void bookTicket_NullRequest() {

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(null, EMAIL)
        );
    }

    // 3. Missing passenger email

    @Test
    void bookTicket_MissingEmail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, " ")
        );
    }

    // 4. Invalid seat capacity

    @Test
    void bookTicket_InvalidSeatCapacity() {

        ReflectionTestUtils.setField(
                reservationService,
                "seatCapacity",
                0
        );

        assertThrows(
                IllegalStateException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 5. Past travel date

    @Test
    void bookTicket_PastTravelDate() {

        request.setTravelDate(LocalDate.now().minusDays(1));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 6. Empty passenger list

    @Test
    void bookTicket_NoPassengers() {

        request.setPassengers(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 7. More than six passengers

    @Test
    void bookTicket_MoreThanSixPassengers() {

        PassengerDetailRequest passenger =
                new PassengerDetailRequest();

        passenger.setPassengerName("Test");
        passenger.setAge(20);
        passenger.setGender("Male");

        request.setPassengers(
                List.of(
                        passenger, passenger, passenger,
                        passenger, passenger, passenger,
                        passenger
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 8. Missing travel class

    @Test
    void bookTicket_MissingTravelClass() {

        request.setTravelClass(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 9. Inactive train

    @Test
    void bookTicket_InactiveTrain() {

        train.setActive(false);

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 10. Empty train route

    @Test
    void bookTicket_EmptyRoute() {

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        when(trainServiceClient.getTrainRoute(10L))
                .thenReturn(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 11. Source station not found

    @Test
    void bookTicket_SourceStationNotFound() {

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        when(trainServiceClient.getTrainRoute(10L))
                .thenReturn(route);

        request.setSourceStationId(99L);

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 12. Destination station not found

    @Test
    void bookTicket_DestinationStationNotFound() {

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        when(trainServiceClient.getTrainRoute(10L))
                .thenReturn(route);

        request.setDestinationStationId(99L);

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 13. Invalid route order

    @Test
    void bookTicket_InvalidRouteOrder() {

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        when(trainServiceClient.getTrainRoute(10L))
                .thenReturn(route);

        request.setSourceStationId(3L);
        request.setDestinationStationId(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 14. Fare unavailable

    @Test
    void bookTicket_FareUnavailable() {

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        when(trainServiceClient.getTrainRoute(10L))
                .thenReturn(route);

        when(trainServiceClient.getTrainFares(10L))
                .thenReturn(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 15. No seats available

    @Test
    void bookTicket_NoSeatsAvailable() {

        ReflectionTestUtils.setField(
                reservationService,
                "seatCapacity",
                1
        );

        mockValidTrainDetails();

        when(reservationRepository.saveAndFlush(
                any(Reservation.class)
        )).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            saved.setId(50L);
            return saved;
        });

        SeatAllocation occupied = new SeatAllocation();
        occupied.setSeatNumber("S1");
        occupied.setSegmentNumber(1);

        when(seatAllocationRepository
                .findByTrainIdAndTravelDateAndTravelClassAndSegmentNumberIn(
                        eq(10L),
                        eq(travelDate),
                        eq("AC"),
                        anyList()
                ))
                .thenReturn(List.of(occupied));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.bookTicket(request, EMAIL)
        );
    }

    // 16. Get passenger bookings

    @Test
    void getMyBookings_Success() {

        Reservation reservation = createReservation();

        when(reservationRepository
                .findByPassengerEmailOrderByBookedAtDesc(EMAIL))
                .thenReturn(List.of(reservation));

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        List<ReservationResponse> responses =
                reservationService.getMyBookings(EMAIL);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("PNR1234567", responses.get(0).getPnr());
    }

    // 17. Get passenger bookings when empty

    @Test
    void getMyBookings_EmptyList() {

        when(reservationRepository
                .findByPassengerEmailOrderByBookedAtDesc(EMAIL))
                .thenReturn(List.of());

        List<ReservationResponse> responses =
                reservationService.getMyBookings(EMAIL);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(trainServiceClient, never()).getTrainById(any());
    }

    // 18. Get booking by PNR

    @Test
    void getBookingByPnr_Success() {

        Reservation reservation = createReservation();

        when(reservationRepository.findByPnr("PNR1234567"))
                .thenReturn(Optional.of(reservation));

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        ReservationResponse response =
                reservationService.getBookingByPnr(
                        "PNR1234567", EMAIL
                );

        assertNotNull(response);
        assertEquals("PNR1234567", response.getPnr());
    }

    // 19. PNR not found

    @Test
    void getBookingByPnr_NotFound() {

        when(reservationRepository.findByPnr("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.getBookingByPnr(
                        "INVALID", EMAIL
                )
        );
    }

    // 20. Booking belongs to another passenger

    @Test
    void getBookingByPnr_WrongPassenger() {

        Reservation reservation = createReservation();

        when(reservationRepository.findByPnr("PNR1234567"))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.getBookingByPnr(
                        "PNR1234567", "other@gmail.com"
                )
        );
    }

    // 21. Cancel booking successfully

    @Test
    void cancelBooking_Success() {

        Reservation reservation = createReservation();

        when(reservationRepository.findByPnr("PNR1234567"))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(trainServiceClient.getTrainById(10L))
                .thenReturn(train);

        ReservationResponse response =
                reservationService.cancelBooking(
                        "PNR1234567", EMAIL
                );

        assertNotNull(response);
        assertEquals(ReservationStatus.CANCELLED, response.getStatus());

        verify(seatAllocationRepository)
                .deleteByReservationId(50L);

        verify(reservationRepository).save(reservation);
    }

    // 22. Cancel booking when PNR does not exist

    @Test
    void cancelBooking_NotFound() {

        when(reservationRepository.findByPnr("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.cancelBooking(
                        "INVALID", EMAIL
                )
        );
    }

    // 23. Cancel booking belonging to another passenger

    @Test
    void cancelBooking_WrongPassenger() {

        Reservation reservation = createReservation();

        when(reservationRepository.findByPnr("PNR1234567"))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.cancelBooking(
                        "PNR1234567", "other@gmail.com"
                )
        );
    }

    // 24. Cancel already cancelled booking

    @Test
    void cancelBooking_AlreadyCancelled() {

        Reservation reservation = createReservation();
        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findByPnr("PNR1234567"))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.cancelBooking(
                        "PNR1234567", EMAIL
                )
        );

        verify(seatAllocationRepository, never())
                .deleteByReservationId(any());
    }
}
