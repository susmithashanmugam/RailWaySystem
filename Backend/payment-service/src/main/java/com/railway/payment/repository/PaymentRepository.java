
package com.railway.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.railway.payment.entity.Payment;
import com.railway.payment.entity.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPassengerEmailOrderByCreatedAtDesc(
            String passengerEmail);

    List<Payment> findByReservationIdOrderByCreatedAtDesc(
            Long reservationId);

    Optional<Payment> findByTransactionReference(
            String transactionReference);

    Optional<Payment> findByRazorpayPaymentLinkId(
            String razorpayPaymentLinkId);

    boolean existsByReservationIdAndStatus(
            Long reservationId,
            PaymentStatus status);
}
