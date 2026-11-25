
package com.railway.reservation.controller;

import com.railway.reservation.dto.PaymentResultRequest;
import com.railway.reservation.service.ReservationPaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalReservationPaymentControllerTest {

    @Mock
    private ReservationPaymentService reservationPaymentService;

    @InjectMocks
    private InternalReservationPaymentController controller;

    private final String validApiKey = "test-secret-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                controller,
                "internalApiKey",
                validApiKey
        );
    }

    @Test
    void updatePaymentResult_ValidApiKey_Success() {

        Long reservationId = 101L;

        PaymentResultRequest request =
                mock(PaymentResultRequest.class);

        assertDoesNotThrow(() ->
                controller.updatePaymentResult(
                        reservationId,
                        validApiKey,
                        request
                )
        );

        verify(reservationPaymentService)
                .updatePaymentResult(reservationId, request);
    }

    @Test
    void updatePaymentResult_InvalidApiKey_Unauthorized() {

        Long reservationId = 101L;

        PaymentResultRequest request =
                mock(PaymentResultRequest.class);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> controller.updatePaymentResult(
                                reservationId,
                                "wrong-api-key",
                                request
                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verify(reservationPaymentService, never())
                .updatePaymentResult(anyLong(), any());
    }
}
