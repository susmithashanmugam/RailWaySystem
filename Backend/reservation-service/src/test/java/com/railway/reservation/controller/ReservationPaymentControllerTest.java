
package com.railway.reservation.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import com.railway.reservation.dto.ReservationPaymentDetailsResponse;
import com.railway.reservation.service.ReservationPaymentService;

@ExtendWith(MockitoExtension.class)
class ReservationPaymentControllerTest {

    @Mock
    private ReservationPaymentService reservationPaymentService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private ReservationPaymentController controller;

    @Test
    void getReservationForPayment_Success() {

        Long reservationId = 101L;
        String passengerEmail = "passenger@gmail.com";

        ReservationPaymentDetailsResponse response =
                mock(ReservationPaymentDetailsResponse.class);

        when(jwt.getSubject()).thenReturn(passengerEmail);

        when(reservationPaymentService.getReservationForPayment(
                reservationId, passengerEmail))
                .thenReturn(response);

        ReservationPaymentDetailsResponse result =
                controller.getReservationForPayment(reservationId, jwt);

        assertSame(response, result);

        verify(reservationPaymentService).getReservationForPayment(
                reservationId, passengerEmail);
    }
}
