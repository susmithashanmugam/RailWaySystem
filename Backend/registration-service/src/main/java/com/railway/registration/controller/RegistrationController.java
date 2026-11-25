
package com.railway.registration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.railway.registration.dto.CreateStaffRequest;
import com.railway.registration.dto.RegistrationRequest;
import com.railway.registration.dto.RegistrationResponse;
import com.railway.registration.service.RegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    // Constructor for dependency injection
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    // Passenger Registration
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerPassenger(
            @Valid @RequestBody RegistrationRequest request) {

        RegistrationResponse response =
                registrationService.registerPassenger(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Admin creates Staff
    @PostMapping("/admin/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse createStaff(
            @Valid @RequestBody CreateStaffRequest request) {

        return registrationService.createStaff(request);
    }
}
