
package com.railway.payment.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.railway.payment.client.ReservationServiceClient;
import com.railway.payment.client.NotificationServiceClient;
import com.railway.payment.client.NotificationServiceClient.NotificationRequest;

import com.railway.payment.dto.PaymentResultRequest;

import com.railway.payment.entity.Payment;
import com.railway.payment.entity.PaymentStatus;

import com.railway.payment.exception.PaymentException;
import com.railway.payment.repository.PaymentRepository;

import feign.FeignException;
import jakarta.transaction.Transactional;

@Service
public class RazorpayWebhookService {

    private static final Logger log =
            LoggerFactory.getLogger(RazorpayWebhookService.class);

    private final PaymentRepository paymentRepository;
    private final ReservationServiceClient reservationServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;
    private final String internalApiKey;

    public RazorpayWebhookService(
            PaymentRepository paymentRepository,
            ReservationServiceClient reservationServiceClient,
            ObjectMapper objectMapper,
            @Value("${razorpay.webhook-secret}") String webhookSecret,
            @Value("${internal.api-key}") String internalApiKey,
            NotificationServiceClient notificationServiceClient) {

        this.paymentRepository = paymentRepository;
        this.reservationServiceClient = reservationServiceClient;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
        this.internalApiKey = internalApiKey;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Transactional
    public void processWebhook(
            String rawBody,
            String signature,
            String eventId) {

        // 1. Verify that the webhook came from Razorpay
        if (!verifySignature(rawBody, signature)) {
            throw new PaymentException(
                    "Invalid Razorpay webhook signature");
        }

        try {

            // 2. Read the original webhook JSON
            JsonNode root = objectMapper.readTree(rawBody);

            String event = root.path("event").asText();

            // We only process these three events
            if (!event.equals("payment_link.paid")
                    && !event.equals("payment_link.expired")
                    && !event.equals("payment_link.cancelled")) {
                return;
            }

            JsonNode link = root.path("payload")
                    .path("payment_link")
                    .path("entity");

            String linkId = link.path("id").asText();
            String referenceId = link.path("reference_id").asText();
            String linkStatus = link.path("status").asText();

            if (linkId.isBlank() || referenceId.isBlank()) {
                throw new PaymentException(
                        "Razorpay webhook is missing payment details");
            }

            // 3. Find our payment record
            Payment payment = paymentRepository
                    .findByRazorpayPaymentLinkId(linkId)
                    .orElseThrow(() ->
                            new PaymentException("Payment record not found"));

            // 4. Confirm that this link belongs to this transaction
            if (!payment.getTransactionReference().equals(referenceId)) {
                throw new PaymentException(
                        "Razorpay transaction reference does not match");
            }

            // 5. Ignore duplicate webhook deliveries
            if (eventId != null && eventId.equals(
                    payment.getRazorpayWebhookEventId())) {
                return;
            }

            // 6. Do not change an already completed payment
            if (payment.getStatus() != PaymentStatus.PENDING) {
                return;
            }

            boolean successful = event.equals("payment_link.paid");

            if (successful) {

                long razorpayAmount = link.path("amount").asLong(-1);

                long expectedAmount = payment.getAmount()
                        .movePointRight(2)
                        .longValueExact();

                String currency = link.path("currency").asText();

                if (razorpayAmount != expectedAmount
                        || !"INR".equals(currency)
                        || !"paid".equalsIgnoreCase(linkStatus)) {

                    throw new PaymentException(
                            "Razorpay payment amount or status is invalid");
                }
            }

            // 7. Update Reservation Service
            try {

                reservationServiceClient.updatePaymentResult(
                        payment.getReservationId(),
                        internalApiKey,
                        new PaymentResultRequest(
                                successful,
                                payment.getTransactionReference()));

            } catch (FeignException ex) {

                throw new PaymentException(
                        "Could not update reservation. Razorpay webhook can be retried.");
            }

            // 8. Save the final payment status
            payment.setStatus(
                    successful ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

            payment.setRazorpayWebhookEventId(eventId);

            paymentRepository.save(payment);

            // 9. Send payment notification
            sendPaymentNotification(payment, successful);

        } catch (PaymentException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new PaymentException(
                    "Unable to process Razorpay webhook");
        }
    }

    // Send Razorpay payment notification
    private void sendPaymentNotification(
            Payment payment,
            boolean successful) {

        String message = successful
                ? "Your Razorpay payment of Rs. " + payment.getAmount()
                    + " for PNR " + payment.getPnr()
                    + " was successful. Transaction ID: "
                    + payment.getTransactionReference()
                : "Your Razorpay payment for PNR " + payment.getPnr()
                    + " was unsuccessful or expired. Please try again.";

        try {

            notificationServiceClient.sendPaymentNotification(
                    internalApiKey,
                    new NotificationRequest(
                            payment.getPassengerEmail(),
                            message
                    )
            );

            log.info(
                    "Razorpay payment notification sent for PNR {}",
                    payment.getPnr()
            );

        } catch (Exception ex) {

            // Do not fail webhook processing just because notification failed.
            log.error(
                    "Could not publish Razorpay payment notification for PNR {}",
                    payment.getPnr(),
                    ex
            );
        }
    }

    // Verify Razorpay webhook signature
    private boolean verifySignature(
            String rawBody,
            String receivedSignature) {

        try {

            if (receivedSignature == null
                    || receivedSignature.isBlank()) {
                return false;
            }

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");

            mac.init(secretKey);

            byte[] expectedBytes = mac.doFinal(
                    rawBody.getBytes(StandardCharsets.UTF_8));

            byte[] receivedBytes =
                    HexFormat.of().parseHex(receivedSignature);

            return MessageDigest.isEqual(
                    expectedBytes,
                    receivedBytes);

        } catch (Exception ex) {

            return false;
        }
    }
}
