
package com.railway.reservation.repository;

import com.railway.reservation.entity.PassengerTicket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerTicketRepository
        extends JpaRepository<PassengerTicket, Long> {

    List<PassengerTicket> findByReservation_Id(Long reservationId);
}
