
package com.railway.reservation.repository;

import com.railway.reservation.entity.Reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByPnr(String pnr);

    List<Reservation> findByPassengerEmailOrderByBookedAtDesc(
            String passengerEmail
    );

    Optional<Reservation> findByIdAndPassengerEmail(
            Long reservationId,
            String passengerEmail
    );

    boolean existsByPnr(String pnr);
}
