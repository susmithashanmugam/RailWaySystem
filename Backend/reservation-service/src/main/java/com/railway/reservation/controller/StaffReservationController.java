
package com.railway.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.reservation.dto.StaffBookingResponse;
import com.railway.reservation.service.ReservationStaffService;

@RestController
@RequestMapping("/api/reservations/staff")
public class StaffReservationController {

    private final ReservationStaffService reservationStaffService;

    // Constructor injection (replaces Lombok)
    public StaffReservationController(
            ReservationStaffService reservationStaffService) {
        this.reservationStaffService = reservationStaffService;
    }

    @GetMapping("/pnr/{pnr}")
    public StaffBookingResponse verifyBooking(@PathVariable String pnr) {
        return reservationStaffService.getBookingForStaff(pnr);
    }

    @PatchMapping("/pnr/{pnr}/tickets/{ticketId}/board")
    public StaffBookingResponse markPassengerBoarded(
            @PathVariable String pnr,
            @PathVariable Long ticketId) {

        return reservationStaffService.markPassengerBoarded(pnr, ticketId);
    }
}