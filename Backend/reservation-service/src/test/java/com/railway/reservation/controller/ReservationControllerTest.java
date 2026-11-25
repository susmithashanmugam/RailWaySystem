
package com.railway.reservation.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import com.railway.reservation.dto.CreateReservationRequest;
import com.railway.reservation.dto.ReservationResponse;
import com.railway.reservation.service.ReservationService;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private ReservationController reservationController;

    private final String passengerEmail = "passenger@gmail.com";

    @BeforeEach
    void setUp() {
        when(jwt.getSubject()).thenReturn(passengerEmail);
    }

    @Test
    void bookTicket_Success() {

        CreateReservationRequest request =
                mock(CreateReservationRequest.class);

        ReservationResponse response =
                mock(ReservationResponse.class);

        when(reservationService.bookTicket(request, passengerEmail))
                .thenReturn(response);

        ReservationResponse result =
                reservationController.bookTicket(request, jwt);

        assertSame(response, result);

        verify(reservationService)
                .bookTicket(request, passengerEmail);
    }

    @Test
    void getMyBookings_Success() {

        List<ReservationResponse> bookings =
                List.of(mock(ReservationResponse.class));

        when(reservationService.getMyBookings(passengerEmail))
                .thenReturn(bookings);

        List<ReservationResponse> result =
                reservationController.getMyBookings(jwt);

        assertEquals(1, result.size());
        assertSame(bookings, result);

        verify(reservationService)
                .getMyBookings(passengerEmail);
    }

    @Test
    void getBookingByPnr_Success() {

        String pnr = "PNR123456";

        ReservationResponse response =
                mock(ReservationResponse.class);

        when(reservationService.getBookingByPnr(pnr, passengerEmail))
                .thenReturn(response);

        ReservationResponse result =
                reservationController.getBookingByPnr(pnr, jwt);

        assertSame(response, result);

        verify(reservationService)
                .getBookingByPnr(pnr, passengerEmail);
    }

    @Test
    void cancelBooking_Success() {

        String pnr = "PNR123456";

        ReservationResponse response =
                mock(ReservationResponse.class);

        when(reservationService.cancelBooking(pnr, passengerEmail))
                .thenReturn(response);

        ReservationResponse result =
                reservationController.cancelBooking(pnr, jwt);

        assertSame(response, result);

        verify(reservationService)
                .cancelBooking(pnr, passengerEmail);
    }
}
