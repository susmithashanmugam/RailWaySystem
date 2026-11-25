
package com.railway.reservation.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.railway.reservation.dto.SeatAvailabilityResponse;
import com.railway.reservation.service.SeatAvailabilityService;

@RestController
@RequestMapping("/api/reservations")
public class AvailabilityController {

    private final SeatAvailabilityService seatAvailabilityService;

    // Constructor injection (replaces Lombok)
    public AvailabilityController(SeatAvailabilityService seatAvailabilityService) {
        this.seatAvailabilityService = seatAvailabilityService;
    }

    @GetMapping("/availability")
    public SeatAvailabilityResponse checkAvailability(
            @RequestParam Long trainId,
            @RequestParam Long sourceStationId,
            @RequestParam Long destinationStationId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate travelDate,
            @RequestParam String travelClass) {

        return seatAvailabilityService.checkAvailability(
                trainId,
                sourceStationId,
                destinationStationId,
                travelDate,
                travelClass
        );
    }
}
