
package com.railway.reservation.controller;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.railway.reservation.dto.SeatAvailabilityResponse;
import com.railway.reservation.service.SeatAvailabilityService;

@ExtendWith(MockitoExtension.class)
class AvailabilityControllerTest {

    @Mock
    private SeatAvailabilityService seatAvailabilityService;

    @InjectMocks
    private AvailabilityController availabilityController;

    @Test
    void checkAvailability_Success() {

        Long trainId = 101L;
        Long sourceStationId = 1L;
        Long destinationStationId = 5L;
        LocalDate travelDate = LocalDate.now().plusDays(5);
        String travelClass = "SLEEPER";

        SeatAvailabilityResponse response =
                mock(SeatAvailabilityResponse.class);

        when(seatAvailabilityService.checkAvailability(
                trainId,
                sourceStationId,
                destinationStationId,
                travelDate,
                travelClass
        )).thenReturn(response);

        SeatAvailabilityResponse result =
                availabilityController.checkAvailability(
                        trainId,
                        sourceStationId,
                        destinationStationId,
                        travelDate,
                        travelClass
                );

        assertSame(response, result);

        verify(seatAvailabilityService).checkAvailability(
                trainId,
                sourceStationId,
                destinationStationId,
                travelDate,
                travelClass
        );
    }
}
