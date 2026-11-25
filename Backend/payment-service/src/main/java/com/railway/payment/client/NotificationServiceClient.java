package com.railway.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "messaging-service")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/payment")
    void sendPaymentNotification(
            @RequestHeader("X-INTERNAL-API-KEY") String apiKey,
            @RequestBody NotificationRequest request);

    record NotificationRequest(
            String recipientEmail,
            String message) {
    }
}