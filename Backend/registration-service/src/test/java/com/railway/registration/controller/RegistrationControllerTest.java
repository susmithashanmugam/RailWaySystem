package com.railway.registration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.railway.registration.dto.RegistrationRequest;
import com.railway.registration.dto.RegistrationResponse;
import com.railway.registration.service.RegistrationService;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    private RegistrationRequest request;
    private RegistrationResponse response;

    @BeforeEach
    void setUp() {
        request = new RegistrationRequest();
        request.setFullName("Test Passenger");
        request.setEmail("test@gmail.com");
        request.setPhoneNumber("9876543210");
        request.setPassword("Test@123");

        response = new RegistrationResponse(
                1L,
                "Test Passenger",
                "test@gmail.com",
                "ROLE_PASSENGER",
                "Registration successful"
        );
    }

    @Test
    void registerPassenger_ShouldReturnCreatedResponse() {

        when(registrationService.registerPassenger(request))
                .thenReturn(response);

        ResponseEntity<RegistrationResponse> result =
                registrationController.registerPassenger(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());

        verify(registrationService).registerPassenger(request);
    }
}
