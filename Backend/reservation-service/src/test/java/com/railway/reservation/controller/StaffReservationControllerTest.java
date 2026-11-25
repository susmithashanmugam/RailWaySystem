
package com.railway.reservation.controller;

import com.railway.reservation.dto.StaffBookingResponse;
import com.railway.reservation.service.ReservationStaffService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StaffReservationControllerTest {

    @Mock
    private ReservationStaffService reservationStaffService;

    @InjectMocks
    private StaffReservationController controller;

    @Test
    void verifyBooking_Success() {

        String pnr = "PNR123456";

        StaffBookingResponse response =
                mock(StaffBookingResponse.class);

        when(reservationStaffService.getBookingForStaff(pnr))
                .thenReturn(response);

        StaffBookingResponse result =
                controller.verifyBooking(pnr);

        assertSame(response, result);

        verify(reservationStaffService).getBookingForStaff(pnr);
    }

    @Test
    void markPassengerBoarded_Success() {

        String pnr = "PNR123456";
        Long ticketId = 10L;

        StaffBookingResponse response =
                mock(StaffBookingResponse.class);

        when(reservationStaffService.markPassengerBoarded(pnr, ticketId))
                .thenReturn(response);

        StaffBookingResponse result =
                controller.markPassengerBoarded(pnr, ticketId);

        assertSame(response, result);

        verify(reservationStaffService)
                .markPassengerBoarded(pnr, ticketId);
    }
}
