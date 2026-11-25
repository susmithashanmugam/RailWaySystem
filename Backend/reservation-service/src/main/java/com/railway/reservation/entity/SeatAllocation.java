
package com.railway.reservation.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
    name = "seat_allocations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_seat_segment",
            columnNames = {
                "train_id",
                "travel_date",
                "travel_class",
                "seat_number",
                "segment_number"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class SeatAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "train_id", nullable = false)
    private Long trainId;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "travel_class", nullable = false, length = 20)
    private String travelClass;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(name = "segment_number", nullable = false)
    private Integer segmentNumber;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;
}
