
package com.railway.reservation.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.railway.reservation.entity.SeatAllocation;

@Repository
public interface SeatAllocationRepository
        extends JpaRepository<SeatAllocation, Long> {

    List<SeatAllocation> findByTrainIdAndTravelDateAndTravelClassAndSegmentNumberIn(
            Long trainId,
            LocalDate travelDate,
            String travelClass,
            List<Integer> segmentNumbers
    );

    void deleteByReservationId(Long reservationId);

    List<SeatAllocation> findByReservationId(Long reservationId);
}
