
package com.railway.reservation.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railway.reservation.dto.PaymentResultRequest;
import com.railway.reservation.dto.ReservationPaymentDetailsResponse;
import com.railway.reservation.entity.Reservation;
import com.railway.reservation.entity.ReservationStatus;
import com.railway.reservation.event.BookingConfirmedEvent;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.repository.ReservationRepository;
import com.railway.reservation.repository.SeatAllocationRepository;

@Service
public class ReservationPaymentService {

    private final ReservationRepository reservationRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationPaymentService(
            ReservationRepository reservationRepository,
            SeatAllocationRepository seatAllocationRepository,
            ApplicationEventPublisher eventPublisher) {

        this.reservationRepository = reservationRepository;
        this.seatAllocationRepository = seatAllocationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public ReservationPaymentDetailsResponse getReservationForPayment(
            Long reservationId,
            String passengerEmail) {

        Reservation reservation = reservationRepository
                .findByIdAndPassengerEmail(reservationId, passengerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Reservation is not awaiting payment");
        }

        return new ReservationPaymentDetailsResponse(
                reservation.getId(),
                reservation.getPnr(),
                reservation.getPassengerEmail(),
                reservation.getTotalAmount(),
                reservation.getStatus().name());
    }

    @Transactional
    public void updatePaymentResult(
            Long reservationId,
            PaymentResultRequest request) {

        if (request == null || request.getSuccessful() == null) {
            throw new IllegalArgumentException(
                    "Payment result is required");
        }

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found"));

        boolean successful = Boolean.TRUE.equals(request.getSuccessful());

        // Handle duplicate callbacks safely.
        if (successful &&
                reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return;
        }

        if (!successful &&
                reservation.getStatus() == ReservationStatus.CANCELLED) {
            return;
        }

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Payment result cannot be applied to this reservation");
        }

        if (successful) {

            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);

            eventPublisher.publishEvent(
                    new BookingConfirmedEvent(
                            reservation.getPnr(),
                            reservation.getPassengerEmail()));

        } else {

            reservation.setStatus(ReservationStatus.CANCELLED);

            seatAllocationRepository.deleteByReservationId(
                    reservationId);

            reservationRepository.save(reservation);
        }
    }
}
