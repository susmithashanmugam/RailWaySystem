
package com.railway.reservation.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.railway.reservation.event.BookingConfirmedEvent;

@Component
public class BookingNotificationListener {

    private final BookingNotificationService notificationService;

    public BookingNotificationListener(
            BookingNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookingConfirmed(BookingConfirmedEvent event) {

        notificationService.sendBookingConfirmation(
                event.pnr(),
                event.passengerEmail()
        );
    }
}
