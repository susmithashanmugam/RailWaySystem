
package com.railway.reservation.event;

public record BookingConfirmedEvent(
        String pnr,
        String passengerEmail
) {
}
