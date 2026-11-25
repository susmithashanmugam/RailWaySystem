
package com.railway.payment.service;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.railway.payment.client.RazorpayClient;
import com.railway.payment.client.ReservationServiceClient;
import com.railway.payment.client.NotificationServiceClient;
import com.railway.payment.client.NotificationServiceClient.NotificationRequest;

import com.railway.payment.dto.CreatePaymentRequest;
import com.railway.payment.dto.PaymentResponse;
import com.railway.payment.dto.PaymentResultRequest;
import com.railway.payment.dto.ReservationPaymentDetails;

import com.railway.payment.entity.Payment;
import com.railway.payment.entity.PaymentMethod;
import com.railway.payment.entity.PaymentStatus;

import com.railway.payment.exception.PaymentException;
import com.railway.payment.repository.PaymentRepository;

import feign.FeignException;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final ReservationServiceClient reservationServiceClient;
    private final RazorpayClient razorpayClient;
    private final NotificationServiceClient notificationServiceClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    // Constructor
    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationServiceClient reservationServiceClient,
            RazorpayClient razorpayClient,
            NotificationServiceClient notificationServiceClient) {

        this.paymentRepository = paymentRepository;
        this.reservationServiceClient = reservationServiceClient;
        this.razorpayClient = razorpayClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    // Process passenger payment
    public PaymentResponse processPayment(
            CreatePaymentRequest request,
            String passengerEmail) {

        ReservationPaymentDetails reservation;

        // Get reservation details
        try {
            reservation = reservationServiceClient
                    .getReservationPaymentDetails(
                            request.getReservationId());

        } catch (FeignException.NotFound ex) {
            throw new PaymentException("Reservation not found");
        }

        // Check passenger ownership
        if (!reservation.getPassengerEmail()
                .equalsIgnoreCase(passengerEmail)) {

            throw new PaymentException(
                    "You cannot pay for another passenger's reservation");
        }

        // Check reservation status
        if (!"PENDING_PAYMENT".equals(reservation.getStatus())) {
            throw new PaymentException(
                    "This reservation is not awaiting payment");
        }

        // Check successful payment
        if (paymentRepository.existsByReservationIdAndStatus(
                request.getReservationId(), PaymentStatus.SUCCESS)) {

            throw new PaymentException(
                    "Payment has already been completed for this reservation");
        }

        // Check pending payment
        if (paymentRepository.existsByReservationIdAndStatus(
                request.getReservationId(), PaymentStatus.PENDING)) {

            throw new PaymentException(
                    "A payment is already being processed for this reservation");
        }

        // Create payment record
        Payment payment = new Payment();

        payment.setReservationId(reservation.getReservationId());
        payment.setPnr(reservation.getPnr());
        payment.setPassengerEmail(passengerEmail);
        payment.setAmount(reservation.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);

        // Generate transaction reference
        String transactionReference = "TXN-" +
                UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 12);

        payment.setTransactionReference(transactionReference);

        payment = paymentRepository.saveAndFlush(payment);

        // Razorpay payment flow
        if (request.getPaymentMethod() == PaymentMethod.RAZORPAY) {
            return createRazorpayPaymentLink(payment);
        }

        // Existing simulated payment flow for other methods
        return processExistingPayment(payment, reservation, request);
    }

    // Create Razorpay Payment Link
    private PaymentResponse createRazorpayPaymentLink(Payment payment) {

        // Convert rupees into paise
        long amountInPaise = payment.getAmount()
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        if (amountInPaise <= 0) {
            throw new PaymentException(
                    "Payment amount must be positive");
        }

        try {

            // Ask RazorpayClient to create the payment link
            Map<String, Object> link =
                    razorpayClient.createPaymentLink(
                            amountInPaise,
                            payment.getTransactionReference(),
                            "Railway ticket payment - PNR: "
                                    + payment.getPnr()
                    );

            // Check Razorpay response
            if (link == null
                    || link.get("id") == null
                    || link.get("short_url") == null) {

                throw new IllegalStateException(
                        "Razorpay did not return a valid payment link");
            }

            // Store Razorpay payment link ID
            payment.setRazorpayPaymentLinkId(
                    link.get("id").toString());

            // Store Razorpay payment URL
            payment.setPaymentLinkUrl(
                    link.get("short_url").toString());

            payment = paymentRepository.save(payment);

            // Return payment response
            return convertToResponse(payment);

        } catch (RuntimeException ex) {

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            throw new PaymentException(
                    "Could not create Razorpay payment link. Please try again.");
        }
    }

    // Existing payment processing for other methods
    private PaymentResponse processExistingPayment(
            Payment payment,
            ReservationPaymentDetails reservation,
            CreatePaymentRequest request) {

        boolean successful = !request.isSimulateFailure();

        try {
            reservationServiceClient.updatePaymentResult(
                    reservation.getReservationId(),
                    internalApiKey,
                    new PaymentResultRequest(
                            successful,
                            payment.getTransactionReference()));

        } catch (FeignException ex) {

            throw new PaymentException(
                    "Payment result could not be confirmed with Reservation Service. "
                            + "The payment is pending reconciliation.");
        }

        payment.setStatus(
                successful ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

        payment = paymentRepository.save(payment);

        // Send payment notification
        sendPaymentNotification(
                payment,
                successful
        );

        return convertToResponse(payment);
    }

    // Send payment notification
    private void sendPaymentNotification(
            Payment payment,
            boolean successful) {

        String message = successful
                ? "Your payment of Rs. " + payment.getAmount()
                    + " for PNR " + payment.getPnr()
                    + " was successful. Transaction ID: "
                    + payment.getTransactionReference()
                : "Your payment for PNR " + payment.getPnr()
                    + " was unsuccessful. Please try again.";

        try {
            notificationServiceClient.sendPaymentNotification(
                    internalApiKey,
                    new NotificationRequest(
                            payment.getPassengerEmail(),
                            message
                    )
            );

            log.info(
                    "Payment notification sent for PNR {}",
                    payment.getPnr()
            );

        } catch (Exception ex) {

            // Notification failure must not undo a completed payment.
            log.error(
                    "Could not publish payment notification for PNR {}",
                    payment.getPnr(),
                    ex
            );
        }
    }

    // Get passenger payment history
    public List<PaymentResponse> getMyPayments(String passengerEmail) {

        return paymentRepository
                .findByPassengerEmailOrderByCreatedAtDesc(passengerEmail)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Convert entity into response DTO
    private PaymentResponse convertToResponse(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getReservationId(),
                payment.getPnr(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionReference(),
                payment.getCreatedAt(),
                payment.getPaymentLinkUrl());
    }
}
