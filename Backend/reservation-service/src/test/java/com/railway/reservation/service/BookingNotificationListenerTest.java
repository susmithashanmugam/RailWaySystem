
package com.railway.reservation.service;

import com.railway.reservation.event.BookingConfirmedEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingNotificationListenerTest {

    @Mock
    private BookingNotificationService notificationService;

    @InjectMocks
    private BookingNotificationListener notificationListener;

    @Test
    void handleBookingConfirmed_Success() {

        BookingConfirmedEvent event =
                new BookingConfirmedEvent(
                        "PNR123456",
                        "passenger@gmail.com"
                );

        notificationListener.handleBookingConfirmed(event);

        verify(notificationService).sendBookingConfirmation(
                "PNR123456",
                "passenger@gmail.com"
        );
    }

    @Test
    void handleBookingConfirmed_NullValues() {

        BookingConfirmedEvent event =
                new BookingConfirmedEvent(null, null);

        notificationListener.handleBookingConfirmed(event);

        verify(notificationService).sendBookingConfirmation(
                null, null
        );
    }
}
