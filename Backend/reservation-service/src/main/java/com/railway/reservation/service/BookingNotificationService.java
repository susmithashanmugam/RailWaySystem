
package com.railway.reservation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.railway.reservation.client.NotificationServiceClient;
import com.railway.reservation.client.NotificationServiceClient.NotificationRequest;

@Service
public class BookingNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(BookingNotificationService.class);

    private final NotificationServiceClient notificationClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    public BookingNotificationService(
            NotificationServiceClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void sendBookingConfirmation(
            String pnr,
            String passengerEmail) {

        try {
            NotificationRequest request = new NotificationRequest(
                    passengerEmail,
                    "Your railway booking is confirmed. PNR: " + pnr
            );

            notificationClient.sendBookingNotification(
                    internalApiKey,
                    request
            );

            log.info("Booking notification submitted for PNR: {}", pnr);

        } catch (Exception exception) {
            log.error("Booking notification failed for PNR: {}", pnr,
                    exception);
        }
    }
}
