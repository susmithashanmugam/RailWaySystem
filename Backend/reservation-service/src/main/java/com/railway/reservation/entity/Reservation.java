
package com.railway.reservation.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "reservations",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "pnr")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String pnr;

    @Column(name = "passenger_email", nullable = false)
    private String passengerEmail;

    @Column(name = "train_id", nullable = false)
    private Long trainId;

    @Column(name = "source_station_id", nullable = false)
    private Long sourceStationId;

    @Column(name = "destination_station_id", nullable = false)
    private Long destinationStationId;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "travel_class", nullable = false, length = 20)
    private String travelClass;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "booked_at", nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    @OneToMany(
        mappedBy = "reservation",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PassengerTicket> tickets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        bookedAt = LocalDateTime.now();
    }
}
