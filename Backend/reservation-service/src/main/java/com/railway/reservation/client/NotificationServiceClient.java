
package com.railway.reservation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "messaging-service")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/booking")
    void sendBookingNotification(
            @RequestHeader("X-INTERNAL-API-KEY") String apiKey,
            @RequestBody NotificationRequest request
    );

    record NotificationRequest(
            String recipientEmail,
            String message
    ) {}
}
