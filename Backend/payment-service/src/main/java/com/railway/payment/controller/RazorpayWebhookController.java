
package com.railway.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.railway.payment.service.RazorpayWebhookService;

@RestController
@RequestMapping("/api/payments/razorpay")
public class RazorpayWebhookController {

    private final RazorpayWebhookService webhookService;

    public RazorpayWebhookController(
            RazorpayWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String rawBody,
            @RequestHeader(
                    value = "X-Razorpay-Signature",
                    required = false) String signature,
            @RequestHeader(
                    value = "x-razorpay-event-id",
                    required = false) String eventId) {

        webhookService.processWebhook(rawBody, signature, eventId);

        return ResponseEntity.ok("Webhook processed");
    }
}
